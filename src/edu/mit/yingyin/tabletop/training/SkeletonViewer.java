package edu.mit.yingyin.tabletop.training;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.tabletop.environment.LeftGloveTrackingExample;
import edu.mit.yingyin.tabletop.environment.SingleCameraCalibrationExample;
import edu.mit.yingyin.util.QuatUtil;

import rywang.util.ObjectIO;
import skinning.SkeletonState;
import skinning.examples.SkinningExample;
import skinning.examples.SkinningExample.SSDSkinner;
import handtracking.TrackingExample;
import handtracking.camera.CustomCameraRenderer;
import handtracking.camera.geometriccalibration.MatlabCameraCalibration;

@SuppressWarnings("restriction")
public class SkeletonViewer {
	
	private static final String SKELETON_SEQ_FILE_FORMAT = EnvConstants.SKELETON_SEQ_FILE;
	private static final String GESTURE_NAMES_FILE_PATH = EnvConstants.MAIN_FOLDER + "data/Gesture/Gestures.txt";
	private static final String GESTURE_SEQ_FILE_PATH = EnvConstants.MAIN_FOLDER + "data/Gesture/gesture_sequence.txt";
	
	private CustomCameraRenderer renderer;
	private SSDSkinner skinner;
	private List<String> gestures = new ArrayList<String>();

	private JFrame frame;
	private DefaultListModel listModel = new DefaultListModel();;
	private JList labelList;
	
	public SkeletonViewer() {
		TrackingExample example = LeftGloveTrackingExample.getInstance();
		SkinningExample skinningExample = example.getSkinningExample();
		
		File gestureNamesFile = new File(GESTURE_NAMES_FILE_PATH);
		try {
			Scanner scanner = new Scanner(gestureNamesFile);
			while(scanner.hasNext())
				gestures.add(scanner.next());
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		MatlabCameraCalibration calibration = SingleCameraCalibrationExample.getInstance().getGeometricCameraCalibration();

		renderer = new CustomCameraRenderer(null);
    frame = renderer.start(calibration);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    if(frame.getContentPane().getComponentCount() >= 1) {
    	Component component = frame.getContentPane().getComponent(0);
    	component.addKeyListener(new Controller()); 
    }
    frame.setLocation(0, 0);
    
    JFrame labelFrame = new JFrame("Labels");
    labelList = new JList(listModel);
    labelList.setPreferredSize(new Dimension(200, 600));
    labelFrame.getContentPane().add(labelList);
    labelFrame.pack();
    labelFrame.setLocation(frame.getWidth(), 0);
    labelFrame.setVisible(true);	      

    skinner = new SSDSkinner(skinningExample);
	}
	
	private class Controller implements KeyListener {
		private int currentIndex = 0, fileIndex = 0;
		private String inputFile;
		private Vector3f prevPos, currPos;
	  private List<SkeletonState> states;
	  private List<List<String> > gestureSeq = new ArrayList<List<String>>();
	  private List<String> labels = new Vector<String>();
	  private List<Integer> indices = new Vector<Integer>();
	  
		@Override
		public void keyPressed(KeyEvent ke) {
			switch(ke.getKeyChar()) {
			case 'd':
			  //load gesture sequences
			  File gestureSeqFile = new File(GESTURE_SEQ_FILE_PATH);
		    try {
		      Scanner scanner = new Scanner(gestureSeqFile);
		      int index = 0;
		      gestureSeq.add(new ArrayList<String>());
		      while(scanner.hasNextLine()) {
		        String s = scanner.nextLine();
		        if (s.length() != 0)
		          gestureSeq.get(index).add(s);
		        else {
		          index++;
		          gestureSeq.add(new ArrayList<String>());
		        }
		      }
		      System.out.println("load gesture sequences.");
		    } catch (FileNotFoundException e1) {
		      // TODO Auto-generated catch block
		      e1.printStackTrace();
		    }
		    break;
			case 'e':
			  /* record the index without label */
			  indices.add(currentIndex);
			  updateList();
			  break;
			case 'f':
			  /* output the labeling result to the file */
			  String fileName = inputFile.replaceAll(".ss", ".lab");
			  try {
			    FileOutputStream fos = new FileOutputStream(fileName);
			    PrintStream ps = new PrintStream(fos);
			    for(int i = 0; i < listModel.size(); i++)
			      ps.println(listModel.elementAt(i));
			    ps.close();
			    System.out.println("Saved file to " + fileName);
			  } catch (FileNotFoundException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			  }
			  break;
			case 'i':
			  indices.remove(indices.size() - 1);
			  updateList();
			  break;
			case 'l':
			  labels.remove(labels.size() - 1);
			  updateList();
			  break;
			case 'n':
			  if (states == null) {
			    System.err.println("states is empty, please read a file by pressing 'o'.");
			    break;
			  }
				currentIndex++;
				if(currentIndex == states.size())
					currentIndex = 0;
				updateState();
				break;
			case 'o':
			  //open next file
			  currentIndex = 0;
			  inputFile = String.format(SKELETON_SEQ_FILE_FORMAT, (++fileIndex));
			  states = getSkeletonStates(inputFile);
			  System.out.println("read " + inputFile);
			  labels.clear();
			  indices.clear();
			  if (!gestureSeq.isEmpty()) {
			    for (String gesture : gestureSeq.get(fileIndex - 1))
			      labels.add(gesture);
			    updateList();
			  }
			  break;
			case 'p':
				currentIndex--;
				if(currentIndex < 0)
					currentIndex = states.size() - 1;
				updateState();
				break;
			case 's':
				/* record the start of a gesture with label */
				String gesture = (String)JOptionPane.showInputDialog(
	                    frame,
	                    "Choose the gesture:",
	                    "Input Gesture",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    gestures.toArray(),
	                    null);
				if (gesture != null) {
					labels.add(gesture);
					indices.add(currentIndex);
					updateList();
				}
				break;
			default:
				break;
					
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		private void updateState() {
			SkeletonState state = states.get(currentIndex);
			renderer.setSkin(skinner.skin(state));
			System.out.println("currentIndex = " + currentIndex);

			prevPos = currPos;
			currPos = state.getTranslation(0);
			if(prevPos != null) {
				Vector3f diff = new Vector3f(currPos);
				diff.sub(prevPos);
				System.out.println("Translation = " + diff);
			}
			
			Quat4f q = state.getRotation(0);
			Tuple3f t = QuatUtil.getEulerAngles(q);
			System.out.println("Rotation = " + t);
		}
		
		private void updateList() {
			listModel.clear();

			Iterator<String> itString = labels.iterator();
			Iterator<Integer>	itInt = indices.iterator();
			
			while (itString.hasNext()) {
				StringBuffer element = new StringBuffer(itString.next());
				if (itInt.hasNext())
					element.append(" " + itInt.next());
				if (itInt.hasNext())
					element.append(" " + itInt.next());
				listModel.addElement(element);
			}
			
			labelList.updateUI();
		}
		
		private List<SkeletonState> getSkeletonStates(String fileName) {
		  try {
        return (List<SkeletonState>)ObjectIO.readObject(fileName);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      return null;
		}
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
		
			public void run() {
				new SkeletonViewer();
			}
		}
		);
	}
}
