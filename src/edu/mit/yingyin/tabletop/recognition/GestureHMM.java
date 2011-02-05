package edu.mit.yingyin.tabletop.recognition;

import com.jmatio.types.MLStructure;

public class GestureHMM extends GaussHMM implements Comparable<GestureHMM> {

	private static final boolean HAS_END_STATE = false;
	private String name;
	
	public GestureHMM(String name, MLStructure hmm) {
		super(hmm, HAS_END_STATE);
		this.name = name;
	}

	@Override
	public int compareTo(GestureHMM other) {
		if (other == null)
			throw new NullPointerException();
		
		if (this.getLoglik() == other.getLoglik())
			return 0;
		if (this.getLoglik() > other.getLoglik())
			return 1;
		return -1;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		String s = "\nname : " + name;
		return s;
	}
}
