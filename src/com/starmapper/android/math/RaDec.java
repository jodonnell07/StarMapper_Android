package com.starmapper.android.math;

public class RaDec {
	
	// Coordinates in degrees
	public float ra;		// 0 < 360
	public float dec;		// -90 < 90
	
	// Constructors
	public RaDec() {
		set(0,0);
	}
	public RaDec(float argRa, float argDec) {
		set(argRa,argDec);
	}
	public RaDec(int argRaHour, int argRaMinute, int argRaSecond, int argDecDegree, int argDecMinute, int argDecSecond) {
		ra = (float) argRaHour   * (float) 15 +
			 (float) argRaMinute / (float) 4  +
			 (float) argRaSecond / (float) 240;
		dec = (float) argDecDegree +
			  (float) argDecMinute / (float) 60 +
			  (float) argDecSecond / (float) 3600;
	}

	// Set methods
	public void set(float argRa, float argDec) {
		ra = argRa;
		dec = argDec;
	}
	
	// Get methods
	public float getRA() {
		return ra;
	}
	public float getDec() {
		return dec;
	}
}
