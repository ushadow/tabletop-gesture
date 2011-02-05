package edu.mit.yingyin.tabletop.handtracking;

import handtracking.TrackingExample;
import handtracking.camera.WebcamType;
import handtracking.camera.geometriccalibration.GeometricCalibrationExample;
import handtracking.camera.geometriccalibration.MatlabCameraCalibration;
import handtracking.dblookup.boosting.BoostingExample;
import handtracking.processpipeline.ProcessPacket;

import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import edu.mit.yingyin.tabletop.environment.LeftGloveBoostingExample;
import edu.mit.yingyin.tabletop.environment.LeftGloveColorCalibrationExample;
import edu.mit.yingyin.tabletop.environment.LeftGloveMarkerModel;
import edu.mit.yingyin.tabletop.environment.LeftGloveTrackingExample;
import edu.mit.yingyin.tabletop.environment.SingleCameraCalibrationExample;
import edu.mit.yingyin.tabletop.environment.StandardEnvironment;
import edu.mit.yingyin.tabletop.handtracking.LowLatencyPCUtility.DriverInterface;

import rywang.util.awt.WindowUtils;

public class TrackerDebug {

  private static final WebcamType CAM_TYPE = StandardEnvironment.getCameraType();
  private static final int NUM_THREADS = 1;
  
  private JFrame cameraFrame;
  
  private List<MyProcessor> processors = new ArrayList<MyProcessor>();
  
  private LowLatencyPCUtility pcu = new LowLatencyPCUtility();
  
  private Webcam camera = new Webcam(CAM_TYPE, 320, 240);
  
  private DriverInterface consumer;
  
  public JFrame getCameraFrame() { return cameraFrame; }
  
  public static enum RenderMode { 
    ROBUST,
    BLENDED,
  }
  
  public static class EmptyDriver implements DriverInterface {
    @Override
    public void handleDebugPacket(ProcessPacket state, int frame) { }

    @Override
    public void initialize() { }
  }
  
  public TrackerDebug(DriverInterface consumer) throws IOException {

//    final WebcamNoView camera = new WebcamNoView(CAM_TYPE);
//    final WebcamSimulation camera = new WebcamSimulation(StandardEnvironment
//        .getOSD()
//        + "/data/HandTracking/Videos/Select_TinyTip2/capture_0_%03d.png", 320, 240);
    MatlabCameraCalibration calibration = SingleCameraCalibrationExample
        .getInstance().getGeometricCameraCalibration();
    TrackingExample trackingExample = LeftGloveTrackingExample.getInstance();
    GeometricCalibrationExample geometricCalibration = SingleCameraCalibrationExample.getInstance();
    
    ProcessUtility processUtility = new ProcessUtility(trackingExample,
        geometricCalibration, LeftGloveMarkerModel
            .getInstance(), LeftGloveColorCalibrationExample.getInstance(),
        100000);
    BoostingExample boostingExample = LeftGloveBoostingExample.getInstance();
    processUtility.useBoost(boostingExample);    

    processors.add(new MyProcessor(processUtility));
    
    for (int i = 0; i < NUM_THREADS - 1; i++) {
      ProcessUtility next = new ProcessUtility(trackingExample,
          geometricCalibration, LeftGloveMarkerModel.getInstance(),
          LeftGloveColorCalibrationExample.getInstance(), 100000,
          processUtility);
      processors.add(new MyProcessor(next));
    }

    if (camera.getImageComponent() != null) {
      cameraFrame = WindowUtils.makeWindowWrapComponent(camera.getImageComponent(), "Camera viewer", false);
      cameraFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      cameraFrame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          // Run this on another thread than the AWT event queue
          new Thread(new Runnable() {
            public void run() {
              pcu.setDone();
              if (camera.getDriver() != null)
                camera.getDriver().cleanUp();
              cameraFrame.setVisible(false);
              cameraFrame.dispose();
            }
          }).start();
        }
      });
    }
    
    this.consumer = new DebugDriver(trackingExample.getSkinningExample(),
        calibration, processUtility, processors, cameraFrame, consumer);
    if (consumer instanceof KeyListener && cameraFrame != null)
      cameraFrame.addKeyListener((KeyListener) consumer);

  }
  
  public void setBackground()
  {
	  if (processors != null)
		  for(MyProcessor processor : processors)
			   processor.setBackground();
  }
  
  public void start()
  {
  	if(LeftGloveColorCalibrationExample.getInstance().useBgSubtract())
  		setBackground();
	  pcu.start(camera, processors, consumer);
  }
  
  public static void main(String[] args) {
    try {
      new TrackerDebug(null).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
