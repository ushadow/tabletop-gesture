package edu.mit.yingyin.tabletop.handtracking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.mit.yingyin.tabletop.browser.MapController;
import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.tabletop.environment.RecognizerExample;
import edu.mit.yingyin.tabletop.recognition.GestureEvent;
import edu.mit.yingyin.tabletop.recognition.GestureEventListener;
import edu.mit.yingyin.tabletop.recognition.GestureRecognizer;
import edu.mit.yingyin.tabletop.recognition.HMMRecognizer;

import rywang.util.ObjectIO;
import skinning.SkeletonState;

public class TrackerSimulator implements TrackerInterface
{
	private static final String HMM_PARAMS_PATH = EnvConstants.MAIN_FOLDER + "Training/MatlabData/hmmParams_04_28.mat";
	private String skeletonDataPath = EnvConstants.MAIN_FOLDER + "Training/03_18/continuous_002.ss";

	private List<SkeletonState> states;
	private GestureRecognizer recognizer;
	
	public TrackerSimulator() {
		
		recognizer = new HMMRecognizer(HMM_PARAMS_PATH);
	
		try {
			states = (List<SkeletonState>)ObjectIO.readObject(skeletonDataPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void addGestureEventListener(GestureEventListener gel) {
		recognizer.addGestureEventListener(gel);
	}

	@Override
	public void start() {
		for (int i = 0; i < states.size(); i++) {
			GestureEvent gesture = recognizer.classify(states.get(i));
			if (gesture != null)
				System.out.println("time = " + i + " gesture = " + gesture);
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}
	
	public void setBackground() {
		
	}
	
	public static void main(String[] args) {
		TrackerSimulator tracker = new TrackerSimulator();
		tracker.start();
	}
	
}
