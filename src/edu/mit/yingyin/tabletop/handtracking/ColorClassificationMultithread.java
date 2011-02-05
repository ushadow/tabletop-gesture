package edu.mit.yingyin.tabletop.handtracking;

import handtracking.camera.LowLatencyPCUtility;
import handtracking.camera.Webcam;
import handtracking.camera.WebcamType;
import handtracking.camera.LowLatencyPCUtility.DriverInterface;
import handtracking.camera.LowLatencyPCUtility.ProcessingInterface;
import handtracking.colorclassification.ColorCalibrationExample;
import handtracking.colorclassification.ColorClassificationModel;
import handtracking.imageprocess.DenoisingClassifier;
import handtracking.imageprocess.ImageNormalization;
import handtracking.processpipeline.ProcessPacket;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import rywang.image.FilterImage.ImageComponent;
import rywang.util.awt.WindowUtils;
import yingyin.webcam.WebcamDriverFirei;
import yingyin.webcam.ControlDialog;
import yingyin.preprocess.PreProcess;
import corejava.PrintfFormat;
import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.tabletop.environment.LeftGloveColorCalibrationExample;
import edu.mit.yingyin.tabletop.environment.StandardEnvironment;

public class ColorClassificationMultithread extends KeyAdapter {

	private enum RecordType {NORM, DEBUG};
	private enum DebugOption {DECODED, DENOISED};
	
	private static final WebcamType CAM_TYPE = StandardEnvironment.getCameraType();
	
  private boolean USE_NATIVE = true;
  
  private RecordType REC_TYPE = RecordType.DEBUG;
  private DebugOption DEBUG_OPTION = DebugOption.DECODED; 
  
  private boolean bgSub = true;
  private boolean recording = false;
  
  private String recordingDirectory = EnvConstants.RECORDING_DIR;
  
  private List<BufferedImage> recordedImages;
  
  public class MyProcessor implements ProcessingInterface {

    private ColorClassificationModel model;
    
    private PreProcess nativeDecoding;
    
    public MyProcessor(ColorCalibrationExample example) {
      bgSub = example.useBgSubtract();
      nativeDecoding = new PreProcess(example.getPrefix(),example.getColorTransformationMethod(),
      		640, 480, 2, bgSub, true);
      
      if(bgSub)
      	nativeDecoding.setBackground();
    }
    
    @Override
    public void initialize() {
    }
    
    public void cleanUp()
    {
    	nativeDecoding.cleanUp();
    }

    @Override
    public ProcessPacket process(BufferedImage image, int index) {
      ProcessPacket packet = new ProcessPacket();
      if (!USE_NATIVE) {
        packet.debugImage = DenoisingClassifier.classifyImage(image, model);
        ImageNormalization normalization = new ImageNormalization(40);
        packet.normalizedToOriginal = normalization
            .getTransformOfColorImageRobust(packet.debugImage);
        packet.normalizedImage = normalization.normalizeImageByTransform(
            packet.debugImage, packet.normalizedToOriginal);
      } else {
        packet.debugImage = new BufferedImage(image.getWidth(), image
            .getHeight(), BufferedImage.TYPE_INT_ARGB);
        packet.normalizedImage = new BufferedImage(40, 40,
            BufferedImage.TYPE_INT_ARGB);
        packet.normalizedToOriginal = new AffineTransform();
        nativeDecoding.filter(image, packet.normalizedImage,
            packet.normalizedToOriginal);
        switch(DEBUG_OPTION)
        {
        	case DECODED: 
        		nativeDecoding.getLastDecoded(packet.debugImage);
        		break;
        	case DENOISED:
        		nativeDecoding.getLastDenoised(packet.debugImage);
        		break;
        	default:
        		break;
        }
      }
      
      if (recording) {
        if (recordedImages == null) recordedImages = new ArrayList<BufferedImage>();
        BufferedImage bi = null;
        
        switch(REC_TYPE)
        {
        case NORM:
        	bi = packet.normalizedImage;
        	break;
        case DEBUG:
        	bi = packet.debugImage;
        	break;
      	default:
      		break;
        }
        recordedImages.add(bi);
        System.out.println("Recorded image: " + recordedImages.size());
      }
      
      return packet;
    }
  }
  
  public class MyDriver implements DriverInterface {
    
    private ImageComponent normalizedComponent;
    
    private ImageComponent decodedComponent;
    
    private JFrame cameraFrame;
    
    public MyDriver(JFrame cameraFrame) {
      this.cameraFrame = cameraFrame;
    }

    @Override
    public void handleDebugPacket(ProcessPacket state, int frame) {
      if (normalizedComponent == null) {
        normalizedComponent = new ImageComponent(state.normalizedImage);
        JFrame nFrame = WindowUtils.makeWindowWrapComponent(normalizedComponent, "N", false);
        nFrame.setLocation(cameraFrame.getWidth(), 0);
        
        if (bgSub)
        {
        	decodedComponent = new ImageComponent(state.debugImage);
        	JFrame dFrame = WindowUtils.makeWindowWrapComponent(decodedComponent, "D", false);
        	dFrame.setLocation(0, cameraFrame.getHeight());
        }
      }
      
      normalizedComponent.setImage(state.normalizedImage);
      normalizedComponent.repaint();
      if (bgSub) {
        decodedComponent.setImage(state.debugImage);
        decodedComponent.repaint();
      }
    }

    @Override
    public void initialize() {
    }
  }
  
  public void keyPressed(KeyEvent e) 
  {
    switch(e.getKeyChar()){
    	case ' ':
    		//toggle continuous recording
    		recording = !recording;
    		System.out.println("Recording: " + recording);
    		break;
    	case 's':
    		//save continuous recording
    		if (recordedImages != null) {
		      for (int i=0; i<recordedImages.size(); i++) {
		        String filename = recordingDirectory
		            + new PrintfFormat("capture%05d.png").sprintf(i + 1);
		        System.out.println("Writing file: " + filename);
		        try {
		          ImageIO.write(recordedImages.get(i), "PNG", new File(filename));
		        } catch (IOException ioe) {
		          ioe.printStackTrace();
		          throw new RuntimeException(ioe.getMessage());
		        }
		      }
    		}
    		break;
    	default:
    		if(Character.isDigit(e.getKeyChar()))
    			DEBUG_OPTION = DebugOption.values()[new Integer(e.getKeyChar())];
    		break;
    }
  }

  
  public ColorClassificationMultithread() {
    final Webcam camera = new Webcam(CAM_TYPE, 320, 240);
    camera.setCameraIndex(0);

    int numThreads = 1;
    List<ProcessingInterface> processors = new ArrayList<ProcessingInterface>();
    for (int i=0; i<numThreads; i++) {
      processors.add(new MyProcessor(LeftGloveColorCalibrationExample.getInstance()));
    }
    
    final JFrame frame = WindowUtils.makeWindowWrapComponent(camera.getImageComponent(), "Camera viewer", false);
    frame.addKeyListener(this);
    
    final LowLatencyPCUtility pcu = new LowLatencyPCUtility();
    
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        // Run this on another thread than the AWT event queue
        new Thread(new Runnable() {
          public void run() {
            pcu.setDone();
            camera.getDriver().cleanUp();
            frame.setVisible(false);
            frame.dispose();
          }
        }).start();
      }
    });    
    pcu.start(camera, processors, new MyDriver(frame)); 
    switch(CAM_TYPE)
    {
	    case POINT_GREY:
	    	break;
	    case FIREI:
	    	ControlDialog cd = new ControlDialog((WebcamDriverFirei)camera.getDriver());
	    	cd.showUI();
	    	break;
    }
  }
  
  
  public static void main(String[] args) {
    new ColorClassificationMultithread();
  }
}
