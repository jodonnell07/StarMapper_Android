package com.starmapper.android.celestial;

import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;

public class ConstLine {

	private Geocentric vertex_1;
	private Geocentric vertex_2;
	
	// Constructors
	public ConstLine() {
		vertex_1 = new Geocentric();
		vertex_2 = new Geocentric();
	}
	public ConstLine(Geocentric v1, Geocentric v2) {
		vertex_1 = v1;
		vertex_2 = v2;
	}
	public ConstLine(RaDec raDec1, RaDec raDec2) {
		vertex_1 = new Geocentric(raDec1);
		vertex_2 = new Geocentric(raDec2);
	}
	
	public Geocentric getVertex1() {
		return vertex_1;
	}
	public Geocentric getVertex2() {
		return vertex_2;
	}
}
