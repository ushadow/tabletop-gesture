package edu.mit.yingyin.tabletop.environment;

public class RecognizerExample
{
	/**
	 * 
	 * @return path to the folder that contains the parameters of the HMM models
	 */
	public static String getHmmParamsFileName()
	{
		return StandardEnvironment.getOSD() + "/data/HandTracking/Training/MatlabData/10_21/";
	}
}
