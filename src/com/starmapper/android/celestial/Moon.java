package com.starmapper.android.celestial;

import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;

public class Moon {

	private Geocentric  geoCoords;
	private RaDec		raDec;
	
	public float mScale;
	
	// Constructors
	public Moon() {
		geoCoords = new Geocentric();
		raDec = new RaDec();
	}
	
	public Moon(float argRa, float argDec, float scale) {
		raDec = new RaDec(argRa, argDec);
		geoCoords = new Geocentric(raDec);
		mScale = scale;
	}
	
	
	
	public Geocentric getCoords() {
		return geoCoords;
	}
	
	public float getScale() {
		return mScale;
	}
}
