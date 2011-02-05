package edu.mit.yingyin.tabletop.environment;

import handtracking.colorclassification.ColorCalibrationExample;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import rywang.util.DirectoryIO;
import colored.lowfrequency.LowFrequencyColorCalibrate;

public class LeftGloveColorCalibrationExample extends ColorCalibrationExample {
  
  private static String OSD = StandardEnvironment.getOSD();

  public static LeftGloveColorCalibrationExample getInstance() {
    return new LeftGloveColorCalibrationExample();
  }
  
  private LeftGloveColorCalibrationExample() { }
  
  public String getPrefix() {
    return OSD + "/data/HandTracking/Satellite_LF/02_12";
  }
  
  public List<String> getColorCalibrationImages() {
    return DirectoryIO.getNumberedFiles(getPrefix() + "/image", ".png", 1);
  }

  public void colorCalibrateImage(String imageFile) {
    try {
      String imageName = new File(imageFile).getName().replaceAll(".png", "");
      String pointsFile = getPrefix() + "/" + imageName + ".pts";
      String challengeFile = getPrefix() + "/" + imageName + ".colorchallenge";

      BufferedImage image = ImageIO.read(new File(imageFile));
      if (image == null) {
        throw new IOException("Could not read image: " + imageFile);
      }

      new LowFrequencyColorCalibrate(image, LeftGloveMarkerModel.getInstance(),
          LeftGloveTrackingExample.getInstance().getSkinningExample()
              .getRestPose(), challengeFile, pointsFile).start();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }
  
  public String getColorTransformationMethod()
  {
  	return "Polyfit";
  }
  
  public boolean useBgSubtract()
  {
  	return true;
  }
  
  public static void main(String[] args) {
    LeftGloveColorCalibrationExample.getInstance().colorCalibrateAllImages();
  }
}
