package com.starmapper.android.grid;

import java.util.ArrayList;

import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;

public class GridLine {

	public ArrayList<Geocentric> vertices;
	public ArrayList<RaDec> raDec_vertices;
	
	// Constructors
	public GridLine() {
		vertices = new ArrayList<Geocentric>();
		raDec_vertices = new ArrayList<RaDec>();
	}
	public GridLine(RaDec argRaDec) {
		raDec_vertices = new ArrayList<RaDec>();
		vertices = new ArrayList<Geocentric>();
		raDec_vertices.add(argRaDec);
		vertices.add(new Geocentric(argRaDec));
	}
	
	public ArrayList<Geocentric> getVertices() {
		return vertices;
	}
}
