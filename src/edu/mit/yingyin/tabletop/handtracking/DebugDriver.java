/**
 * 
 */
package edu.mit.yingyin.tabletop.handtracking;

import handtracking.camera.CustomCameraRenderer;
import handtracking.camera.FPSCounter;
import handtracking.camera.geometriccalibration.MatlabCameraCalibration;
import handtracking.processpipeline.ProcessPacket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.tabletop.handtracking.MyProcessor;
import edu.mit.yingyin.tabletop.handtracking.LowLatencyPCUtility.DriverInterface;

import rywang.image.FilterImage.ImageComponent;
import rywang.util.ObjectIO;
import yingyin.gui.WindowUtils;
import skinning.SkeletonState;
import skinning.examples.SkinningExample;
import skinning.examples.SkinningExample.SSDSkinner;
import yingyin.gui.StatusBar;

public class DebugDriver extends KeyAdapter implements DriverInterface {

	private final String REC_DIR = EnvConstants.RECORDING_DIR;
	private static final String GESTURE_NAMES_FILE_PATH = EnvConstants.MAIN_FOLDER + "data/Gesture/Gestures.txt";
	
	private int debugOption = 2;
	
  public SkinningExample example;
  public SSDSkinner skinner;
  private MatlabCameraCalibration calibration;
  private CustomCameraRenderer renderer;
  private ImageComponent debugComponent;
  private AffineTransform downScale;
  private ProcessUtility pu;
  private Graphics2D bufferGraphics;
  private JFrame frame3;
  private JFrame cameraFrame;
  private DriverInterface follower;
  private boolean showPrior;
  private boolean recording = false;
  private List<SkeletonState> listRecording;
  private List<String> gestures = new ArrayList<String>();
  private String lastAccessedDir = EnvConstants.RECORDING_DIR;
  private String gestureName;
  private String title;
  private StatusBar sb;
  private int currentGesture = -1, currentIndex = 1, debugImgIndex = 1;
  private FPSCounter fpsCounter;
  private NumberFormat format;
  private BufferedImage debugImage;

  public DebugDriver(SkinningExample example,
      MatlabCameraCalibration calibration, ProcessUtility pu,
      List<MyProcessor> processors, JFrame cameraFrame, DriverInterface follower) {
    this.example = example;
    this.calibration = calibration;
    this.pu = pu;
    this.skinner = new SSDSkinner(example);
    this.cameraFrame = cameraFrame;
    this.follower = follower;
    listRecording = new ArrayList<SkeletonState>();
    
    boolean exists = (new File(REC_DIR)).exists();
    if(!exists)
  	  (new File(REC_DIR)).mkdir();
    
    fpsCounter = new FPSCounter();
    format = NumberFormat.getInstance();
    
  	File gestureNamesFile = new File(GESTURE_NAMES_FILE_PATH);
		try {
			Scanner scanner = new Scanner(gestureNamesFile);
			while(scanner.hasNext())
				gestures.add(scanner.next());
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
  }
  
  @Override
  public void handleDebugPacket(ProcessPacket dp, int frame) {
  	fpsCounter.computeFPS();
  	if (renderer != null) {
      if (showPrior || dp.filtered == null) {
        renderer.setSkin(skinner.skin(dp.prior));
        
        System.out.println(this.getClass() + ":" + "rendering prior");
        if(recording)
      	  listRecording.add(dp.prior);
      }
      else
        renderer.setSkin(skinner.skin(dp.filtered));
    } 
    
    frame3.setTitle(title + " FPS = " + format.format(fpsCounter.getLastFPS()));
    downScale.setToIdentity();
    downScale.scale(0.5f, 0.5f);
    switch(debugOption) {
  	  case 1:
  	  	debugImage = pu.getDatabase().getMetric().viewFeature(dp.imageFeature);
  		  downScale.concatenate(dp.normalizedToOriginal);
	      bufferGraphics.setColor(Color.black);
	      bufferGraphics.fillRect(0, 0, 320, 240);
	      bufferGraphics.drawImage(debugImage,downScale, null);
  	      
        AffineTransform T = bufferGraphics.getTransform();
        bufferGraphics.transform(downScale);
        bufferGraphics.setColor(Color.red);
        bufferGraphics.drawRect(0, 0, 40, 40);
        bufferGraphics.setTransform(T);
  		  break;
  	  case 2:
  		  debugImage = pu.getBackgroundSubtractedImg();
  		  bufferGraphics.drawImage(debugImage, downScale,null);
  		  break;
  	  default:
  		  break;
    }
    debugComponent.repaint();
    if (follower != null)
      follower.handleDebugPacket(dp, frame);
 }

    @Override
    public void initialize() {
      if (follower != null) {
        follower.initialize();
      } else {
        renderer = new CustomCameraRenderer(null);
        Frame frame = renderer.start(calibration);
        frame.setLocation(cameraFrame.getWidth(), 0);
      }
      if (debugComponent == null) {
        BufferedImage buffer = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
        bufferGraphics = buffer.createGraphics();
        debugComponent = new ImageComponent(buffer);
        title = "N";
        frame3 = WindowUtils.makeWindowWrapComponent(debugComponent, title, false);
        sb = new StatusBar();
        frame3.getContentPane().add(sb, BorderLayout.SOUTH);

        frame3.setLocation(0, cameraFrame.getHeight());
        frame3.addKeyListener(this);
        downScale = new AffineTransform();
      }
  }
  
  public void keyPressed(KeyEvent e) {
    switch(e.getKeyChar()) {
    
    case 'a':
  	  //auto saving of skeleton states without asking for file name
  	  if(listRecording.isEmpty()){
  		  sb.setMessage(gestureName + " is empty.");
  		  break;
  	  }
  	  try {
			ObjectIO.writeObject(listRecording, REC_DIR + gestureName + ".ss");
			sb.setMessage(gestureName + " saved.");
  	  } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
  	  }
  	  listRecording.clear();
  	  break;
  	  
    case 'b':
    	//save debug image
			try{
				ImageIO.write(debugImage, "PNG", new File(REC_DIR + "image" + (debugImgIndex++) + ".png"));
			} catch (IOException e2){
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
    	break;
    	
    case 'c':
  	  //toggle recording
  	  //clear the recording list before starting the new recording
  	  recording = !recording;
  	  if(recording == true)
  		  listRecording.clear();
  	  
  	  sb.setMessage("Recording: " + recording);
  	  break;
  	  
    case 'd':
  	  //decrease index for the current gesture
  	  currentIndex--;
  	  if(currentIndex < 1)
  		  currentIndex = 1;
  	  gestureName = String.format("%s_%03d", gestures.get(currentGesture), currentIndex);
  	  sb.setMessage("Current gesture: " + gestureName);
  	  break;
  	  
    case 'e':
    	//go to previous gesture
    	currentGesture = (--currentGesture) % gestures.size();
    	currentIndex = 1;
    	gestureName = String.format("%s_%03d", gestures.get(currentGesture), currentIndex);
  	  sb.setMessage("Current gesture: " + gestureName);
  	  break;
  	  
    case 'g':
  	  //go to next gesture
  	  currentGesture = (++currentGesture) % gestures.size();
  	  currentIndex = 1;
  	  gestureName = String.format("%s_%03d", gestures.get(currentGesture), currentIndex);
  	  sb.setMessage("Current gesture: " + gestureName);
  	  break;
  	  
    case 'i':
  	  //increase index for the current gesture
  	  currentIndex++;
  	  gestureName = String.format("%s_%03d", gestures.get(currentGesture), currentIndex);
  	  sb.setMessage("Current gesture: " + gestureName);
  	  break;
  	  
    case 'l':
  	  //clear recording list and stop recording
  	  recording = false;
  	  listRecording.clear();
  	  sb.setMessage("Recording cleared.");
    	  break;
 
    case 'p':
      showPrior = !showPrior; 
      System.out.println("Showing prior: " + showPrior);
      break;
    
    case 'r':
      renderer.resetCamera();
      break;
    
    case 's':
  	  //save the recording in a file with file chooser
  	  JFileChooser fc = new JFileChooser();
  	  fc.setCurrentDirectory(new File(lastAccessedDir));
		
		  int returnValue = fc.showSaveDialog(frame3);
			
		  if(returnValue == JFileChooser.APPROVE_OPTION){
				File f = fc.getSelectedFile();
				lastAccessedDir = f.getParent();
				try {
					ObjectIO.writeObject(listRecording, f.getPath());
					sb.setMessage("Recording saved.");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		  
		  //clear the recording list for the next record
		  listRecording.clear();
		  break;
  	  
    default:
      if (Character.isDigit(e.getKeyChar())) 
        debugOption = new Integer(e.getKeyChar());
      break;
    }
  }
}