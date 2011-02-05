package edu.mit.yingyin.tabletop.handtracking;

import handtracking.processpipeline.ProcessPacket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.tabletop.handtracking.MyProcessor;
import edu.mit.yingyin.tabletop.handtracking.LowLatencyPCUtility.DriverInterface;
import edu.mit.yingyin.tabletop.handtracking.TrackerDebug.RenderMode;
import edu.mit.yingyin.tabletop.recognition.GestureEvent;
import edu.mit.yingyin.tabletop.recognition.GestureEventListener;
import edu.mit.yingyin.tabletop.recognition.GestureRecognizer;

import rywang.image.FilterImage.ImageComponent;
import rywang.util.ObjectIO;
import yingyin.gui.WindowUtils;
import skinning.SkeletonState;

public class GestureDriver extends KeyAdapter implements DriverInterface {

	private final static boolean DEBUG = EnvConstants.TINYIMAGE_DEBUG;
	private final static boolean PRINT = EnvConstants.GESTUREDRIVER_PRINT;
	
  private ImageComponent normalizedComponent;
  private AffineTransform downScale;
  private ProcessUtility pu;
  private Graphics2D bufferGraphics;
  private JFrame frame;
  private List<MyProcessor> processors;
  private List<SkeletonState> listRecording;
  private String lastAccessedDir = EnvConstants.RECORDING_DIR;
  private GestureRecognizer gr;
  private Controller controller;
  private boolean recording = false;
  
  public GestureDriver(ProcessUtility pu, GestureRecognizer gr) {
  	this.gr = gr;

  	if (DEBUG) {
	  	this.pu = pu;
	    listRecording = new ArrayList<SkeletonState>();
	    BufferedImage buffer = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
	    bufferGraphics = buffer.createGraphics();
	    normalizedComponent = new ImageComponent(buffer);
	    
	    try {
	    	SwingUtilities.invokeAndWait(new Runnable() {
					public void run()	{
						frame = WindowUtils.makeWindowWrapComponent(normalizedComponent, "N", false);
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						frame.setLocation(0, 0);
						controller = new Controller();
						frame.addKeyListener(controller);
					}
	    	});
	    } catch (InterruptedException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    } catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }
	    downScale = new AffineTransform();
  	}
  }
  
  public void addWindowListener(WindowListener wl) {
  	if (DEBUG)
  		frame.addWindowListener(wl);
  }
    
  @Override
  public void handleDebugPacket(ProcessPacket dp, int frame) {
  	GestureEvent ge = gr.classify(dp.prior);
  	
  	if (PRINT && ge != null)
  		System.out.println(ge.toString());
    
  	if (DEBUG) {
	    BufferedImage tinyImage = null;
	    if (!showEstimate) {
	      if (dp.patchClassifiedFeature != null) {
	      	//System.out.println(this.getClass() + ":" + "Displaying patchClassifiedFeature");
	        tinyImage = pu.getDatabase().getMetric().viewFeature(dp.patchClassifiedFeature);
	      } else {
	      	//System.out.println(this.getClass() + ":" + "Displaying imageFeature");
	        tinyImage = pu.getDatabase().getMetric().viewFeature(dp.imageFeature);
	      }
	    } else {
	      tinyImage = pu.getDatabase().getMetric().viewFeature(dp.historyFeature);
	    }
    
	    downScale.setToIdentity();
	    downScale.scale(0.5f, 0.5f);
	    downScale.concatenate(dp.normalizedToOriginal);
	    bufferGraphics.setColor(Color.black);
	    bufferGraphics.fillRect(0, 0, 320, 240);
	    bufferGraphics.drawImage(tinyImage, downScale, null);
	    
	    AffineTransform T = bufferGraphics.getTransform();
	    bufferGraphics.transform(downScale);
	    bufferGraphics.setColor(Color.red);
	    bufferGraphics.drawRect(0, 0, 40, 40);
	    bufferGraphics.setTransform(T);
    
	    SwingUtilities.invokeLater(new Runnable() {
	  	  public void run() {
	  		  normalizedComponent.repaint();
	  	  }
	    });
  	}
  }

  @Override
  public void initialize() {
  }
  
  public void addGestureEventListener(GestureEventListener gel) {
  	gr.addGestureEventListener(gel);
  }
  
  private boolean showEstimate = false;

  private class Controller extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
      switch(e.getKeyChar()) {
      case 'c':
    	  //toggle recording
    	  recording = !recording;
    	  if(recording == true)
    		  listRecording.clear();
    	  
    	  break;
      case 's':
    	  JFileChooser fc = new JFileChooser();
			
    	  fc.setCurrentDirectory(new File(lastAccessedDir));
			
			  int returnValue = fc.showSaveDialog(frame);
				
			  if(returnValue == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					lastAccessedDir = f.getParent();
					try {
						ObjectIO.writeObject(listRecording, f.getPath());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			  
			  //clear the recording list for the next record
			  listRecording.clear();
			  break;
			
      case 'l':
    	  //clear recording list and stop recording
    	  recording = false;
    	  listRecording.clear();
    	  break;
    	  
      default:
	      if (Character.isDigit(e.getKeyChar())) {
	        for (MyProcessor processor : processors) {
	          switch (e.getKeyChar()) {
	          case '1':
	            processor.renderMode = RenderMode.BLENDED;
	            frame.setTitle("Blended");
	            break;
	          case '2':
	          default:
	            processor.renderMode = RenderMode.ROBUST;
	            frame.setTitle("Robust");
	            break;
	          }
	        }
	      }
      }
    }
  }
}