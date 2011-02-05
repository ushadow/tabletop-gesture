package edu.mit.yingyin.tabletop.handtracking;

import handtracking.processpipeline.ProcessPacket;

import java.awt.image.BufferedImage;

import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.tabletop.handtracking.LowLatencyPCUtility.ProcessingInterface;
import edu.mit.yingyin.tabletop.handtracking.TrackerDebug.RenderMode;

import rywang.util.ThreadTimer;
import rywang.util.Timer;

public class MyProcessor implements ProcessingInterface {

	private static final boolean DEBUG = EnvConstants.BROWSER_DEBUG;
	
  private ProcessUtility bfpu;
  
  public RenderMode renderMode;
  
  public MyProcessor(ProcessUtility bfpu) {
    this.bfpu = bfpu;
    renderMode = RenderMode.BLENDED;
  }
  
  @Override
  public void initialize() {
  }

  @Override
  public ProcessPacket process(BufferedImage image, int index) {
  	ThreadTimer threadTimer;
  	
  	if (DEBUG)
    	threadTimer = new ThreadTimer();
    
  	ProcessPacket packet = null;
    switch (renderMode) {
    case BLENDED:
      packet = bfpu.processJustBlended(image, false);
      break;
    case ROBUST:
    default:
      packet = bfpu.processRobust(image, false);
      break;
    }
    if (DEBUG)
    	threadTimer.getTocln("### G : T Processed image " + index + " " + Thread.currentThread().getName());
    return packet;
  }
  
  public void setBackground() {
  	bfpu.setBackground();
  }
}
