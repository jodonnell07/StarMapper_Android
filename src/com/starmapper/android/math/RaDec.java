package com.starmapper.android.math;

public class RaDec {
	
	// Coordinates in degrees
	public float ra;
	public float dec;
	
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

	public void set(float argRa, float argDec) {
		ra = argRa;
		dec = argDec;
	}
}
