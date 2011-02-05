package edu.mit.yingyin.tabletop.handtracking;

import edu.mit.yingyin.tabletop.recognition.GestureEventListener;

public interface TrackerInterface
{
	public void start();
	public void stop();
	public void addGestureEventListener(GestureEventListener gel);
	public void setBackground();
}
