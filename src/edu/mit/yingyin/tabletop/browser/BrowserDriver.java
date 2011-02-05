package edu.mit.yingyin.tabletop.browser;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.tabletop.handtracking.TrackerInterface;
import edu.mit.yingyin.tabletop.handtracking.TrackerMultithreaded;
import edu.mit.yingyin.tabletop.handtracking.TrackerSimulator;


public class BrowserDriver
{
	private static final boolean SIMULATE = EnvConstants.BROWSERDRIVER_SIMULATE;
	
	private BrowserFrame browserFrame;
	private TrackerInterface tracker;
	private MapController mapController;

	public BrowserDriver() {
		browserFrame = new BrowserFrame(SIMULATE);
	
    browserFrame.addWindowListener(new WindowAdapter() {
    	@Override
      public void windowClosing(WindowEvent e) {
        // Run this on another thread than the AWT event queue
        new Thread(new Runnable() {
          public void run() {
            if(tracker != null)
          	  tracker.stop();
            System.exit(0);
          }
        }).start();
    	}
    });
      
    try {
      if(SIMULATE)
				tracker = new TrackerSimulator();
			else tracker = new TrackerMultithreaded();
			
			mapController = new MapController(browserFrame.getWebBrowser(), tracker);
			tracker.addGestureEventListener(mapController);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run() {
					browserFrame.pack();
					browserFrame.setVisible(true);
				}
			});
			Thread.sleep(15000);
			
			//tracker is not started in the event dispatching thread
			tracker.start();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		new BrowserDriver();
	}
	
}
