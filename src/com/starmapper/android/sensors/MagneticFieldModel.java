package com.starmapper.android.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.Matrix3x3;
import com.starmapper.android.program.StarMapperRenderer;
import com.starmapper.android.user.User;
import com.starmapper.android.utils.MathUtils;

public class MagneticFieldModel implements SensorEventListener, MathConstants {

	private StarMapperRenderer mRenderer;
	
	private User mUser;
	
	private Geocentric mNewMagneticFieldReading;
	private GLSurfaceView mGLSurfaceView;
	
	// Smoother Variables
	private float alpha;
	private int   exponent;
	private float[] last;
	private float[] current;
	
	// Constructors
	public MagneticFieldModel(User user, StarMapperRenderer renderer, GLSurfaceView GLview) {
		mUser = user;
		mRenderer = renderer;
		mGLSurfaceView = GLview;
		mNewMagneticFieldReading = new Geocentric(0.0f, 0.0f, 0.0f);
		
		alpha = 0.05f;
		exponent = 3;
		last = new float[3];
		current = new float[3];
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//smoothing code
		for (int i = 0; i < 3; ++i) {
			last[i] = current[i];
			float diff = event.values[i] - last[i];
			float correction = diff * alpha;
			for (int j = 1; j < exponent;  ++j) {
				correction *= Math.abs(diff);
			}
			if (correction > Math.abs(diff) || correction < -Math.abs(diff)) {
				correction = diff;
			}
			current[i] = last[i] + correction;
		}

		mNewMagneticFieldReading.x = -current[0];
		mNewMagneticFieldReading.y = -current[1];
		// This value is negative because the z-value is opposite in direction to looking in the phone
		mNewMagneticFieldReading.z = -current[2];
		
		mUser.setMagneticField(mNewMagneticFieldReading);
		
		Matrix3x3 phoneSpaceMatrix = mUser.getLocalNorthAndUpMatrix_PhoneSpace();
		Matrix3x3 celestialSpaceMatrix = mUser.getLocalNorthAndUpMatrix_CelestialSpace();
		Matrix3x3 viewTransform = MathUtils.multiplyMatrices(celestialSpaceMatrix, phoneSpaceMatrix);
		
		Geocentric lookVector = MathUtils.multiplyGeocentricAndMatrix3x3(viewTransform, Z_DOWN_VECTOR);
		Geocentric upVector   = MathUtils.multiplyGeocentricAndMatrix3x3(viewTransform, INIT_UP_VECTOR);
		
		mUser.lookDir = lookVector;
		mUser.lookNormal = upVector;
		
		mRenderer.mLookX = mUser.getLookX();
		mRenderer.mLookY = mUser.getLookY();
		mRenderer.mLookZ = mUser.getLookZ();
		mRenderer.mUpX   = mUser.getNormalX();
		mRenderer.mUpY   = mUser.getNormalY();
		mRenderer.mUpZ   = mUser.getNormalZ();
		
//		Log.d("MagneticFieldModel", "INSIDE MAGNETICFIELD SENSORCHANGED");
//		Log.d("MagneticFieldModel", "lastX: " + String.valueOf(last[0]) + " lastY: " + String.valueOf(last[1]) + " lastZ: " + String.valueOf(last[2]));
		Log.d("MagneticFieldModel", "currentX: " + String.valueOf(current[0]) + " currentY: " + String.valueOf(current[1]) + " currentZ: " + String.valueOf(current[2]));
//		Log.d("MagneticFieldModel", "mLookX: " + String.valueOf(lookVector.x) + " mLookY: " + String.valueOf(lookVector.y) + " mLookZ: " + String.valueOf(lookVector.z));
		
		//mGLSurfaceView.requestRender();
	}
}
