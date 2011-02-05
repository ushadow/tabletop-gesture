package edu.mit.yingyin.tabletop.environment;

public class EnvConstants {

	public static final String LOCAL_FOLDER = "C:/Documents and Settings/yingyin/Desktop/";
	public static final String MAIN_FOLDER = LOCAL_FOLDER;
	
	public static final String TEST_FOLDER = LOCAL_FOLDER + "/data/HandTracking/Satellite_LF/";
	public static final String SUFFIX = "_proc";
	
	public static final String IMAGE_NAME = "/image1"; //image used for calibration
	public static final String IMAGE_TYPE = ".png";
	public static final String CHALLENGE_SUFFIX = ".colorchallenge";
	
	public static final String CAMERA_CALIB_FOLDER = MAIN_FOLDER + "CameraCalib/7_7/";
	
	/**
	 * Directory containing the calibration data. Note this path does not end with '/'
	 */
	public static final String CALIB_DATA_FOLDER = TEST_FOLDER + "calib_data" + SUFFIX;
	public static final String CALIB_IMAGE_PREFIX = CALIB_DATA_FOLDER + "/image";
	public static final String TARGET_IMAGE_PATH = CALIB_DATA_FOLDER + IMAGE_NAME + IMAGE_TYPE;
	public static final String TARGET_DATA_PATH = CALIB_DATA_FOLDER + IMAGE_NAME + CHALLENGE_SUFFIX;
	public static final String TARGET_POINTS_PATH = CALIB_DATA_FOLDER + IMAGE_NAME + ".pts";
	public static final String CORRESPONDENCE_PATH = TEST_FOLDER + "correspondence.txt";
	public static final String TRANSFORMED_PATH = TEST_FOLDER + "transformed.txt";
	public static final String TRANFORM_MATRIX_PATH = TEST_FOLDER + "transform.matrix";
	
	//recording directory for for DebugDriver
	public static final String RECORDING_DIR = LOCAL_FOLDER + "recording/05_20/";
	
	//environment constants for DataProcessor
	public static final String DATA_PROCESSOR_DATA_DIR = "05_21/";
	public static final String DATA_RROCESSOR_OUTPUT_DIR = "TrainingData/isolated/";
	
	//environment constants for SkeletonViewer
	public static final String SV_INPUT_FILE_NAME = "lei_2/continuous_003.ss";
	public static final String SV_OUTPUT_DIR = "TrainingData/continuous/"; 

	//environment constants for BrowserDriver
	public static final boolean BROWSERDRIVER_SIMULATE = false;
	
	//enviorment constants for processor
	public static final boolean BROWSER_DEBUG = false;
	
	public static final boolean TINYIMAGE_DEBUG = true;
	
	public static final String SKELETON_SEQ_FILE = EnvConstants.MAIN_FOLDER + "Training/05_21/continuous_%03d.ss";
	//whether to show the tiny image in a debug window
	public static final boolean GESTUREDRIVER_PRINT = true;
	
	public static final double EPS = 1E-4;
}
