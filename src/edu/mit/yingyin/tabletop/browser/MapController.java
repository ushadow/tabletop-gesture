 package edu.mit.yingyin.tabletop.browser;

import javax.swing.SwingUtilities;
import javax.vecmath.Vector3f;

import org.jdesktop.jdic.browser.WebBrowser;

import edu.mit.yingyin.tabletop.environment.EnvConstants;
import edu.mit.yingyin.tabletop.handtracking.TrackerInterface;
import edu.mit.yingyin.tabletop.recognition.GestureEvent;
import edu.mit.yingyin.tabletop.recognition.GestureEventListener;


public class MapController implements GestureEventListener {
	private static final int WAIT_TIME = 3000;
	private static final int ZOOM_STEP = 200;
	private static final float SCALE = (float) Math.sqrt(640 * 640 + 480 * 480);
	
	private WebBrowser webBrowser;
	private TrackerInterface tracker;
	
	public MapController(WebBrowser webBrowser, TrackerInterface tracker) {
		this.webBrowser = webBrowser;
		this.tracker = tracker;
	}
	
	/**
	 * 
	 * @param dx change of the lookat position by the camera in x direction (to the east) in local frame
	 * @param dy change of the lookat position by the camera in y direction (to the north) in local frame
	 * @param length
	 */
	public void panBy(float dx, float dy, float length) { 
		webBrowser.executeScript("panBy(" + dx + "," + dy + "," + length + ")");
	}
	
	public void tiltBy(int angle) {
		webBrowser.executeScript("tiltBy(" + angle + ")");
	}
	
	public void rotateBy(int angle) {
		webBrowser.executeScript("rotateBy(" + angle + ")");
	}
	
	public void zoomBy(int distance) {
		webBrowser.executeScript("zoomBy(" + distance + ")");
	}
	
	public void gestureRecognized(GestureEvent ge) {
		if(ge == null)
			return;
		
		//SwingUtilities.invokeLater(new GestureEventHandler(ge));
		(new GestureEventHandler(ge)).run();
	}
	
	public void gestureEnded(GestureEvent ge) {
		if(ge == null)
			return;
		
		SwingUtilities.invokeLater(new GestureEventHandler(ge));
	}	

	private class GestureEventHandler extends Thread {
		private GestureEvent ge;
		
		public GestureEventHandler(GestureEvent ge) {
			super();
			this.ge = ge;
		}
		
		public void run() {
			String name = ge.toString();
			if (name.startsWith("pan")) {
				//world coordinate frame:
				//x-axis: downward in the tabletop plane
				//y-axis: to the right in the tabletop plane
				//z_axis: vertical and coming out of the tabletop plane
				// To change the vector in this frame to a frame relative to the local frame in the earth:
				//	(0 1; -1, 0)(dx, dy)
				Vector3f translation = ge.getTranslation();
				float length = (float)Math.sqrt(translation.x * translation.x + translation.y * translation.y);
				if (length < EnvConstants.EPS)
					return;
				float dx = translation.x / length;
				float dy = translation.y / length;
				length = length / SCALE;
				System.out.println(dx + " " + dy + " " + length);
//				if(length<0.1)
//					return;
				//The movement of the camera is opposite to the movement of the map
				panBy(-dy, dx, length);
				resetBackground();
				return;
			}
			if (name.startsWith("rotx")) {
				tiltBy(-ge.getYRotation());
				resetBackground();
				return;
			}
			if (name.startsWith("rotz")) {
				rotateBy(-ge.getZRotation());
				resetBackground();
				return;
			}
			if (name.startsWith("zoom")) {
				int distance;
				if (name.endsWith("in"))
					distance = -ZOOM_STEP;
				else distance = ZOOM_STEP;
				
				zoomBy(distance);
				resetBackground();
				return;
				
			}
		}
		
		private void resetBackground() {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tracker.setBackground();
		}
	}
}
