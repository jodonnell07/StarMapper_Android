package com.starmapper.android.celestial;

import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;

public class Star {

	public String     name;
	public Geocentric geoCoords;
	public RaDec      raDec;
	public float      magnitude;
	public int		  starID;
	
	// Constructors
	public Star(RaDec argRaDec) {
		raDec = argRaDec;
		geoCoords = new Geocentric(raDec);
	}
	public Star() {
		name = "";
		geoCoords = new Geocentric();
		raDec = new RaDec();
		magnitude = 0.0f;
		starID = 0;
	}
	
	public void setName(String argName) {
		name = argName;
	}
	public void setMagnitude(float argMag) {
		magnitude = argMag;
	}
	public void setStarID(int ID) {
		starID = ID;
	}
	public void setGeocentric(Geocentric argGeocentric) {
		geoCoords = argGeocentric;
	}
	public void setUsingRaDec(RaDec argRaDec) {
		raDec = argRaDec;
		geoCoords.setUsingRaDec(argRaDec);
	}
	public Geocentric getCoords() {
		return geoCoords;
	}
}
