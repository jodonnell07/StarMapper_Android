package com.starmapper.android.user;

public class Geographic {

	private float latitude;
	private float longitude;
	
	// Constructors
	public Geographic() {
		latitude = 0.0f;
		longitude = 0.0f;
	}
	public Geographic(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	
	
	// Set methods
	public void setLatitude(float lat) {
		latitude = lat;
	}
	public void setLongitude(float lon) {
		longitude = lon;
	}
	
	// Get methods
	public float getLatitude() {
		return latitude;
	}
	public float getLongitude() {
		return longitude;
	}
}
