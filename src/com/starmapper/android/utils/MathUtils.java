package com.starmapper.android.utils;

import android.util.Log;

import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.Matrix3x3;

import android.hardware.GeomagneticField;

public abstract class MathUtils implements MathConstants {
	
	// PI constants
	public static final float PI = (float) Math.PI;
	public static final float TWOPI = 2.0f * PI;
	
	// constant conversion functions
	public static final float convertToRadians = PI / 180.0f;
	public static final float convertToDegrees = 180.0f / PI;
	
	// conversion functions
	public static final float convertRAToDegrees(float hour, float minute, float second) {
		return (hour * 15.0f + minute / 4.0f + second / 240.0f);
	}
	public static final float convertDecToDegrees(float degree, float minute, float second) {
		if(degree < 0) {
			return (degree - minute / 4.0f - second / 240.0f);
		} else {
			return (degree + minute / 4.0f + second / 240.0f); 
		}
	}
	public static final float convertRAToRadians(float hour, float minute, float second) {
		return (hour * 15.0f + minute / 4.0f + second / 240.0f) * MathUtils.convertToRadians;
	}
	public static final float convertDecToRadians(float degree, float minute, float second) {
		if(degree < 0) {
			return (degree - minute / 4.0f - second / 240.0f) * MathUtils.convertToRadians;
		} else {
			return (degree + minute / 4.0f + second / 240.0f) * MathUtils.convertToRadians;
		}
	}

	/*
	 * Trigonometry
	 * Values take radian arguments
	 * FloatMath used when possible because it's faster than Math lib
	 */
	public static float sin(float angle) {
		return (float) Math.sin(angle);
	}
	public static float cos(float angle) {
		return (float) Math.cos(angle);
	}
	public static float tan(float angle) {
		return (float) Math.sin(angle) / (float) Math.cos(angle);
	}
	public static float arcsin(float val) {
		return (float) Math.asin(val);
	}
	public static float arccos(float val) {
		return (float) Math.acos(val);
	}
	public static float arctan(float val) {
		return (float) Math.atan(val);
	}
	public static float arctan2(float val1, float val2) {
		return (float) Math.atan2(val1, val2);
	}
	public static double sind(double angle) {
		return Math.sin(angle);
	}
	public static double cosd(double angle) {
		return Math.cos(angle);
	}
	public static double tand(double angle) {
		return Math.sin(angle) / Math.cos(angle);
	}
	public static double arcsind(double val) {
		return Math.asin(val);
	}
	public static double arccosd(double val) {
		return Math.acos(val);
	}
	public static double arctand(double val) {
		return Math.atan(val);
	}
	public static double arctan2d(double val1, double val2) {
		return Math.atan2(val1, val2);
	}
	
	
	
	
	/*
	 * Vector Functions
	 */
	public static float dotProduct(Geocentric geo1, Geocentric geo2) {
		return geo1.x * geo2.x + geo1.y * geo2.y + geo1.z * geo2.z;
	}
	
	public static Geocentric crossProduct(Geocentric geo1, Geocentric geo2) {
		return new Geocentric(geo1.y * geo2.z - geo1.z * geo2.y,
							 -geo1.x * geo2.z + geo1.z * geo2.x,
							  geo1.x * geo2.y - geo1.y * geo2.x);
	}
	public static float scalarLength(Geocentric geo) {
		return (float) Math.sqrt(geo.x * geo.x + geo.y * geo.y + geo.z * geo.z);
	}
	public static Geocentric normalize(Geocentric geo) {
		float length = scalarLength(geo);
		return new Geocentric(geo.x / length, geo.y / length, geo.z / length);
	}
	public static Geocentric add(Geocentric geo1, Geocentric geo2) {
		float addx = geo1.x + geo2.x;
		float addy = geo1.y + geo2.y;
		float addz = geo1.z + geo2.z;
		
		return new Geocentric(addx, addy, addz);
	}
	public static Geocentric sub(Geocentric geo1, Geocentric geo2) {
		float subx = geo1.x - geo2.x;
		float suby = geo1.y - geo2.y;
		float subz = geo1.z - geo2.z;
		
		return new Geocentric(subx, suby, subz);
	}
	public static Geocentric scale(Geocentric geo, float scale) {
		return new Geocentric(geo.x * scale, geo.y * scale, geo.z * scale);
	}
	
	
	/*
	 * Matrix Functions
	 */
	public static Matrix3x3 multiplyMatrices(Matrix3x3 m1, Matrix3x3 m2) {
		return new Matrix3x3(m1.xx * m2.xx + m1.xy * m2.yx + m1.xz * m2.zx,
							 m1.xx * m2.xy + m1.xy * m2.yy + m1.xz * m2.zy,
							 m1.xx * m2.xz + m1.xy * m2.yz + m1.xz * m2.zz,
							 m1.yx * m2.xx + m1.yy * m2.yx + m1.yz * m2.zx,
							 m1.yx * m2.xy + m1.yy * m2.yy + m1.yz * m2.zy,
							 m1.yx * m2.xz + m1.yy * m2.yz + m1.yz * m2.zz,
							 m1.zx * m2.xx + m1.zy * m2.yx + m1.zz * m2.zx,
							 m1.zx * m2.xy + m1.zy * m2.yy + m1.zz * m2.zy,
							 m1.zx * m2.xz + m1.zy * m2.yz + m1.zz * m2.zz);
	}
	
	public static Matrix3x3 createRotationMatrix(float angle, Geocentric axis) {
		float cosAngle = MathUtils.cos(angle);
		float sinAngle = MathUtils.sin(angle);
		float oneMinusCosAngle = 1.0f - cosAngle;
		
		float x = axis.x;
		float y = axis.y;
		float z = axis.z;
		
		float x_s = x * sinAngle;
		float y_s = y * sinAngle;
		float z_s = z * sinAngle;
		
		float x_omc = x * oneMinusCosAngle;
		float y_omc = y * oneMinusCosAngle;
		float z_omc = z * oneMinusCosAngle;
		
		float xy_omc = x * y_omc;
		float yz_omc = y * z_omc;
		float zx_omc = z * x_omc;
		
		return new Matrix3x3(x * x_omc + cosAngle, xy_omc + z_s, zx_omc - y_s,
							 xy_omc - z_s, y * y_omc + cosAngle, yz_omc + x_s,
							 zx_omc + y_s, yz_omc - x_s, z * z_omc + cosAngle);
	}
	
	public static Matrix3x3 calculateLocalNorthAndUpMatrix_PhoneSpace(Geocentric acceleration, Geocentric magneticField) {
		Geocentric accel = new Geocentric(acceleration.x, acceleration.y, acceleration.z);
		Geocentric magField = new Geocentric(magneticField.x, magneticField.y, magneticField.z);
		
//		Log.d("PhoneSpace", "Raw: " + "accelX: " + String.valueOf(accel.x) + " accelY: " + String.valueOf(accel.y) + " accelZ: " + String.valueOf(accel.z));
//		Log.d("PhoneSpace", "Raw: " + "magFieldX: " + String.valueOf(magField.x) + " magFieldY: " + String.valueOf(magField.y) + " magFieldZ: " + String.valueOf(magField.z));
		
//		accel.resize(-1);
		accel = normalize(accel);
		// flipping the magField reading, since we want it pointing north, but Earth's magnetic field is from north->south
		magField.resize(-1);
		magField = normalize(magField);

//		Log.d("PhoneSpace", "Normalized: " + "accelX: " + String.valueOf(accel.x) + " accelY: " + String.valueOf(accel.y) + " accelZ: " + String.valueOf(accel.z));
//		Log.d("PhoneSpace", "Normalized: " + "magFieldX: " + String.valueOf(magField.x) + " magFieldY: " + String.valueOf(magField.y) + " magFieldZ: " + String.valueOf(magField.z));

		// The vector to Earth's magnetic North
//		float dotMagAcc = -dotProduct(magField, accel);
//		Log.d("PhoneSpace", "dotMagAcc: " + String.valueOf(dotMagAcc));
//		Geocentric scaledAccel = scale(accel, -dotProduct(magField, accel));
//		Log.d("PhoneSpace", "Testing: " + "scaledAccelX: " + String.valueOf(scaledAccel.x) + " scaledAccelY: " + String.valueOf(scaledAccel.y) + " scaledAccelZ: " + String.valueOf(scaledAccel.z));
//		Log.d("PhoneSpace", "Testing: " + "magFieldX: " + String.valueOf(magField.x) + " magFieldY: " + String.valueOf(magField.y) + " magFieldZ: " + String.valueOf(magField.z));
		
		Geocentric earthMagneticNorth = add(magField, scale(accel, -dotProduct(magField, accel)));
//		Log.d("PhoneSpace", "Testing" + "earthMagneticNorthX: " + String.valueOf(earthMagneticNorth.x) + " earthMagneticNorthY: " + String.valueOf(earthMagneticNorth.y) + " earthMagneticNorthZ: " + String.valueOf(earthMagneticNorth.z));
		earthMagneticNorth = normalize(earthMagneticNorth);
//		Log.d("PhoneSpace", "Z-Testing: " + "scaledAccelZ: " + String.valueOf(scaledAccel.z) + " magFieldZ: " + String.valueOf(magField.z) + " earthMagneticNorthZ: " + String.valueOf(scaledAccel.z + magField.z) + " normalizedEarthMagneticNorthZ: " + String.valueOf(earthMagneticNorth.z));

//		Log.d("PhoneSpace", "TestingNormalized" + "earthMagneticNorthX: " + String.valueOf(earthMagneticNorth.x) + " earthMagneticNorthY: " + String.valueOf(earthMagneticNorth.y) + " earthMagneticNorthZ: " + String.valueOf(earthMagneticNorth.z));
		
		Geocentric anti_accel = scale(accel, -1);
		Geocentric earthMagneticEast = crossProduct(earthMagneticNorth, anti_accel);
		
		return new Matrix3x3(earthMagneticNorth.x, earthMagneticNorth.y, earthMagneticNorth.z,
							         anti_accel.x,         anti_accel.y,         anti_accel.z,
							  earthMagneticEast.x,  earthMagneticEast.y,  earthMagneticEast.z);
	}
	
	public static Matrix3x3 calculateLocalNorthAndUpMatrix_CelestialSpace(Geocentric zenith, GeomagneticField geoMagField) {
		float dotZZenith = dotProduct(zenith, Z_UP_VECTOR);
		Geocentric trueZenith = add(Z_UP_VECTOR, scale(zenith, -dotZZenith));
		trueZenith = normalize(trueZenith);
		
		Geocentric trueEast = crossProduct(trueZenith, zenith);
		
		double geoDec = geoMagField.getDeclination();
		
		// Apply magnetic declination
		Matrix3x3 rotationMatrix = createRotationMatrix(geoMagField.getDeclination() * convertToRadians, zenith);
		Geocentric magneticTrueZenith = multiplyGeocentricAndMatrix3x3(rotationMatrix, trueZenith);
		Geocentric magneticTrueEast = crossProduct(magneticTrueZenith, zenith);
					
		return new Matrix3x3(magneticTrueZenith.x, zenith.x, magneticTrueEast.x,
							 magneticTrueZenith.y, zenith.y, magneticTrueEast.y,
							 magneticTrueZenith.z, zenith.z, magneticTrueEast.z);	
	}
	
	
	
	public static Geocentric multiplyGeocentricAndMatrix3x3(Matrix3x3 mat, Geocentric geo) {
		return new Geocentric(mat.xx * geo.x + mat.xy * geo.y + mat.xz * geo.z,
							  mat.yx * geo.x + mat.yy * geo.y + mat.yz * geo.z,
							  mat.zx * geo.x + mat.zy * geo.y + mat.zz * geo.z);
	}
	
	
	/*
	 * Celestial Functions
	 */
	public static double calculateTrueAnomaly(double meanAnomaly, double eccentricity, int iterations) {
		// meanAnomaly is in degrees, so convert to Rad for trig functions
		double meanAnomalyRad = meanAnomaly * convertToRadians;
		//first approximation of E (eccentric anomaly)
		double E = meanAnomaly + eccentricity * sind(meanAnomalyRad) * (1.0f + eccentricity * cosd(meanAnomalyRad));
		
  		// iterate to improve accuracy of eccentricity anomaly
		for (int i = 0; i < iterations; i++) {
			double E_Prime = E;
			double E_PrimeRad = E * convertToRadians;
			E = E_Prime - ((E_Prime - eccentricity * sind(E_PrimeRad) - meanAnomaly) / (1.0f - eccentricity * cosd(E_PrimeRad)));
		}
		double ERad = E * convertToRadians;
		
		// true anomaly (rads)
		double VRad = 2 * arctand(Math.sqrt((1 + eccentricity) / (1 - eccentricity)) * tand(ERad / 2));
//		double VRad_test = 2 * arctan2d(Math.sqrt((1.0 + eccentricity) * tand(ERad / 2) * tand(ERad / 2)), Math.sqrt(1.0 - eccentricity));
		// convert true anomaly to degrees & (0 < V < 360)
		double V = (VRad * convertToDegrees) % 360.0f;
		
		return V;			
	}
}
