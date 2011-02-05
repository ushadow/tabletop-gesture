package edu.mit.yingyin.tabletop.handtracking;

import handtracking.TrackingExample;
import handtracking.camera.CustomCameraRenderer;
import handtracking.camera.geometriccalibration.GeometricCalibrationExample;
import handtracking.camera.geometriccalibration.MatlabCameraCalibration;
import handtracking.colorclassification.ColorCalibrationExample;
import handtracking.colorclassification.cleanup.MAPPixelClassifier;
import handtracking.colorclassification.cleanup.PatchConnectedComponentFilter;
import handtracking.dblookup.DatabaseEntryRenderer;
import handtracking.dblookup.DatabaseLookup;
import handtracking.dblookup.TinyImageDatabase;
import handtracking.dblookup.TinyImageResult;
import handtracking.dblookup.boosting.BoostedDatabase;
import handtracking.dblookup.boosting.BoostingExample;
import handtracking.dblookup.distance.ParamHausdorffFeature;
import handtracking.dblookup.distance.ParametricHausdorff;
import handtracking.dblookup.sampling.ConfigurationSampling;
import handtracking.experimental.DirtyWarping;
import handtracking.glove.MarkerModel;
import handtracking.ik.QuaternionBlendTest;
import handtracking.ik.constraints.DenseConstraintsUtility3;
import handtracking.ik.constraints.FastCorrespondenceGenerator;
import handtracking.ik.constraints.VarianceConstraints;
import handtracking.ik.constraints.DenseConstraintsUtility3.ImageConstraint;
import handtracking.ik.init.CorrespondenceUtility;
import handtracking.ik.init.CorrespondenceUtility.Correspondence;
import handtracking.ik.kalman.HandDKFFilter;
import handtracking.ik.kalman.SharedPoseStore;
import handtracking.ik.kalman.SharedPoseStore.HistoryPacket;
import handtracking.ik.warping.WarpState;
import handtracking.processpipeline.DatabaseConstraintHelper;
import handtracking.processpipeline.ProcessPacket;
import handtracking.processpipeline.SSDPointRasterizer;
import handtracking.regressions.ProcessRegression;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.vecmath.Point2f;

import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.tabletop.environment.LeftGloveColorCalibrationExample;
import edu.mit.yingyin.tabletop.environment.LeftGloveMarkerModel;
import edu.mit.yingyin.tabletop.environment.LeftGloveTrackingExample;
import edu.mit.yingyin.tabletop.environment.LeftGloveWarpingHeuristics;
import edu.mit.yingyin.tabletop.environment.SingleCameraCalibrationExample;
import edu.mit.yingyin.tabletop.environment.StandardEnvironment;

import rywang.image.FilterImage;
import rywang.math.FloatMatrixUtils;
import rywang.util.DirectoryIO;
import rywang.util.Pair;
import rywang.util.Timer;
import skinning.BasicSkeletonState;
import skinning.SkeletonState;
import skinning.examples.SkinningExample;
import yingyin.preprocess.PreProcess;

public class ProcessUtility {

  public static final boolean DEBUG = EnvConstants.BROWSER_DEBUG;
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 480;
	public static final int IMAGE_SCALE = 2;
	
  public static final int N_GUESSES = 10;
  public static final int BOOSTING_BITS = 192;
  
  public static final float KNN_BLEND_SIGMA = 0.75f;
  public static final float HISTORY_3D_DISTANCE_THRESHOLD = 200; 
  public static final float HISTORY_IMAGE_THRESHOLD = 6;
  public static final float HISTORY_PREFERENCE = 0.75f;
  
  public boolean printTiming = false;

  public TinyImageDatabase<ParamHausdorffFeature> getDatabase() { return database; }
  public DatabaseLookup<ParamHausdorffFeature> getLookup() { return lookup; }
  public SharedPoseStore getHistoryStore() { return historyStore; }

  private PreProcess nativeDecoding;
  private DatabaseLookup<ParamHausdorffFeature> lookup;
  private TinyImageDatabase<ParamHausdorffFeature> database;
  private ParametricHausdorff hausdorff;
  private CorrespondenceUtility correspondenceUtility;
  private SSDPointRasterizer rasterizer;
  private MAPPixelClassifier pixelClassifier;
  private HandDKFFilter kalmanFilter;
  private MatlabCameraCalibration calibration;
  private SharedPoseStore historyStore;
  private BoostedDatabase<ParamHausdorffFeature> boostedDatabase;
  private DirtyWarping dirtyWarping;
  
  public ProcessUtility(TrackingExample trackingExample, 
      GeometricCalibrationExample geometricCalibration,
      MarkerModel markerModel,
      ColorCalibrationExample colorCalibration, int numEntries) {

    calibration = geometricCalibration.getGeometricCameraCalibration();
    nativeDecoding = new PreProcess(colorCalibration.getPrefix(), 
        colorCalibration.getColorTransformationMethod(),
    		IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_SCALE, colorCalibration.useBgSubtract(), DEBUG);
    
    hausdorff = new ParametricHausdorff(markerModel);
    database = new TinyImageDatabase<ParamHausdorffFeature>(trackingExample,
        geometricCalibration, hausdorff, numEntries);
    lookup = database;
    hausdorff.primeCache(database);
    correspondenceUtility = new CorrespondenceUtility(trackingExample, geometricCalibration.getGeometricCameraCalibration());
    rasterizer = trackingExample.getSSDPointRasterizer();
    rasterizer.setCameraData(geometricCalibration.getCameraData());
    pixelClassifier = new MAPPixelClassifier(hausdorff, colorCalibration);
    kalmanFilter = new HandDKFFilter(trackingExample);
    historyStore = new SharedPoseStore();
    dirtyWarping = new DirtyWarping(hausdorff, trackingExample
        .getSkinningExample(), trackingExample.getConfigurationLibrary(),
        LeftGloveWarpingHeuristics.PATCH_GROUPS,
        LeftGloveWarpingHeuristics.JOINT_GROUPS, geometricCalibration
            .getCameraData());
  }
  
  public ProcessUtility(TrackingExample trackingExample, 
      GeometricCalibrationExample geometricCalibration,
      MarkerModel markerModel,
      ColorCalibrationExample colorCalibration,
      int numEntries, ProcessUtility copy) {

    calibration = geometricCalibration.getGeometricCameraCalibration();
    nativeDecoding = new PreProcess(colorCalibration.getPrefix(), 
        colorCalibration.getColorTransformationMethod(),
    		IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_SCALE, colorCalibration.useBgSubtract(), DEBUG);
    hausdorff = new ParametricHausdorff(markerModel);
    database = new TinyImageDatabase<ParamHausdorffFeature>(hausdorff, copy.database);
    hausdorff.copyCache(copy.hausdorff);
    lookup = database;
    correspondenceUtility = new CorrespondenceUtility(trackingExample, geometricCalibration.getGeometricCameraCalibration());
    rasterizer = trackingExample.getSSDPointRasterizer();
    rasterizer.setCameraData(geometricCalibration.getCameraData());
    pixelClassifier = new MAPPixelClassifier(hausdorff, colorCalibration);
    kalmanFilter = new HandDKFFilter(trackingExample);
    historyStore = copy.historyStore;
    
    if (copy.boostedDatabase != null) {
      lookup = boostedDatabase = new BoostedDatabase<ParamHausdorffFeature>(database, copy.boostedDatabase);
    }
  }
  
  public void useBoost(BoostingExample boostingExample) {
    try {
      lookup = boostedDatabase = new BoostedDatabase<ParamHausdorffFeature>(database,
          boostingExample.getImageFormats(), BOOSTING_BITS);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }
  
  public ProcessPacket processImage(BufferedImage image) {
    ProcessPacket packet = new ProcessPacket();
    packet.normalizedImage = new BufferedImage(40, 40,
        BufferedImage.TYPE_INT_ARGB);
    packet.debugImage = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
    packet.normalizedToOriginal = new AffineTransform();
    nativeDecoding.filter(image, packet.normalizedImage,
        packet.normalizedToOriginal);
    packet.imageFeature = hausdorff.generateFeatureFromImage(packet.normalizedImage);
    
    return packet;
  }
  
  public void getNearestNeighbors(ProcessPacket packet) {
    packet.nearest = lookup.getNearestNeigbors(packet.imageFeature, 20);
  }
  
  
  public void getInitialGuesses(ProcessPacket packet) {
    List<ParamHausdorffFeature> dbs = new ArrayList<ParamHausdorffFeature>();
    List<int[]> triangle_index_images = new ArrayList<int[]>();
    List<Float> angles = new ArrayList<Float>();
    List<SkeletonState> poses = new ArrayList<SkeletonState>();
    
    for (TinyImageResult<ParamHausdorffFeature> result : packet.nearest) {
      dbs.add(result.normalizedResult);
      triangle_index_images.add(result.normalizedTriangleIndexImage);
      angles.add(result.zrotation);
      poses.add(result.normalizedState);
      
      if (poses.size() >= N_GUESSES) break;
    }
    
    HistoryPacket historyPacket = null;
    if ((historyPacket = historyStore.getFeature()) != null) {
      packet.historyPose = historyPacket.pose;
      packet.historyFeature = historyPacket.feature;
      int[] encodedTriangleIndexImage = historyPacket.encodedTriangleIndexImage;
     
      dbs.add(packet.historyFeature);
      angles.add(0.0f);
      triangle_index_images.add(encodedTriangleIndexImage);
      poses.add(packet.historyPose);
    }

    packet.initialGuesses = new ArrayList<SkeletonState>();
    correspondenceUtility.clearCachedTransform();
    for (int i=0; i<dbs.size(); i++) {
      List<Correspondence> correspondences = DatabaseConstraintHelper.quickerAndDirtierCorrespondencesDB(hausdorff, dbs.get(i),
          triangle_index_images.get(i), angles.get(i), 20);
      // for the last item, if we have history, tell fit correspondence that it
      // is the history (it's close)
      packet.initialGuesses.add(correspondenceUtility.fitCorrespondences(
          correspondences, packet.normalizedToOriginal, poses.get(i), 
          (i == dbs.size() - 1 && packet.historyPose != null)));
    }
    packet.initialGuessFeatures = dbs;
    packet.initialGuessAngles = angles;
  }

  public void blendInitialGuesses(ProcessPacket packet) {
    float[] errors = new float[packet.initialGuesses.size()];
    for (int i=0; i<packet.initialGuesses.size(); i++) {
      errors[i] = packet.nearest.get(i).diff;
      if (i == packet.initialGuesses.size() - 1 && packet.historyPose != null) {
        errors[i] = errors[i] * HISTORY_PREFERENCE;
      }
    }
    
    if (packet.historyPose != null) {
      float r_history_distance = hausdorff.diffFeature(packet.historyFeature, packet.imageFeature);
      float epsilon = HISTORY_3D_DISTANCE_THRESHOLD;
      float delta = HISTORY_IMAGE_THRESHOLD;
//      System.out.println("HISTORY_IMAGE_THRESHOLD: " + r_history_distance);
      
      StringBuffer buf = new StringBuffer();
      buf.append(r_history_distance);
      buf.append(",");
      float[] q_history = database.getSampling().getPosePoints(packet.historyPose);
      // compute the q state differences from history
      for (int i=0; i<packet.initialGuesses.size(); i++) {
        SkeletonState guess = packet.initialGuesses.get(i);
        float[] q_prime = database.getSampling().getPosePoints(guess);
        float q_history_distance = ConfigurationSampling.posePointDistance(q_prime, q_history);
        buf.append(q_history_distance);
        if (r_history_distance < delta && q_history_distance > epsilon) {
          // effectively set the weight to zero
          errors[i] = 1e10f;
          buf.append("*");
        }
        buf.append(",");
      }
      packet.historyStats = buf.toString();
    }

    float bestErrorGlobal = Float.MAX_VALUE;
    for (int i=0; i<errors.length; i++) {
      bestErrorGlobal = Math.min(errors[i], bestErrorGlobal);
    }
    // exponential weights
//    FloatMatrixUtils.println(errors, "errors");
    float[] weights = new float[packet.initialGuesses.size()];
    for (int i=0; i<packet.initialGuesses.size(); i++) {
      weights[i] = (float) Math.exp(- (errors[i] - bestErrorGlobal) * (errors[i] - bestErrorGlobal) / (KNN_BLEND_SIGMA * KNN_BLEND_SIGMA));
    }
//    FloatMatrixUtils.println(weights, "weights");
    FloatMatrixUtils.multiplyInPlace(1.0f / FloatMatrixUtils.sum(weights), weights);
    packet.initialGuessErrors = errors;
    
    packet.blendedInitialGuess = QuaternionBlendTest.computeBlend(
        packet.initialGuesses.toArray(new SkeletonState[0]), weights);
    
    // render the blended
    rasterizer.setInverseTransform(packet.normalizedToOriginal, TinyImageDatabase.IMAGE_SIZE);
    packet.blendedTriangleIndexImage = rasterizer.encode(packet.blendedInitialGuess);
    packet.blendedFeature = hausdorff.generateFeatureFromTriangleIndexImage(packet.blendedTriangleIndexImage);
    
    packet.prior = packet.blendedInitialGuess;
    packet.priorFeature = packet.blendedFeature;
    packet.priorTriangleIndexImage = packet.blendedTriangleIndexImage;
  }
  
  public void warpBlended(ProcessPacket packet) {
    List<float[]> per_patch_errors = new ArrayList<float[]>();
    List<int[]> per_patch_counts = new ArrayList<int[]>();
    List<ParamHausdorffFeature> features = new ArrayList<ParamHausdorffFeature>();
    List<SkeletonState> poses = new ArrayList<SkeletonState>();
    boolean hasHistory = (packet.historyPose != null);
    
    Timer timer = new Timer();
    for (int i=0; i<packet.initialGuesses.size(); i++) {
      if (packet.initialGuessErrors[i] < 1e2f) {
        // re-render with the blended global pose
        BasicSkeletonState blendedStateFrame = new BasicSkeletonState(packet.blendedInitialGuess);
        SkeletonState currentState = packet.initialGuesses.get(i);
        // copy everything but the global pose
        for (int j=1; j<currentState.getNumJoints(); j++) {
          blendedStateFrame.setRotation(j, currentState.getRotation(j));
        }
        // render and generate feature
        ParamHausdorffFeature feature = hausdorff
            .generateFeatureFromTriangleIndexImage(rasterizer
                .encode(blendedStateFrame));
        
        features.add(feature);
        poses.add(blendedStateFrame);
      }
    }
    if (printTiming)
      timer.tocln("Rendered for warping");
    timer.tic();
    
    features.add(packet.blendedFeature);
    poses.add(packet.blendedInitialGuess);
    
    for (int i=0; i<features.size(); i++) {
      per_patch_errors.add(LeftGloveWarpingHeuristics.getAvgErrorsCounted(hausdorff,
          packet.patchClassifiedFeature, features.get(i)));
      per_patch_counts.add(LeftGloveWarpingHeuristics.getPerPatchCounts(hausdorff,
          packet.patchClassifiedFeature, features.get(i)));
    }
    
    packet.perPatchState = new WarpState();
    packet.perPatchState.classified = packet.patchClassifiedFeature;
    packet.perPatchState.transform = packet.normalizedToOriginal;
    packet.perPatchState.features = features;
    packet.perPatchState.perPatchErrors = per_patch_errors;
    packet.perPatchState.associatedPoses = poses;
    packet.perPatchState.perPatchCounts = per_patch_counts;
    packet.perPatchState.hasHistory = hasHistory;
    if (printTiming)
      timer.tocln("computed errors");
    
    timer.tic();
    packet.warped = LeftGloveWarpingHeuristics.warpMinError(
        packet.perPatchState, hausdorff, database.getSampling()
            .getMeshPointSampling());
    if (printTiming)
      timer.tocln("Actually warped");
    
    packet.prior = packet.warped;
    packet.priorTriangleIndexImage = rasterizer.encode(packet.prior);
    packet.priorFeature = hausdorff
        .generateFeatureFromTriangleIndexImage(packet.priorTriangleIndexImage);
  }

  public void moreIKPrior(ProcessPacket packet) {
    if (packet.patchClassifiedFeature == null) {
      cleanupQuery(packet);
    }
    
    /*
    List<ImageConstraint> constraints = new ArrayList<ImageConstraint>();
    constraints = DenseConstraintsUtility3.computeDenseImageConstraints(
        hausdorff, packet.patchClassifiedFeature, packet.patchClassifiedFeature, packet.priorFeature,
        packet.priorTriangleIndexImage, constraints);
        */

    
    Pair<Point2f[], int[]> p = FastCorrespondenceGenerator.getPatchAverages(hausdorff, packet.patchClassifiedFeature);
    Point2f[] vEnd = p.getFirst();
    int[] endCounts = p.getSecond();
    
    int[] nearestTriangles = FastCorrespondenceGenerator
      .populateNearestTriangles(hausdorff, packet.priorTriangleIndexImage,
          packet.priorFeature);
      
    List<Correspondence> correspondences = new ArrayList<Correspondence>();
    
    for (int i = 0; i < nearestTriangles.length; i++) {
      if (nearestTriangles[i] != -1 && endCounts[i] >= 3) {
        Correspondence c = new Correspondence(vEnd[i].x, vEnd[i].y,
          nearestTriangles[i]);
        c.patch = i;
        correspondences.add(c);
      }
    }
      
    correspondenceUtility.clearCachedTransform();
    packet.prior = correspondenceUtility.fitCorrespondences(
        correspondences, packet.normalizedToOriginal, packet.prior, true);
    packet.priorTriangleIndexImage = rasterizer.encode(packet.prior);
    packet.priorFeature = hausdorff
        .generateFeatureFromTriangleIndexImage(packet.priorTriangleIndexImage);
  }
  
  public void cleanupQuery(ProcessPacket packet) {
    List<ParamHausdorffFeature> dbs = new ArrayList<ParamHausdorffFeature>();
    List<Float> angles = new ArrayList<Float>();
    List<Float> errors = new ArrayList<Float>();
    
    for (int i=0; i<packet.initialGuesses.size(); i++) {
      if (packet.initialGuessErrors[i] < 1e2f) {
        dbs.add(packet.initialGuessFeatures.get(i));
        angles.add(packet.initialGuessAngles.get(i));
        errors.add(packet.initialGuessErrors[i]);
      }
    }
    
    if (errors.size() == 0) {
      dbs.add(packet.blendedFeature);
      angles.add(0.0f);
      errors.add(hausdorff.diffFeature(packet.imageFeature, packet.blendedFeature));
    }
    
    packet.patchClassifiedFeature = pixelClassifier.classifyQueryPixels(hausdorff,
        packet.imageFeature, dbs, angles, errors);
    packet.patchClassifiedFeature = PatchConnectedComponentFilter
        .filterSmallComponents(hausdorff, packet.patchClassifiedFeature);
  }
  
  public void makeConstraints(ProcessPacket packet) {
    List<ImageConstraint> constraints = new ArrayList<ImageConstraint>();
    constraints = DenseConstraintsUtility3.computeDenseImageConstraints(
        hausdorff, packet.patchClassifiedFeature, packet.patchClassifiedFeature, packet.priorFeature,
        packet.priorTriangleIndexImage, constraints);
    
    VarianceConstraints vc = new VarianceConstraints();
    vc.transform = packet.normalizedToOriginal;
    for (ImageConstraint constraint : constraints) {
      //if (constraint.weight < 1) continue;
      if (constraint.confidence < 3) continue;
      
      vc.nearestTriangle.add(constraint.triangleIndex);
      vc.target.add(constraint.point);
      int[][] tips = LeftGloveWarpingHeuristics.GROUP_TIPS;
      boolean isTip = false;
      for (int[] tip_group : tips) {
        for (int tip : tip_group) {
          if (tip == constraint.patch) {
            vc.stdev.add(2.0f);
            isTip = true;
          }
        }
      }
      if (!isTip)
        vc.stdev.add(12.0f);
      vc.patch.add((int) constraint.patch);
    }
    
    packet.vc = vc;
  }
  
  public void filter(ProcessPacket packet) {
    SkeletonState history = packet.prior;
    if (packet.historyPose != null
        && hausdorff.diffFeature(packet.historyFeature, packet.imageFeature) < 
        HISTORY_IMAGE_THRESHOLD)
      history = packet.historyPose;
      
    packet.filtered = kalmanFilter.filter(Collections.singletonList(packet.vc),
        Collections.singletonList(calibration), packet.prior, history, 
        packet.prior);
  }
  
  public ProcessPacket processJustBlended(BufferedImage image, boolean debug) {
    Timer totalTimer = new Timer();
    ProcessPacket packet = processNoFilter(image, debug);
    
    for (int i = 0; i < packet.priorTriangleIndexImage.length; i++)
      packet.priorTriangleIndexImage[i] = DatabaseEntryRenderer
          .encodeIndexRGB(packet.priorTriangleIndexImage[i]);
    historyStore.setFeature(packet.prior,
        packet.priorTriangleIndexImage, packet.priorFeature);

    if (printTiming) {
      totalTimer.tocln("Total processing time");
    }
    return packet;
  }
  
  public ProcessPacket processBlendedWithMoreIK(BufferedImage image, boolean debug) {
    Timer totalTimer = new Timer();
    ProcessPacket packet = processNoFilter(image, debug);
    moreIKPrior(packet);
    
    for (int i = 0; i < packet.priorTriangleIndexImage.length; i++)
      packet.priorTriangleIndexImage[i] = DatabaseEntryRenderer
          .encodeIndexRGB(packet.priorTriangleIndexImage[i]);
    historyStore.setFeature(packet.prior,
        packet.priorTriangleIndexImage, packet.priorFeature);

    if (printTiming) {
      totalTimer.tocln("Total processing time");
    }
    return packet;
  }
  
  public ProcessPacket processRobust(BufferedImage image, boolean debug) {
    Timer totalTimer = new Timer();
    Timer timer = new Timer();
    ProcessPacket packet = processNoFilter(image, debug);
    timer.tic();
    cleanupQuery(packet);
    if (printTiming)
      timer.tocln("Cleaned up query");
    timer.tic();
    makeConstraints(packet);
    if (printTiming)
      timer.tocln("Made constraints");
    timer.tic();
    filter(packet);
    if (printTiming) {
      timer.tocln("Filtered with IK");
    }
    
    int[] encodedTriangleIndexImage = rasterizer.encode(packet.filtered);
    ParamHausdorffFeature feature = hausdorff.generateFeatureFromTriangleIndexImage(encodedTriangleIndexImage);
    for (int i=0; i<encodedTriangleIndexImage.length; i++)
      encodedTriangleIndexImage[i] = DatabaseEntryRenderer.encodeIndexRGB(encodedTriangleIndexImage[i]);
    historyStore.setFeature(packet.filtered, encodedTriangleIndexImage, feature);

    if (printTiming) {
      totalTimer.tocln("Total processing time");
    }
    return packet;
  }
  
  public ProcessPacket processNoFilter(BufferedImage image, boolean debug) {
    Timer timer = new Timer();
    timer.tic();
    ProcessPacket packet = processImage(image);
    if (printTiming)
      timer.tocln("Processed image");
    timer.tic();
    getNearestNeighbors(packet);

    if (printTiming)
      timer.tocln("Got NN");
    timer.tic();
    getInitialGuesses(packet);
    if (printTiming)
      timer.tocln("Got initial guesses");
    timer.tic();

    blendInitialGuesses(packet);
    if (printTiming)
      timer.tocln("Blended guesses");

    return packet;
  }

  
  public BufferedImage viewNearestNeighbors(ProcessPacket packet) {
    BufferedImage resultsImage = null;
    Graphics2D g2d = null;
    int currentHeight = 0;
    for (TinyImageResult<ParamHausdorffFeature> result : packet.nearest) {
      ParamHausdorffFeature rotated = hausdorff.rotateFeature(result.normalizedResult, result.zrotation);
      BufferedImage imageDiff = hausdorff.viewFeatureDiff(packet.imageFeature,
          rotated);
      
      if (resultsImage == null) {
        int numEntries = packet.nearest.size();
        resultsImage = new BufferedImage(imageDiff.getWidth(), imageDiff
            .getHeight()
            * numEntries, BufferedImage.TYPE_INT_ARGB);
        g2d = resultsImage.createGraphics();
      }
      g2d.drawImage(imageDiff, 0, currentHeight, null);
      currentHeight += imageDiff.getHeight();
    }
    g2d.dispose();
    return resultsImage;
  }
  
  public BufferedImage viewFeature(AffineTransform transform, ParamHausdorffFeature feature) {
    BufferedImage normalized = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = normalized.createGraphics();
    BufferedImage img = hausdorff.viewFeature(feature);
    g2d.drawImage(img, transform, null);
    g2d.dispose();
    return normalized;
  }
  
  public static void processFiles(String format,
      TrackingExample trackingExample,
      GeometricCalibrationExample geometricCalibration,
      MarkerModel markerModel, ColorCalibrationExample colorCalibration,
      int numEntries, BoostingExample boostingExample) throws IOException {
    ProcessUtility bfpu = new ProcessUtility(trackingExample,
        geometricCalibration, markerModel, colorCalibration, numEntries);
    if (boostingExample != null) {
      bfpu.useBoost(boostingExample);
    }

    List<String> files = DirectoryIO.getNumberedFiles(format, 1);

    Timer timer = new Timer();
    timer.tic();
    String metricName = bfpu.lookup.getName();
    
    for (String filename : files) {
      timer.tocln("Processing image: " + filename);
      BufferedImage image = ImageIO.read(new File(filename));
      String outputName = filename.replaceAll("\\.png", "");

      ProcessPacket dp = bfpu.processRobust(image, true);
      dp.write(bfpu.hausdorff, outputName, metricName);
    }
    
    new ProcessRegression(format.substring(0, format.length() - 4), metricName,
        trackingExample, geometricCalibration, markerModel);
  }
  
  public static void main(String[] args) {
    try {
      TrackingExample trackingExample = LeftGloveTrackingExample.getInstance();
      GeometricCalibrationExample geometricCalibration = SingleCameraCalibrationExample.getInstance();
      
      ProcessUtility processUtility = new ProcessUtility(trackingExample,
          geometricCalibration, LeftGloveMarkerModel
              .getInstance(), LeftGloveColorCalibrationExample.getInstance(),
          100000);
      String OSD = StandardEnvironment.getOSD();
      BufferedImage image = ImageIO.read(new File(OSD
          + "/data/HandTracking/Videos/DefaultTest_TinyTip/capture_0_047.png"));
      ProcessPacket packet = processUtility.processRobust(image, false);
      
      SkinningExample example = trackingExample.getSkinningExample();
      CustomCameraRenderer renderer = new CustomCameraRenderer(example
          .getSSDSkinFromState(packet.filtered));
      renderer.start(geometricCalibration.getCameraData());
      
      
      FilterImage.showImageInNewWindow(processUtility.viewFeature(packet.normalizedToOriginal, packet.patchClassifiedFeature));
      
      BufferedImage result = processUtility.viewNearestNeighbors(packet);
      FilterImage.showImageInNewWindow(result);

      FilterImage.showImageInNewWindow(processUtility.viewFeature(packet.normalizedToOriginal, packet.blendedFeature), "Blended");
      
      int width = 640;
      int height = 480;
      FilterImage.showImageInNewWindow(packet.vc.view(width, height, processUtility.hausdorff));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void setBackground()
  {
	  nativeDecoding.setBackground();
  }
  
  public BufferedImage getBackgroundSubtractedImg()
  {
	  BufferedImage decoded = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
	  nativeDecoding.getLastDecoded(decoded);
	  
	  return decoded;
  }
}
