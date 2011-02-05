package edu.mit.yingyin.tabletop.environment;

import handtracking.glove.MarkerModel;
import handtracking.glove.ViewSceneForPrinting;
import handtracking.glove.colordesign.FingerRefinedGlove;
import handtracking.glove.colordesign.MergeTriangleGroupsByColor;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Color3f;

import rywang.scene.Scene;
import rywang.scene.SceneColorData;
import rywang.util.ObjectIO;
import colored.glove.Pattern;

/**
 * Builds a color 
 * 
 * @author rywang
 *
 */
public class LeftGloveMarkerModel implements MarkerModel, Serializable {

  private static final long serialVersionUID = 1L;

  private SceneColorData scd;
  
  private List<Color3f> modelColors;
  
  public static LeftGloveMarkerModel getInstance() {
    if (instance == null) 
      instance = new LeftGloveMarkerModel();
    return instance; 
  }
  
  public static LeftGloveMarkerModel instance = null;
  
  @SuppressWarnings("unchecked")
  private LeftGloveMarkerModel() {
    try {
      LeftGloveTrackingExample trackingExample = LeftGloveTrackingExample.getInstance();
      String prefix = StandardEnvironment.getOSD() + "/data/HandTracking/Calibration/stereocalib/Right";
      modelColors = (List<Color3f>) ObjectIO.readObject(prefix + "/modelcolors.clist");
      
      Scene scene = trackingExample.getSkinningExample().getRestPose();
      FingerRefinedGlove msba = new FingerRefinedGlove(scene, 20);
      
      List<Color3f> assignments = new ArrayList<Color3f>();
      int[] best_assignments = trackingExample.getBestColorAssignments();
      for (int i=0; i<best_assignments.length; i++) {
        assignments.add(i, modelColors.get(best_assignments[i]));
      }
      colorAssignments = assignments;
      assignmentTriangleGroups = msba.getTriangleGroups();
      
      MergeTriangleGroupsByColor merged = new MergeTriangleGroupsByColor(msba
          .getTriangleGroups(), assignments);
      
      scd = new SceneColorData(3 * scene.getNumTriangles());
      List<Color3f> colors = merged.getNewColors();
      List<List<Integer>> triangleGroups = merged.getNewTriangleGroups();
      for (int i=0; i<colors.size(); i++) {
        List<Integer> triangleGroup = triangleGroups.get(i);
        for (int t : triangleGroup) {
          for (int j=0; j<3; j++) {
            scd.setColor(3 * t + j, colors.get(i));
          }
        }
      }
      
      System.out.println("Number of colors: " + merged.getNewColors().size());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @Override
  public List<Color3f> getModelColors() {
    return modelColors;
  }

  @Override
  public SceneColorData getPerTriangleSceneColorData() {
    return scd;
  }

  @Override
  public void viewForPrinting() {
    try {
      Pattern tightMesh = (Pattern) ObjectIO.readObject(LeftGloveData.PREFIX + "/MyLeftHand.tight_pattern");
      LeftGloveData.scalePattern(tightMesh);
      ViewSceneForPrinting.viewSceneForPrinting((int) (512f * 242f / 110f),
          tightMesh.getMesh(), scd);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Color3f getTriangleColor(int triangle) {
    return new Color3f(scd.getColorFragile(triangle * 3));
  }
  
  public int getNumTriangles() { return scd.getNumColors() / 3; }

  private List<Color3f> colorAssignments;
  
  public List<Color3f> getColorAssignments() { return colorAssignments; }
  
  private List<List<Integer>> assignmentTriangleGroups;
  
  public List<List<Integer>> getAssignmentTriangleGroups() { return assignmentTriangleGroups; }
  
  public static void main(String[] args) {
    getInstance().viewForPrinting();
  }
}
