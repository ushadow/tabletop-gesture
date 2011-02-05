package edu.mit.yingyin.tabletop.recognition;

public interface GestureEventListener {
	public void gestureRecognized(GestureEvent ge);
	public void gestureEnded(GestureEvent ge);
}
