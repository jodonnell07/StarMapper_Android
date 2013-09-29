package com.starmapper.android.celestial;

import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;

public class Planet {

	private String     name;
	private Geocentric geoCoords;
	private RaDec      raDec;
	
	private double	  mDistanceToEarth;
	public float	  mScale;
	
	private int		  mPlanetIdx;
	
	// Constructors
	public Planet() {
		name = "";
		geoCoords = new Geocentric();
		raDec = new RaDec();
	}
	
	public Planet(String name) {
		this.name = name;
	}
	
	public Planet(float argRa, float argDec) {
		name = "";
		raDec = new RaDec(argRa, argDec);
		geoCoords = new Geocentric(raDec);
	}
	
	public Planet(String name, RaDec argRaDec, double distance, int idx, float scale) {
		this.name = name;
		raDec = argRaDec;
		geoCoords = new Geocentric(raDec);
		mDistanceToEarth = distance;
		mPlanetIdx = idx;
		mScale = scale;
	}
	
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Geocentric getCoords() {
		return geoCoords;
	}
	
	public void setCoords(float argRa, float argDec) {
		raDec = new RaDec(argRa, argDec);
		geoCoords.setUsingRaDec(raDec);
	}
	
	public void setDistance(float distance) {
		mDistanceToEarth = distance;
	}
	
	public double getDistance() {
		return mDistanceToEarth;
	}
	
	public void setIdx(int idx) {
		mPlanetIdx = idx;
	}
	
	public int getIdx() {
		return mPlanetIdx;
	}
	
	public void setScale(float scale) {
		mScale = scale;
	}
	
	public float getScale() {
		return mScale;
	}

}
