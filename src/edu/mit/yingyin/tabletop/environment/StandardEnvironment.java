package edu.mit.yingyin.tabletop.environment;

import handtracking.camera.WebcamType;

public class StandardEnvironment {

  private static StandardEnvironment instance = null;
  
  private StandardEnvironment() {
    OS_DEPENDENT_PREFIX = null;
    if (System.getProperty("os.name").equals("Windows XP")) {
      OS_DEPENDENT_PREFIX = "G:";
      hasMKL = true;
      hasNativePreprocess = true;
    }
    else if (System.getProperty("os.name").equals("Mac OS X")) {
      OS_DEPENDENT_PREFIX = "/afs/csail.mit.edu/group/graphics/data/fast_skinning";
      hasMKL = false;
      hasNativePreprocess = false;
    }
    else {
      OS_DEPENDENT_PREFIX = "/afs/csail.mit.edu/group/graphics/data/fast_skinning";
      hasMKL = false;
      hasNativePreprocess = true;
    }
  }
  
  public static StandardEnvironment getInstance() {
    if (instance == null) instance = new StandardEnvironment();
    return instance;
  }
  
  private String OS_DEPENDENT_PREFIX;
  
  private boolean hasMKL;
  
  private boolean hasNativePreprocess;
  
  public static String getOSD() { return getInstance().OS_DEPENDENT_PREFIX; }
  
  public boolean hasMKL() { return hasMKL; }
  
  public boolean hasNativePreprocess() { return hasNativePreprocess; }
 
  /**
   * Change CAM_TYPE for different webcam
   */
  public static WebcamType getCameraType() { return WebcamType.JMF; }
}
