package edu.mit.yingyin.tabletop.environment;

import handtracking.TrackingExample;
import handtracking.glove.MarkerModel;
import handtracking.glove.wrapping.TriangleCorrespondenceSolverTest;
import handtracking.glove.wrapping.nodemesh.AlternativePatternNodeMesh;
import handtracking.glove.wrapping.nodemesh.NodeMesh;

import java.io.File;
import java.io.IOException;

import examples.CorrespondedSkinningExample;

import rywang.scene.BasicScene;
import rywang.scene.Scene;
import rywang.scene.SceneUtils;
import rywang.util.ObjectIO;
import skinning.BasicSkeletonStateSequence;
import skinning.SkeletonStateSequence;
import skinning.examples.SkinningExample;

public class LeftGloveTrackingExample extends TrackingExample {
  
  public static String OS_DEPENDENT_PREFIX = StandardEnvironment.getOSD();
  
  public static LeftGloveTrackingExample getInstance() {
    return new LeftGloveTrackingExample();
  }
  
  public String getBestColorAssignmentsFile(){
    return getPrefix() + ".best_color_assignments";
  }
  
  public int[] getBestColorAssignments() {
    try {
      return (int[]) ObjectIO.readObject(getBestColorAssignmentsFile());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @Override
  public SkeletonStateSequence getConfigurationLibrary() {
    try {
      BasicSkeletonStateSequence bsss = (BasicSkeletonStateSequence) ObjectIO
          .readObject(OS_DEPENDENT_PREFIX
              + "/data/HandTracking/Cyberglove/captureCombined.skelState");
      return bsss;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public SkinningExample getSkinningExample() {
    final String prefix = getPrefix() + "CSE";
    return new CorrespondedSkinningExample(){
    
      @Override
      public String getPrefix() {
        return prefix;
      }

      @Override
      public SkinningExample getTargetSkinningExample() {
        return LeftHandExample.getInstance();
      }
    
      @Override
      public Scene getSourceMesh() {
        try {
          return getCorrespondedMesh();
        } catch (IOException e) {
          e.printStackTrace();
          throw new RuntimeException(e.getMessage());
        }
      }
    
      @Override
      public float getDistanceBetweenPoints() {
        return LeftGloveData.Y_SCALE;
      }
      
      public int getMaxNumJointInfluences() { return 2; }
    };
  }

  public Scene getPatternMesh() throws IOException {
    return LeftGloveData.readPattern().getMesh();
  }
  
  private String getCorrespondedMeshFile() {
    return getPrefix() + ".cmap.all";
  }
  
  public Scene getCorrespondedMesh() throws IOException {
    if (!new File(getCorrespondedMeshFile()).exists()) {
      String outputFile = getPrefix() + ".cmap";
      TriangleCorrespondenceSolverTest.buildBestFitPose(
          LeftGloveData.readPattern(), LeftHandExample.getInstance()
              .getRestPose(), getMarkerModel().getPerTriangleSceneColorData(), outputFile); 
    } 
    return (Scene) ObjectIO.readObject(getCorrespondedMeshFile());
  }

  @Override
  public String getPrefix() {
    return OS_DEPENDENT_PREFIX + "data/HandTracking/YingColorCalibration/data/HandTracking/MyLeftHandPro/MyLeftHand";
  }
  
  public Scene getMarkerModelMesh() throws IOException {
    NodeMesh sourcePose = AlternativePatternNodeMesh
        .createNodeMeshFromPattern(LeftGloveData.readPattern());
    Scene scene = BasicScene.createSceneFromPoints(sourcePose
        .getVerticesFragile(), sourcePose.getTrianglesFragile().toArray(
        new int[0][]));
    return scene;
  }
  
  public void viewSeamedMesh() throws IOException {
    SceneUtils.quickPreviewScene(getMarkerModelMesh());
  }
  
  public void viewCorrespondedMesh() throws IOException {
    SceneUtils.quickPreviewScene(getCorrespondedMesh());
  }
  
  public void viewCorrespondedSkinning() throws IOException {
    SkinningExample example = getSkinningExample();
    example.viewTruthWithSkeletons();
  }

  @Override
  public MarkerModel getMarkerModel() {
    return LeftGloveMarkerModel.getInstance();
  }
  
  public static void main(String[] args) {
    try {
      LeftGloveTrackingExample.getInstance().viewCorrespondedSkinning();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public int[] getDOFs() {
    return new int[] { 6, 3, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1 };
  }
}
