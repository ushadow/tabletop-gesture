package edu.mit.yingyin.tabletop.environment;

import handtracking.camera.geometriccalibration.GeometricCalibrationExample;

/**
 * SingleCameraCalibrationExample provides environment variables for camera calibration data. The 
 * calibration files needed are "Intrinsic.mat", "Extrinsic.mat", and "Points.mat".
 * @author yingyin
 *
 */
public class SingleCameraCalibrationExample extends GeometricCalibrationExample {
 
  public static SingleCameraCalibrationExample getInstance() { return new 
    SingleCameraCalibrationExample(); }
  
  public String getPrefix() {
    return StandardEnvironment.getOSD() + "/data/HandTracking/YingColorCalibration/data/HandTracking/CameraCalib/7_25";
  }
}
