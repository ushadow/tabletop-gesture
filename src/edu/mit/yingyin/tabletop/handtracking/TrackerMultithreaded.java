package edu.mit.yingyin.tabletop.handtracking;

import handtracking.TrackingExample;
import handtracking.camera.WebcamType;
import handtracking.camera.geometriccalibration.GeometricCalibrationExample;
import handtracking.dblookup.boosting.BoostingExample;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.mit.yingyin.tabletop.environment.LeftGloveBoostingExample;
import edu.mit.yingyin.tabletop.environment.LeftGloveColorCalibrationExample;
import edu.mit.yingyin.tabletop.environment.LeftGloveMarkerModel;
import edu.mit.yingyin.tabletop.environment.LeftGloveTrackingExample;
import edu.mit.yingyin.tabletop.environment.RecognizerExample;
import edu.mit.yingyin.tabletop.environment.SingleCameraCalibrationExample;
import edu.mit.yingyin.tabletop.environment.StandardEnvironment;
import edu.mit.yingyin.tabletop.handtracking.MyProcessor;
import edu.mit.yingyin.tabletop.recognition.GestureEventListener;
import edu.mit.yingyin.tabletop.recognition.GestureRecognizer;
import edu.mit.yingyin.tabletop.recognition.HMMRecognizer;

public class TrackerMultithreaded implements TrackerInterface {

  private static final WebcamType CAM_TYPE = StandardEnvironment.getCameraType();
  private static final int NUM_THREADS = 1;
  
  private final LowLatencyPCUtility pcu = new LowLatencyPCUtility();
  private final Webcam camera;
  private List<MyProcessor> processors;
  private GestureDriver consumer;
  
  public static enum RenderMode { 
    ROBUST,
    BLENDED,
  }
  
  public TrackerMultithreaded() throws IOException {
    camera = new Webcam(CAM_TYPE, 320, 240);
    TrackingExample trackingExample = LeftGloveTrackingExample.getInstance();
    GeometricCalibrationExample geometricCalibration = SingleCameraCalibrationExample.getInstance();
    
    ProcessUtility processUtility = new ProcessUtility(trackingExample,
        geometricCalibration, LeftGloveMarkerModel.getInstance(), 
        LeftGloveColorCalibrationExample.getInstance(), 100000);
    BoostingExample boostingExample = LeftGloveBoostingExample.getInstance();
    processUtility.useBoost(boostingExample);    

    processors = new ArrayList<MyProcessor>();
    processors.add(new MyProcessor(processUtility));
    for (int i = 0; i < NUM_THREADS - 1; i++) {
      ProcessUtility next = new ProcessUtility(trackingExample,
          geometricCalibration, LeftGloveMarkerModel.getInstance(),
          LeftGloveColorCalibrationExample.getInstance(), 100000,
          processUtility);
      processors.add(new MyProcessor(next));
    }
    
    GestureRecognizer gr = new HMMRecognizer(RecognizerExample.getHmmParamsFileName());
    consumer = new GestureDriver(processUtility, gr);
    consumer.addWindowListener(new WindowAdapter() {
    	 public void windowClosing(WindowEvent e) {
         // Run this on another thread than the AWT event queue
         new Thread(new Runnable() {
           public void run() {
             pcu.setDone();
             if (camera.getDriver() != null)
               camera.getDriver().cleanUp();
           }
         }).start();
    	 }
    });
  }
  
  /**
   * clean up before quitting the application
   */
  public void stop() {
	  pcu.setDone();
    if (camera.getDriver() != null)
      camera.getDriver().cleanUp();
  }

  public void setBackground() {
	  if (processors != null)
		  for(MyProcessor processor : processors)
			   processor.setBackground();
  }
  
  public void addGestureEventListener(GestureEventListener gel) {
	  if(consumer != null)
		  consumer.addGestureEventListener(gel);
  }
  
  public void start() {
	  setBackground();
	  pcu.start(camera, processors, consumer);
  }
  
  public static void main(String[] args) {
  	try {
			new TrackerMultithreaded().start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
}
