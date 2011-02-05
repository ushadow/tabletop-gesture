package edu.mit.yingyin.tabletop.environment;

import handtracking.dblookup.distance.ParamHausdorffFeature;
import handtracking.dblookup.distance.ParametricHausdorff;
import handtracking.dblookup.sampling.MeshPointSampling;
import handtracking.ik.warping.WarpState;

import java.util.Arrays;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rywang.math.FloatMatrixUtils;
import skinning.BasicSkeletonState;
import skinning.SkeletonState;

/**
 * Heuristic for preferring the finger configuration of the last pose when
 * insufficient information is available in this pose.
 * 
 * @author rywang
 * 
 */
public class LeftGloveWarpingHeuristics {
  
  public static final int[] TIP_THRESHOLDS = {4,4,4,8};

  public static final int[][] PATCH_GROUPS = {{1,18},{2,13},{4,10},{0,5,15,12}};
  
  public static final int[][] GROUP_TIPS = {{1},{2},{4},{0,5}};
  
  public static final int[][] JOINT_GROUPS = {//{0}, {1}, // global and wrist
      {2,3,4}, // thumb
      {5,6,7}, // index
      {8,9,10}, // middle 
      {11,12,13,14,15,16}, // ring, pinky
      };
  
  private static int[] patch_to_finger_map = null;
  
  private static boolean VERBOSE = false;
  
  private static void initPatchToFingerMap(int numPatches) {
    patch_to_finger_map = new int[numPatches];
    Arrays.fill(patch_to_finger_map, -1);
    for (int i=0; i<PATCH_GROUPS.length; i++) {
      int[] finger_group = PATCH_GROUPS[i];
      for (int j : finger_group)
        patch_to_finger_map[j] = i;
    }
  }


  private static int[] getPatchCounts(ParamHausdorffFeature feature, ParametricHausdorff hausdorff) {
    int[] patches = new int[hausdorff.getNumPatchIndices()];
    byte[] image = feature.patchIndexImage;
    for (int i=0; i<image.length; i++) {
      patches[image[i]]++;
    }
    return patches;
  }
  
  private static float[] getFingerErrors(SkeletonState a, SkeletonState b, MeshPointSampling mps) {
    float[] pa = mps.getPointPositions(a);
    float[] pb = mps.getPointPositions(b);
    
    Vector3f offset = getOffset(pa, pb);

    float[] errors = new float[JOINT_GROUPS.length];
    for (int i=0; i<JOINT_GROUPS.length; i++) {
      BasicSkeletonState test = new BasicSkeletonState(b);
      for (int joint : JOINT_GROUPS[i]) {
        test.setRotation(joint, a.getRotation(joint));
      }
      float[] ptest = mps.getPointPositions(test);
      errors[i] = diffWithOffsetSquared(pa, ptest, offset);
    }
    
    return errors;
  }
  
  private static float diffWithOffsetSquared(float[] x, float[] y, Vector3f offset) {
    float diff = 0;
    for (int i=0; i<x.length/3; i++) {
      float d;
      d = (x[3 * i + 0] - (y[3 * i + 0] + offset.x));
      diff += d * d;
      d = (x[3 * i + 1] - (y[3 * i + 1] + offset.y));
      diff += d * d;
      d = (x[3 * i + 2] - (y[3 * i + 2] + offset.z));
      diff += d * d;
    }
    
    return diff;
  }
  
  private static Vector3f getOffset(float[] x, float[] y) {
    Point3f xmean = new Point3f(0,0,0);
    Point3f ymean = new Point3f(0,0,0);
    
    for (int i=0; i<x.length/3; i++) {
      xmean.x += x[3 * i + 0];
      xmean.y += x[3 * i + 1];
      xmean.z += x[3 * i + 2];
      
      ymean.x += y[3 * i + 0];
      ymean.y += y[3 * i + 1];
      ymean.z += y[3 * i + 2];
    }
    
    xmean.sub(ymean);
    xmean.scale(1.0f / (x.length / 3));
    return new Vector3f(xmean);
  }
  
  private static float[] getTotalFingerErrorsSquared(float[] perPatchErrors, int[] perPatchCounts) {
    if (patch_to_finger_map == null) { 
      initPatchToFingerMap(perPatchCounts.length);
    }
    
    float[] finger_errors = new float[PATCH_GROUPS.length];
    int[] finger_counts = new int[PATCH_GROUPS.length];
    for (int i=0; i<perPatchErrors.length; i++) {
      if (patch_to_finger_map[i] == -1) continue;
      finger_counts[patch_to_finger_map[i]] += perPatchCounts[i];
      finger_errors[patch_to_finger_map[i]] += perPatchErrors[i]*perPatchErrors[i] * perPatchCounts[i];
    }
    return finger_errors;
  }


  public static SkeletonState warpMinError(WarpState debugState,
      ParametricHausdorff hausdorff, MeshPointSampling mps) {
    int historyIndex = debugState.associatedPoses.size() - 2;
    int blendedIndex = debugState.associatedPoses.size() - 1;
    
    BasicSkeletonState history = new BasicSkeletonState(debugState.associatedPoses.get(historyIndex));
    BasicSkeletonState warped = new BasicSkeletonState(debugState.associatedPoses.get(blendedIndex));
    
    float[] historyTotalFingerError = getTotalFingerErrorsSquared(debugState.perPatchErrors.get(historyIndex),
            debugState.perPatchCounts.get(historyIndex));
    
    float[] blendedTotalFingerError = getTotalFingerErrorsSquared(debugState.perPatchErrors.get(blendedIndex),
            debugState.perPatchCounts.get(blendedIndex));
    
    int[] currentPatchCounts = getPatchCounts(debugState.classified, hausdorff);
    
    float[][] totalFingerErrorsSquared3d = new float[debugState.perPatchErrors.size()][];
    float[][] totalFingerErrorsSquared2D = new float[debugState.perPatchErrors.size()][];
    for (int j = 0; j < debugState.perPatchErrors.size(); j++) {
      totalFingerErrorsSquared2D[j] = getTotalFingerErrorsSquared(debugState.perPatchErrors.get(j),
              debugState.perPatchCounts.get(j));
      totalFingerErrorsSquared3d[j] = getFingerErrors(history, debugState.associatedPoses.get(j), mps);
    }
    
    for (int i=0; i<GROUP_TIPS.length; i++) {
      int[] patchGroup = GROUP_TIPS[i];
      int totalCount = 0;
      for (int patch : patchGroup) {
        totalCount += currentPatchCounts[patch];
      }
      
      // always warp thumb
      if (i == 0 || totalCount < TIP_THRESHOLDS[i]) {
        float min = Float.MAX_VALUE;
        int minIndex = 0;
        for (int j = 0; j < debugState.perPatchErrors.size(); j++) {
          float composite = totalFingerErrorsSquared2D[j][i] + totalFingerErrorsSquared3d[j][i] / 40;
          
          if (VERBOSE)
            System.out.println("  " + i + " state: " + j
                + " imate: " + totalFingerErrorsSquared2D[j][i] + " 3d: " + totalFingerErrorsSquared3d[j][i] + " comp: " + composite);
          
          if (composite < min) {
            min = composite;
            minIndex = j;
          }
        }
        
        int[] jointGroup = JOINT_GROUPS[i];
        for (int joint : jointGroup) {
          warped.setRotation(joint, debugState.associatedPoses.get(minIndex).getRotation(joint));
        }
        if (VERBOSE)
          System.out.println("  " + i + ": " + totalCount + " best: " + min + "(" + minIndex + ") b: " + blendedTotalFingerError[i] + " h: " + historyTotalFingerError[i]);
      }
    }
    
    return warped;
  }
  
  private static float[] getAvgErrorsOneWay(ParametricHausdorff hausdorff, byte[] patchIndexImage, byte[] minPatchImage) {
    float[] diff = new float[hausdorff.getNumPatchIndices()];
    int[] count = new int[hausdorff.getNumPatchIndices()];
    float scale = hausdorff.getMaxDistance() / (float) Byte.MAX_VALUE;
    scale = scale * scale;
    int numPatchIndices = hausdorff.getNumPatchIndices();
    byte blackPatchIndex = hausdorff.getBlackPatchIndex();
    for (int i=0; i<patchIndexImage.length; i++)
      if (patchIndexImage[i] != blackPatchIndex) {
        int d = minPatchImage[i * numPatchIndices + patchIndexImage[i]];
        diff[patchIndexImage[i]] += d * d;
        count[patchIndexImage[i]]++;
      }
    
//    for (int i=0; i<diff.length; i++) {
//      if (count[i] > 0)
//        diff[i] = (float) Math.sqrt(diff[i] * scale / count[i]);
//    }
    
    for (int i=0; i<diff.length; i++) {
      diff[i] = diff[i] * scale;
    }
    
    return diff;
  }
  
  public static float[] getAvgErrorsCounted(ParametricHausdorff hausdorff, ParamHausdorffFeature f1, ParamHausdorffFeature f2) {
    float[] f1tof2 = getAvgErrorsOneWay(hausdorff, f1.patchIndexImage, f2.minPatchDistance);
    float[] f2tof1 = getAvgErrorsOneWay(hausdorff, f2.patchIndexImage, f1.minPatchDistance);
    
    int[] count12 = getCountOneWay(hausdorff, f1.patchIndexImage, f2.minPatchDistance);
    int[] count21 = getCountOneWay(hausdorff, f2.patchIndexImage, f1.minPatchDistance);
    
    int[] count = new int[count12.length];
    for (int i=0; i<count.length; i++)
      count[i] = count12[i] + count21[i];
    
    FloatMatrixUtils.add(f1tof2, f2tof1, f1tof2);
    for (int i=0; i<f1tof2.length; i++) {
      if (count[i] != 0) {
        f1tof2[i] = (float) Math.sqrt(f1tof2[i] / count[i]);
      } else 
        f1tof2[i] = (float) Math.sqrt(f1tof2[i]);
    }
    return f1tof2;
  }
  
  public static int[] getPerPatchCounts(ParametricHausdorff hausdorff, ParamHausdorffFeature f1, ParamHausdorffFeature f2) {
    int[] count12 = getCountOneWay(hausdorff, f1.patchIndexImage, f2.minPatchDistance);
    int[] count21 = getCountOneWay(hausdorff, f2.patchIndexImage, f1.minPatchDistance);
    
    int[] count = new int[count12.length];
    for (int i=0; i<count.length; i++)
      count[i] = count12[i] + count21[i];

    return count;
  }
  
  private static int[] getCountOneWay(ParametricHausdorff hausdorff, byte[] patchIndexImage, byte[] minPatchImage) {
    int[] count = new int[hausdorff.getNumPatchIndices()];
    byte blackPatchIndex = hausdorff.getBlackPatchIndex();
    for (int i=0; i<patchIndexImage.length; i++)
      if (patchIndexImage[i] != blackPatchIndex)
        count[patchIndexImage[i]]++;
    return count;
  }
}
