package edu.mit.yingyin.tabletop.recognition;

import java.util.ArrayList;
import java.util.List;

import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;

import rywang.math.Gaussian;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

public class GaussHMM {
	
	private DoubleMatrix1D prior1;
	private DoubleMatrix2D transmat1; //transmat1(i,j) = Pr(Q(t+1)=j | Q(t)=i)
	private List<Gaussian> gaussians;
	private DoubleMatrix1D obslik;
	private DoubleMatrix1D alpha;
	private DoubleMatrix1D m;
	private float loglik = 0;
	private boolean start = true; //start of the sequence
	private boolean end = false;
	private boolean hasEndState = false;
	private int currentState = -1;
	private int numStates;
	
	public GaussHMM(MLStructure hmm, boolean hasEndState) {
		this.hasEndState = hasEndState;
		double[][] p = ((MLDouble)hmm.getField("prior1")).getArray();
		double[][] t = ((MLDouble)hmm.getField("transmat1")).getArray();
		
		numStates = p.length;
		prior1 = new DenseDoubleMatrix1D(numStates);
		for(int i = 0; i < numStates; i++)
			prior1.set(i, p[i][0]);
		
		transmat1 = new DenseDoubleMatrix2D(t);
		gaussians = new ArrayList<Gaussian>(numStates);
		obslik = new DenseDoubleMatrix1D(numStates);
		obslik.setQuick(numStates - 1, 1);
		alpha = new DenseDoubleMatrix1D(numStates);
		m = new DenseDoubleMatrix1D(numStates);
		
		MLDouble mlMu1 = (MLDouble)hmm.getField("mu1");
		MLDouble mlSigma1 = (MLDouble)hmm.getField("Sigma1");
		double[][] mu1 = mlMu1.getArray();
		double[][] sigma1 = mlSigma1.getArray();
		int featureLen = mlMu1.getM();
		
		for (int i = 0; i < numStates; i++) {
			float[] mean = new float[featureLen];
			float[][] covariance = new float[featureLen][featureLen];
			for (int j = 0; j < featureLen; j++) {
				mean[j] = (float)mu1[j][i];
				
				for (int k = 0; k < featureLen; k++)
					covariance[j][k] = (float)sigma1[j][featureLen * i + k];
			}
			
			Gaussian gaussian = new Gaussian(mean, covariance, 0);
			gaussians.add(gaussian);
		}
	}
	
	public void timeStep(float[] feature) {
		
		int numObslikStates;
		
		if (hasEndState)
			numObslikStates = numStates - 1;
		else numObslikStates = numStates;
		
		for  (int i = 0; i < numObslikStates; i++)
			obslik.setQuick(i, gaussians.get(i).likelihood(feature));

		if(start) {
			for(int i  = 0; i < numStates; i++)
				alpha.setQuick(i, prior1.get(i) * obslik.get(i));
			start = false;
		} else {
			//m = trainsmat1' * alpha
			transmat1.viewDice().zMult(alpha, m);
			for(int i  = 0; i < numStates; i++)
				alpha.setQuick(i, m.get(i) * obslik.get(i));
			
		}
		
		double scale = alpha.zSum();
		loglik += Math.log(scale);
		
		//normalize alpha
		if (scale == 0)
			scale = 1;
		
		for(int i = 0; i < numStates; i++)
			alpha.setQuick(i, alpha.get(i)/scale);
		
		double maxAlpha = 0;
		for ( int i = 0; i < numStates; i++) {
			if(alpha.get(i) > maxAlpha) {
				maxAlpha = alpha.get(i);
				currentState = i;
			}
		}
	}
	
	public void reset() {
		loglik = 0;
		start = true;
		end = false;
		currentState = -1;
	}
	
	public float getLoglik() {
		return loglik;
	}
	
	public int getCurrentState() {
		return currentState;
	}
	
	public boolean isEnd() {
		return end;
	}
}
