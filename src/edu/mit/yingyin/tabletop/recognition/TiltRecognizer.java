package edu.mit.yingyin.tabletop.recognition;

import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;

import edu.mit.yingyin.util.QuatUtil;

import skinning.SkeletonState;

public class TiltRecognizer extends GestureRecognizer
{
	private int counter = 0;
	private float initAngle = 0;
	
	@Override
	public GestureEvent classify(SkeletonState ss)
	{
		counter++;
		
		if(counter <= 20)	
			return null;
		
		Quat4f q = ss.getRotation(0);
		Tuple3f t = QuatUtil.getEulerAngles(q);
		
		if(counter == 21)
		{
			initAngle = t.y;
			return null;
		}
		if(counter == 26)
		{
			int tiltAngle = (int) ((initAngle-t.y)/Math.PI * 180);
			counter = 0;
			return new GestureEvent("tilt", ss);
		}
		
		return null;
	}

}
