package edu.mit.yingyin.tabletop.recognition;


import java.util.List;

import javax.vecmath.Vector3f;

import edu.mit.yingyin.tabletop.handtracking.MyProcessor;

import skinning.SkeletonState;

public class PanRecognizer extends GestureRecognizer
{
	Vector3f initPos = null;
	int counter = 0;
	List<MyProcessor> processors;
	int initCounter = 0;
		
	public PanRecognizer(List<MyProcessor> processors)
	{
		this.processors = processors;
	}

	
	@Override
	public GestureEvent classify(SkeletonState ss)
	{
		if(initCounter <= 20)
		{
			if(initCounter++ == 19)
			{
				for(MyProcessor processor : processors)
					processor.setBackground();
			}
			
			return null;
		}
		GestureEvent ge = null;
		
		Vector3f curPos = ss. getTranslation(0);
		
		if(counter == 0)
		{
			initPos = curPos;
			//ge = new GestureEvent("start",initPos);
		}
		else if (counter == 5)
			{
				Vector3f diff = new Vector3f(curPos);
				diff.sub(initPos);
				ge = new GestureEvent("pan", ss);
				initCounter = 0;
			}
		
		if((++counter)>5)
			counter = 0;
		return ge;
	}
	
	public static void main(String args[])
	{
		int counter = 0;
		
		if(counter++ >= 1)
			System.out.println("1st:" + counter);
		
		System.out.println("2nd:" + counter);
	}

}
