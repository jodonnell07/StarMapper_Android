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

public class AccelerometerModel implements SensorEventListener, MathConstants {

	private StarMapperRenderer mRenderer;
	
	private User mUser;
	
	private Geocentric mNewAccelerationReading;
	private GLSurfaceView mGLSurfaceView;
	
	// Smoother variables
	private float alpha;
	private int   exponent;
	private float[] last;
	private float[] current;
	
	// Constructors
	public AccelerometerModel(User user, StarMapperRenderer renderer, GLSurfaceView GLview) {
		mUser = user;
		mRenderer = renderer;
		mGLSurfaceView = GLview;
		mNewAccelerationReading = new Geocentric(0.0f, 0.0f, 0.0f);
		
		alpha = 0.7f;
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
			
		mNewAccelerationReading.x = -current[0];
		mNewAccelerationReading.y = -current[1];
		mNewAccelerationReading.z = -current[2];
		
		mUser.setAcceleration(mNewAccelerationReading);
		
		Matrix3x3 phoneSpaceMatrix = mUser.getLocalNorthAndUpMatrix_PhoneSpace();
		Matrix3x3 celestialSpaceMatrix = mUser.getLocalNorthAndUpMatrix_CelestialSpace();
		Matrix3x3 viewTransform = MathUtils.multiplyMatrices(celestialSpaceMatrix, phoneSpaceMatrix);
		
		Geocentric lookVector = MathUtils.multiplyGeocentricAndMatrix3x3(viewTransform, Z_DOWN_VECTOR);
		Geocentric upVector   = MathUtils.multiplyGeocentricAndMatrix3x3(viewTransform, INIT_UP_VECTOR);
		
//		Log.d("AccelerometerModel", "INSIDE ACCELEROMETER SENSORCHANGED");
		Log.d("AccelerometerModel", "ps_xx: " + String.valueOf(phoneSpaceMatrix.xx) + " ps_xy: " + String.valueOf(phoneSpaceMatrix.xy) + " ps_xz: " + String.valueOf(phoneSpaceMatrix.xz));
		Log.d("AccelerometerModel", "ps_yx: " + String.valueOf(phoneSpaceMatrix.yx) + " ps_yy: " + String.valueOf(phoneSpaceMatrix.yy) + " ps_yz: " + String.valueOf(phoneSpaceMatrix.yz));
		Log.d("AccelerometerModel", "ps_zx: " + String.valueOf(phoneSpaceMatrix.zx) + " ps_zy: " + String.valueOf(phoneSpaceMatrix.zy) + " ps_zz: " + String.valueOf(phoneSpaceMatrix.zz));


//		Log.d("AccelerometerModel", "vt_xx: " + String.valueOf(viewTransform.xx) + " vt_xy: " + String.valueOf(viewTransform.xy) + " vt_xz: " + String.valueOf(viewTransform.xz));
//		Log.d("AccelerometerModel", "vt_yx: " + String.valueOf(viewTransform.yx) + " vt_yy: " + String.valueOf(viewTransform.yy) + " vt_yz: " + String.valueOf(viewTransform.yz));
//		Log.d("AccelerometerModel", "vt_zx: " + String.valueOf(viewTransform.zx) + " vt_zy: " + String.valueOf(viewTransform.zy) + " vt_zz: " + String.valueOf(viewTransform.zz));
		
//		Log.d("AccelerometerModel", "event.values[0]: " + String.valueOf(event.values[0]) + " event.values[1]: " + String.valueOf(event.values[1] + " event.values[2]: " + String.valueOf(event.values[2])));

		mUser.lookDir = lookVector;
		mUser.lookNormal = upVector;
		
		mRenderer.mLookX = mUser.getLookX();
		mRenderer.mLookY = mUser.getLookY();
		mRenderer.mLookZ = mUser.getLookZ();
		mRenderer.mUpX   = mUser.getNormalX();
		mRenderer.mUpY   = mUser.getNormalY();
		mRenderer.mUpZ   = mUser.getNormalZ();
		
		Log.d("AccelerometerModel", "lastX: " + String.valueOf(last[0]) + " lastY: " + String.valueOf(last[1]) + " lastZ: " + String.valueOf(last[2]));
		Log.d("AccelerometerModel", "currentX: " + String.valueOf(current[0]) + " currentY: " + String.valueOf(current[1]) + " currentZ: " + String.valueOf(current[2]));
		Log.d("AccelerometerModel", "mLookX: " + String.valueOf(mUser.getLookX()) + " mLookY: " + String.valueOf(mUser.getLookY()) + " mLookZ: " + String.valueOf(mUser.getLookZ()));
		Log.d("AccelerometerModel", "mUpX: " + String.valueOf(mUser.getNormalX()) + " mUpY: " + String.valueOf(mUser.getNormalY()) + " mUpZ: " + String.valueOf(mUser.getNormalZ()));
		
		//mGLSurfaceView.requestRender();
	}
}
