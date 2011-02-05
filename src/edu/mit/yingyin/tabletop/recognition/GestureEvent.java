package edu.mit.yingyin.tabletop.recognition;

import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import edu.mit.yingyin.utils.QuatUtil;

import skinning.BasicSkeletonState;
import skinning.SkeletonState;

public class GestureEvent {
	private String name;
	private SkeletonState startState, endState;

	//constructor
	public GestureEvent(String name) {
		this.name = name;
	}
	
	public GestureEvent(String name, SkeletonState state) {
		this.name = name;
	}
	
	public GestureEvent(String name, SkeletonState startState, SkeletonState endState) {
		this.name = name;
		this.startState = new BasicSkeletonState(startState);
		this.endState = new BasicSkeletonState(endState);
	}
	
	public String getName() {
		return name;
	}
	
	public void setEndState(SkeletonState state) {
		this.endState = new BasicSkeletonState(state);
	}
	
	public Vector3f getTranslation() {
		Vector3f translation = new Vector3f();
		translation.sub(endState.getTranslation(0), startState.getTranslation(0));
		return translation;
	}
	
	public int getYRotation() {
		Tuple3f t = QuatUtil.getEulerAngles(startState.getRotation(0));
		float startTiltAngle = t.y;
		t = QuatUtil.getEulerAngles(endState.getRotation(0));
		float endTiltAngle = t.y;
		return (int)((endTiltAngle - startTiltAngle) / Math.PI * 180);
	}
	
	public int getZRotation() {
		Tuple3f t = QuatUtil.getEulerAngles(startState.getRotation(0));
		float startAngle = t.z;
		t = QuatUtil.getEulerAngles(endState.getRotation(0));
		float endAngle = t.z;
		return (int)((endAngle - startAngle) / Math.PI * 180);
	}
	
	public String toString() {
		return name;
	}
}
