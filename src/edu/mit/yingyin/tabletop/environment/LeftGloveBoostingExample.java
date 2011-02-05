package edu.mit.yingyin.tabletop.environment;

import handtracking.dblookup.boosting.BoostingExample;

import java.util.Arrays;
import java.util.List;

public class LeftGloveBoostingExample extends BoostingExample {

  @Override
  public List<String> getImageFormats() {
    String OSD = StandardEnvironment.getOSD();
    return Arrays
        .asList(
            OSD
                + "/data/HandTracking/NormalizedTinyTip1/capture_0_%03d_normalized.png",
            OSD
                + "/data/HandTracking/NormalizedTinyTip2/capture_0_%03d_normalized.png",
            OSD
                + "/data/HandTracking/NormalizedTinyTip3/capture_0_%03d_normalized.png",
            OSD
                + "/data/HandTracking/NormalizedTinyTip4/capture_0_%03d_normalized.png");
  }

  public static BoostingExample getInstance() {
    return new LeftGloveBoostingExample();
  }

}
