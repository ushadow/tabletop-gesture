package edu.mit.yingyin.tabletop.training;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.util.QuatUtil;

import rywang.util.ObjectIO;
import skinning.SkeletonState;


public class DataProcessor {
	
	//name of the folder that contains the data to process
	private static final String DATA_DIR = EnvConstants.DATA_PROCESSOR_DATA_DIR;
	
	//name of the folder where the output should go to. ends with '/'
	private static final String OUTPUT_DIR = EnvConstants.DATA_RROCESSOR_OUTPUT_DIR;
	
	private static final String TRAINING_DIR = EnvConstants.MAIN_FOLDER + "Training/";
	private static final String RESULT_DIR = TRAINING_DIR + OUTPUT_DIR + DATA_DIR;
	
	/**
	 * Process all files in DATA_DIR and save the files in RESULT_DIR.
	 * The output file contains hand pose data for each row. The first vector is the translation of the forearm. The rest vectors are the 
	 * roll, yaw and pitch for 17 joints
	 * @param args
	 */
	public static void main(String[] args) {
		//directory containing .ss files
		String ssDir = TRAINING_DIR + DATA_DIR;
		File dir = new File(ssDir);
		
		String[] files = dir.list();
		
		if (files == null) {
			System.err.println("Invalid directory!");
			return;
		}
		
		String newDir = RESULT_DIR;
		boolean exists = (new File(newDir)).exists();
		if (!exists)
			(new File(newDir)).mkdir();
		else {			
			System.err.println(newDir + " already exists!");
			System.out.println("Continue? (y/n): ");
			Scanner scanIn = new Scanner(System.in);
			String answer = scanIn.nextLine();
			if (!answer.equals("y")) {
				System.out.println("Quit.");
				System.exit(0);
			}
			System.out.println("Continue...");
		}
		
		for (String fileName : files) {
			if (!fileName.endsWith(".ss"))
				continue;
			
			try {
				String fullPath = ssDir + fileName;
				List<SkeletonState> states = (List<SkeletonState>)ObjectIO.readObject(fullPath);
				
				String newName = newDir + fileName.replaceAll(".ss", ".txt");
				PrintStream ps = new PrintStream(new File(newName));
				for (SkeletonState state : states) {
					int numJoints = state.getNumJoints();
					Vector3f v = state.getTranslation(0);
					ps.format("%8.3f %8.3f %8.3f ",v.x, v.y, v.z);
					for (int i = 0; i < numJoints; i ++) {
						Quat4f q = state.getRotation(i);
						Tuple3f t = QuatUtil.getEulerAngles(q);
						ps.format("%6.3f %6.3f %6.3f ", t.x, t.y, t.z);
					}
					ps.println();
				}
				ps.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
