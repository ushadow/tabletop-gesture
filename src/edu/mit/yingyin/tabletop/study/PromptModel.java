package edu.mit.yingyin.tabletop.study;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.mit.yingyin.util.SimpleAudioPlayer;

public class PromptModel {
	private static final String SOUND_FILE_NAME = "resource/audio/IM08.wav";
	private static final String PROMPTS_FILE_NAME = "resource/doc/prompts.txt";
	private File alertSoundFile;
	private List<String> promptsList = new ArrayList<String>();
	private int currentPromptIndex = 0;
	
	public PromptModel() {
		alertSoundFile = new File(SOUND_FILE_NAME);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(PROMPTS_FILE_NAME));
			String str;
			while((str = br.readLine()) != null) {
				promptsList.add(str);
			}
		} catch (FileNotFoundException e) {
			System.err.println(this.getClass().getName() + ": cannot find file " + PROMPTS_FILE_NAME);
		} catch (IOException ioe) {
			System.err.println(this.getClass().getName() + ": error reading file " + PROMPTS_FILE_NAME);
		}
	}
	
	public void playAlert() {
		SimpleAudioPlayer.play(alertSoundFile);
	}
	
	public String getNextPrompt() {
		if (currentPromptIndex >= promptsList.size())
			currentPromptIndex = 0;
		return promptsList.get(currentPromptIndex++) + "\n\n";
	}
}
