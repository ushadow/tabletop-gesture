package edu.mit.yingyin.tabletop.recognition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import skinning.BasicSkeletonState;
import skinning.SkeletonState;


import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;

import edu.mit.yingyin.util.QuatUtil;

public class HMMRecognizer extends GestureRecognizer {

	private static final int DIFF_THRESH = 15;
	private static final int Z_THRESH = 500;
	
	List<GestureHMM> hmms;
	GaussHMM hmmSeg;
	GestureEvent currentGesture = null;
	Vector3f currentPos = new Vector3f();
	Vector3f previousPos = new Vector3f();
	SkeletonState initState;
	
	float[] mu;
	float[] sigma;
	float[] feature;
	float[] prevFeature;
	float[] diffNorm = new float[1];
	int numModels;
	int initCount = 0;
	boolean gestureOn = false;
	
	/**
	 * Initialize the HMM model parameters
	 * @param filePath full path of the folder containing the Matlab data.
	 */
	public HMMRecognizer(String filePath) {
		//initialize member fields
		loadMatlabParams(filePath);
	}
	
	public GestureEvent classify(SkeletonState state) {
		if (filter(state)) 
			return null;
		
		preprocessData(state);
		
		//skip the first two frames
		if (initCount < 2) {
			initCount++;
			return null;
		}
//		for(int i = 0; i < featureLen; i++)
//			System.out.print(feature[i] + " ");
//		System.out.println();
		
		diffNorm[0] = getNormDiff();
		hmmSeg.timeStep(diffNorm);
		if (hmmSeg.getCurrentState() == 1) {
			if (!gestureOn) {
				//gesture just starts
				gestureOn = true;
				initState = state;
				for (int i = 0; i < numModels; i++) {
					hmms.get(i).reset();
					hmms.get(i).timeStep(feature);
				}
			} else {
				//gesture continues
				for (int i = 0; i < numModels; i++) 
					hmms.get(i).timeStep(feature);
			}
			
			Collections.sort(hmms);
			if (hmms.get(hmms.size() - 1).getLoglik() - hmms.get(hmms.size() - 2).getLoglik() > DIFF_THRESH
					&& currentGesture == null) {
				//gesture recognized
				currentGesture = new GestureEvent(hmms.get(hmms.size() - 1).getName(), initState, state);
				for (GestureEventListener gel : gelList)
					gel.gestureRecognized(currentGesture);
			}
		} else {
			//gesture ends
			if (gestureOn == true) {
				gestureOn = false;

				if (currentGesture != null) {
					currentGesture.setEndState(state);
//					for (GestureEventListener gel : gelList) {
//						gel.gestureEnded(currentGesture);
//					}
					currentGesture = null;
				}
			}
		}
		
		return currentGesture;
	}
	
	/**
	 * Read HMM model parameters from matlab files
	 * @param filePath full path of the matlab file
	 */
	private void loadMatlabParams(String filePath) {

		try {
			//load hmm parameters
			MatFileReader reader = new MatFileReader(filePath);

			MLStructure mlHmmSeg = (MLStructure)reader.getContent().get("hmmSeg");
			MLCell mlHMMs = (MLCell)reader.getContent().get("hmm");
			//load scaling parameters
			double[][] mlMu = ((MLDouble)reader.getContent().get("mu")).getArray();
			double[][] mlSigma = ((MLDouble)reader.getContent().get("sigma")).getArray();

			hmmSeg = new GaussHMM(mlHmmSeg, false);
			
			numModels = mlHMMs.getSize();
			System.out.println("numModels = " + numModels);
			hmms = new ArrayList<GestureHMM>(numModels);
			
			for (int i = 0; i < numModels; i++) {
				MLStructure hmm = (MLStructure)mlHMMs.get(i);
				MLChar mlName = (MLChar)hmm.getField("gesture");
				String name = mlName.getString(0);
				GestureHMM gestureHMM = new GestureHMM(name, hmm);
				hmms.add(gestureHMM);
			}
			
			int featureLen = mlMu.length;
			System.out.println("featureLen = " + featureLen);
			mu = new float[featureLen];
			sigma = new float[featureLen];
			feature = new float[featureLen];
			prevFeature = new float[featureLen];
			
			for (int i = 0; i < featureLen; i++) {
				mu[i] = (float)mlMu[i][0];
				sigma[i] = (float)mlSigma[i][0];
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void preprocessData(SkeletonState state)
	{
		setFeatureVector(state);
		normalizeFeature();
	}
	
	private void setFeatureVector(SkeletonState state) {
		previousPos.set(currentPos);
		System.arraycopy(feature, 0, prevFeature, 0, feature.length);
		currentPos = state.getTranslation(0);
		
		int featureIndex = 0;
		
		feature[featureIndex++] = currentPos.x - previousPos.x;
		feature[featureIndex++] = currentPos.y - previousPos.y;
		feature[featureIndex++] = currentPos.z;
		
		//forearm
		Quat4f q = state.getRotation(0);
		Tuple3f t = QuatUtil.getEulerAngles(q);
		feature[featureIndex++] = t.x;
		feature[featureIndex++] = t.y;
		feature[featureIndex++] = t.z;
		
		//thumb
		q = state.getRotation(2);
		t = QuatUtil.getEulerAngles(q);
		feature[featureIndex++] = t.y;
		feature[featureIndex++] = t.z;
		
		q = state.getRotation(3);
		t = QuatUtil.getEulerAngles(q);
		feature[featureIndex++] = t.y;
		
		q = state.getRotation(4);
		t = QuatUtil.getEulerAngles(q);
		feature[featureIndex++] = t.y;
		
		for (int i = 0; i < 4; i++) {
			int indexBase = 5 + i * 3;
			q = state.getRotation(indexBase);
			t = QuatUtil.getEulerAngles(q);
			feature[featureIndex++] = t.y;
			feature[featureIndex++] = t.z;
			
			q = state.getRotation(indexBase + 1);
			t = QuatUtil.getEulerAngles(q);
			feature[featureIndex++] = t.z;
			
			q = state.getRotation(indexBase + 2);
			t = QuatUtil.getEulerAngles(q);
			feature[featureIndex++] = t.z;
		}
	}
	
	private void normalizeFeature() {
		for (int i = 0; i < feature.length; i++)
			feature[i] = (feature[i] - mu[i]) / sigma[i];
	}
	
	/**
	 * calculate norm-2 of the difference vector of (featurure - prevFeature)
	 * @return
	 */
	private float getNormDiff() {
		
		//feature[0] and feature[1] are x and y velocities
		float sum = feature[0] * feature[0] + feature[1] * feature[1];
		
		for (int i = 2; i < feature.length; i++) 
			sum += (feature[i] - prevFeature[i]) * (feature[i] - prevFeature[i]);
		
		return (float)Math.sqrt(sum);
	}
	
	private boolean filter(SkeletonState state) {
		float z = state.getTranslation(0).z; 
		if ( z > Z_THRESH || z < -Z_THRESH )
			return true;
		return false;
	}

}
