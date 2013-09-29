package com.starmapper.android.celestial;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.ArrayList;

public class Constellation {

	private String name;
	// Stars in constellation
	private Set<Star> StarSet;
	// Constellation Lines (separate lines each connecting two stars)
	private ArrayList<ConstLine> ConstLines;
		
	// Constructors
	public Constellation() {
		StarSet = new LinkedHashSet<Star>();
		ConstLines = new ArrayList<ConstLine>();
	}
	
	public void addName(String argName) {
		name = argName;
	}
	
	public void addStar(Star argStar) {
		StarSet.add(argStar);
	}
	
	public int getNumStars() {
		return StarSet.size();
	}
	
	public Set<Star> getStars() {
		return StarSet;
	}
	
	public void addLine(ConstLine line) {
		ConstLines.add(line);
	}
	
	public ArrayList<ConstLine> getLines() {
		return ConstLines;
	}
	
	public int getNumLines() {
		return ConstLines.size();
	}
}
