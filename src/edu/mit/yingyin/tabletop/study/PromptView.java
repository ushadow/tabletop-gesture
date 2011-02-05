package edu.mit.yingyin.tabletop.study;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

public class PromptView extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5890109840660247623L;
	private static final int WINDOW_WIDTH = 50;
	private static final int WINDOW_HEIGHT = 100;
	private static final int TEXT_AREA_ROWS_NUM = 20;
	private static final int TEXT_AREA_COLUNMS_NUM = 20;
	private static final int TEXT_AREA_FIONT_SIZE = 20;
	private static final Color FLASH_COLOR = Color.CYAN;
	
	private JTextArea textArea;
	private int flashState = 0;
	
	public PromptView() {
		super("Command Center Prompt");
		initComponent();
		pack();
	}
	
	private void initComponent() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); 
		setLocation(dim.width - getWidth(), dim.height - getHeight());
		textArea = new JTextArea();
    textArea.setColumns(TEXT_AREA_COLUNMS_NUM);
    textArea.setRows(TEXT_AREA_ROWS_NUM);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);
    textArea.setFont(new Font("Serif", Font.BOLD, TEXT_AREA_FIONT_SIZE));
    
    getContentPane().add(textArea);
	}

	public void newPrompt(String prompt) {
		int lineCount = textArea.getLineCount();
		try {
			int end = textArea.getLineEndOffset(lineCount - 1);
			textArea.replaceRange(prompt, 0, end);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void appendPrompt(String prompt) {
		textArea.append(prompt);
	}
	
	public void addController(PromptController promptController) {
		textArea.addKeyListener(promptController);
	}
	
	public void flashBackground() {
		if (flashState == 0) {
			textArea.setBackground(FLASH_COLOR);
			flashState =1;
		} else {
			textArea.setBackground(Color.WHITE);
			flashState = 0;
		}
		repaint();
	}
}
