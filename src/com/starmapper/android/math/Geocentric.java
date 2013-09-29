package com.starmapper.android.math;

import com.starmapper.android.utils.MathUtils;

public class Geocentric {

	// Coordinates
	public float x;
	public float y;
	public float z;
	
	// Constructors
	public Geocentric() {
		set(0,0,0);
	}
	public Geocentric(float argx, float argy, float argz) {
		set(argx, argy, argz);
	}
	public Geocentric(RaDec raDec) {
		setUsingRaDec(raDec);
	}
	public Geocentric(Geocentric geo) {
		set(geo.x, geo.y, geo.z);
	}
	
	public void set(float argx, float argy, float argz) {
		x = argx;
		y = argy;
		z = argz;
	}
	public void setUsingRaDec(RaDec raDec) {
		float raRad = raDec.ra * MathUtils.convertToRadians;
		float decRad = raDec.dec * MathUtils.convertToRadians;
		x = MathUtils.cos(raRad) * MathUtils.cos(decRad);
		y = MathUtils.sin(raRad) * MathUtils.cos(decRad);
		z = MathUtils.sin(decRad);
	}
	public void resize(float size) {
		x = x * size;
		y = y * size;
		z = z * size;
	}
}
