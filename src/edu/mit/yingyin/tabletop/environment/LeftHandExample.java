package edu.mit.yingyin.tabletop.environment;

import java.io.IOException;
import java.util.Arrays;

import poser.PoserExample;
import poser.PoserSkinAndSkeletonSequence;
import skinning.Skeleton;
import skinning.SkeletonPruner;
import skinning.SkeletonStateSequence;
import skinning.SkinAndSkeletonSequence;

public class LeftHandExample extends PoserExample {

  private static String OS_DEPENDENT_PREFIX = null;
  
  private static String[] UNWANTED_JOINTS = {"WORLD_ROOT", "faceRoomActor", "faceRoomTexActor", "BODY:1", };
  
  static {
    OS_DEPENDENT_PREFIX = StandardEnvironment.getOSD();
    pruner = new SkeletonPruner(Arrays.asList(UNWANTED_JOINTS));
  }
  
  private static SkeletonPruner pruner;
  
  @Override
  public final String getPrefix() { 
    return OS_DEPENDENT_PREFIX + "/data/HandTracking/LeftHand/lefthand"; 
  }  
  
  public static LeftHandExample getInstance() {
    return new LeftHandExample();
  }

  @Override
  public SkinAndSkeletonSequence buildSequenceFromFile() throws IOException {
    if (pruner == null)
      pruner = new SkeletonPruner(super.getSkeletonStateSequence());

    SkinAndSkeletonSequence sFiltered = pruner
        .filterSASS(new PoserSkinAndSkeletonSequence(getBVHFile(), getPrefix(),
            1));
    return sFiltered;
  }

  @Override
  public SkeletonStateSequence getSkeletonStateSequence() throws IOException {
    if (pruner == null)
      pruner = new SkeletonPruner(super.getSkeletonStateSequence());
    
    SkeletonStateSequence sFiltered = pruner.filterSSS(super
        .getSkeletonStateSequence(), super.getSkeleton());
    return sFiltered;
  }

  @Override
  public Skeleton getSkeleton() throws IOException {
    if (pruner == null)
      pruner = new SkeletonPruner(super.getSkeletonStateSequence());
    return pruner.filterSkeleton(super.getSkeleton());
  }
  
  public static void main(String[] args) {
    try {
      getInstance().viewTruthWithSkeletons();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}