package edu.mit.yingyin.tabletop.recognition;

import java.util.ArrayList;
import java.util.List;

import skinning.SkeletonState;

public abstract class GestureRecognizer
{
	protected List<GestureEventListener> gelList = new ArrayList<GestureEventListener>();
	
	abstract public GestureEvent classify(SkeletonState ss);
	
	public void addGestureEventListener(GestureEventListener gel) {
		gelList.add(gel);
	}
}
