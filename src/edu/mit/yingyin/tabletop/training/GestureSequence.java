package edu.mit.yingyin.tabletop.training;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import edu.mit.yingyin.tabletop.environment.EnvConstants;


public class GestureSequence {
	private static final String GESTURE_NAMES_FILE_PATH = EnvConstants.MAIN_FOLDER + "data/Gesture/Gestures.txt";
	private static final String GESTURE_SEQUENCE = EnvConstants.MAIN_FOLDER + "data/Gesture/gesture_sequence.txt";
	
	public static void main(String[] args) {
		List<String> gestures = new ArrayList<String>();
		File gestureNamesFile = new File(GESTURE_NAMES_FILE_PATH);
		try {
			Scanner scanner = new Scanner(gestureNamesFile);
			while(scanner.hasNext())
				gestures.add(scanner.next());
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int[] count = new int[gestures.size()];
		Random rand = new Random(System.nanoTime());
		int g;
		try {
			PrintStream p = new PrintStream(new File(GESTURE_SEQUENCE));
			for (int i = 0; i < 36; i++) {
				g = rand.nextInt(gestures.size());
				count[g]++;
				p.println(gestures.get(g));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
