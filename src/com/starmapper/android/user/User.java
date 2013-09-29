package com.starmapper.android.user;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.hardware.GeomagneticField;

import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.constants.OrbitalElementsConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.Matrix3x3;
import com.starmapper.android.math.RaDec;
import com.starmapper.android.utils.MathUtils;

public class User implements MathConstants, OrbitalElementsConstants {

	public Geocentric lookDir;
	public Geocentric lookNormal;
	
	public Geocentric currentAcceleration;
	public Geocentric currentMagneticField;
	
	public Geographic geoLocation;
	
	public Geocentric mZenith;
	
	public Matrix3x3 mLocalNorthAndUpMatrix_PhoneSpace;
	public Matrix3x3 mLocalNorthAndUpMatrix_CelestialSpace;
	
	public GeomagneticField geoMagField;
	
	// Constructors
	public User() {
		lookDir = new Geocentric(INIT_LOOK_VECTOR);
		lookNormal = new Geocentric(INIT_UP_VECTOR);
		currentAcceleration = new Geocentric(INIT_ACCEL);
		currentMagneticField = new Geocentric(INIT_MAG_FIELD);
	}
	
	// Set methods
	public void setLookDir(Geocentric newLook) {
		lookDir = newLook;
	}
	public void setLookNormal(Geocentric newNormal) {
		lookNormal = newNormal;
	}
	public void setAcceleration(Geocentric accel) {
		currentAcceleration = accel;
		updateLocalNorthAndUpMatrix_PhoneSpace();
	}
	public void setMagneticField(Geocentric magField) {
		currentMagneticField = magField;
		updateLocalNorthAndUpMatrix_PhoneSpace();
	}
	public void setLookDir(float argx, float argy, float argz) {
		lookDir.set(argx, argy, argz);
	}
	public void setLookNormal(float argx, float argy, float argz) {
		lookNormal.set(argx, argy, argz);
	}
	public void setGeoLocation(float lat, float lon) {
		geoLocation = new Geographic(lat, lon);
	}
	public void setGeomagneticField() {
		geoMagField = new GeomagneticField(geoLocation.getLatitude(), geoLocation.getLongitude(), 0, System.currentTimeMillis());
	}
	
	// Get methods
	public Geocentric getLookDir() {
		return lookDir;
	}
	public Geocentric getLookNormal() {
		return lookNormal;
	}
	public Geocentric getAcceleration() {
		return currentAcceleration;
	}
	public Geocentric getMagneticField() {
		return currentMagneticField;
	}
	public float getLookX() {
		return lookDir.x;
	}
	public float getLookY() {
		return lookDir.y;
	}
	public float getLookZ() {
		return lookDir.z;
	}
	public float getNormalX() {
		return lookNormal.x;
	}
	public float getNormalY() {
		return lookNormal.y;
	}
	public float getNormalZ() {
		return lookNormal.z;
	}
	
	// Sensor-related methods
	public Matrix3x3 getLocalNorthAndUpMatrix_PhoneSpace() {
		return mLocalNorthAndUpMatrix_PhoneSpace;
	}
	public Matrix3x3 getLocalNorthAndUpMatrix_CelestialSpace() {
		return mLocalNorthAndUpMatrix_CelestialSpace;
	}
	
	// Location-related methods
	public void setZenith() {
		
		long millisSinceEpoch = System.currentTimeMillis();
		Date dateSinceEpoch = new Date(millisSinceEpoch);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTime(dateSinceEpoch);
		double hour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0f + cal.get(Calendar.SECOND) / 3600.0f;
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		double jd = 367.0 * year - Math.floor(7.0 * (year + Math.floor((month + 9.0) / 12.0)) / 4.0) + Math.floor(275.0 * month / 9.0) + day + 1721013.5 + hour / 24.0;
		double delta = jd - 2451545.0f;
		
		// taken from http://aa.usno.navy.mil/faq/docs/SunApprox.php
		double globalSiderealTime = (280.461 + 360.98564737 * delta) % 360.0;
		double localSiderealTime = globalSiderealTime + geoLocation.getLongitude();
		if (localSiderealTime > 360.0) {
			localSiderealTime %= 360.0;
		} else if (localSiderealTime < 0.0) {
			localSiderealTime += 360.0;
		}
		 
		mZenith = new Geocentric(new RaDec((float) localSiderealTime, geoLocation.getLatitude()));
		
		updateLocalNorthAndUpMatrix_CelestialSpace();
	}
	
	public void updateLocalNorthAndUpMatrix_PhoneSpace() {
		mLocalNorthAndUpMatrix_PhoneSpace = MathUtils.calculateLocalNorthAndUpMatrix_PhoneSpace(currentAcceleration, currentMagneticField);
	}
	
	public void updateLocalNorthAndUpMatrix_CelestialSpace() {
		mLocalNorthAndUpMatrix_CelestialSpace = MathUtils.calculateLocalNorthAndUpMatrix_CelestialSpace(mZenith, geoMagField);
	}
}
