package edu.mit.yingyin.tabletop.study;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

public class PromptController extends KeyAdapter implements ActionListener {

	private static final int FLASH_INTERVAL = 300; //in ms
	private static final int NUM_FLASHES = 4;
	
	private PromptView pv;
	private PromptModel pm;
	private Timer flasher;
	private int flashCount = 0;
	
	public PromptController() {
		pm = new PromptModel();
		pv = new PromptView();
		pv.addController(this);
		flasher = new Timer(FLASH_INTERVAL, this);
		flasher.setRepeats(true);
		pv.newPrompt(pm.getNextPrompt());
	}
	
	public void keyPressed(KeyEvent ke) {
		switch(ke.getKeyChar()) {
		case 'n':
			String str = pm.getNextPrompt();
			if (str.startsWith("Waiting")) {
				//no flash
				pv.newPrompt(str);
				break;
			}
			flasher.start();
			if (str.startsWith("Report")) {
				pv.newPrompt(str);
				str = pm.getNextPrompt();
				pv.appendPrompt(str);
			} else {
				pv.newPrompt(str);
			}
			pm.playAlert();
			
			break;
		default:
			break;
		}
	}
	
	public void showUI() {
		pv.setVisible(true);
	}
	
	public static void main(String args[]) {
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            //Turn off metal's use of bold fonts
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            (new PromptController()).showUI();
        }
    });
}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (flashCount < NUM_FLASHES) {
			pv.flashBackground();
			flashCount++;
		} else {
			flashCount = 0;
			flasher.stop();
		}
	}

}

