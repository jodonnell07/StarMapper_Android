package com.starmapper.android.program;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.example.starmapper_android.R;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.Matrix3x3;
import com.starmapper.android.sensors.AccelerometerModel;
import com.starmapper.android.sensors.MagneticFieldModel;
import com.starmapper.android.user.User;
import com.starmapper.android.utils.Flinger;
import com.starmapper.android.utils.Flinger.FlingListener;
import com.starmapper.android.utils.MathUtils;
import com.starmapper.android.utils.Zoom;

public class StarMapper extends Activity implements MathConstants, OnSharedPreferenceChangeListener {

	// keys used by Preferences
	public static final String PREF_KEY_STARS            = "settings_provider_stars";
	public static final String PREF_KEY_BACKGROUND_STARS = "settings_provider_background_stars";
	public static final String PREF_KEY_CONSTELLATIONS   = "settings_provider_constellations";
	public static final String PREF_KEY_SUN              = "settings_provider_sun";
	public static final String PREF_KEY_MOON             = "settings_provider_moon";
	public static final String PREF_KEY_PLANETS          = "settings_provider_planets";
	public static final String PREF_KEY_CELESTIAL_GRID   = "settings_provider_celestial_grid";
	
	// Settings fragment for user settings
	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}
	}
	
	/** Preferences shared between all activities **/
	private SharedPreferences sharedPreferences;
	
	/** SettingsFragment for user settings **/
	private SettingsFragment settingsFragment;
	
	/** Model for the User **/
	public User mUser;
	
	/** Reference to GLSurfaceView **/
	private GLSurfaceView mGLSurfaceView;
	
	/** Reference to StarMapperRenderer used by GLSurfaceView **/
	private StarMapperRenderer mStarMapperRenderer;
	
	/** For location **/
	private LocationManager mLocationManager;
	
	/** For touchscreen **/
	private enum TouchState {INIT, DRAG_ONE, DRAG_TWO}
	private float mPreviousX1;
	private float mPreviousY1;
	private float mPreviousX2;
	private float mPreviousY2;
	private int   mPointerIndex;
	private TouchState mCurrentTouchState;
	private GestureDetector flinger;
	private Zoom mZoomer;
	
	/** For sensors **/
	private SensorManager mSensorManager;
	private Sensor mAccelerometerSensor;
	private Sensor mMagneticFieldSensor;
	private AccelerometerModel mAccelerometerModel;
	private MagneticFieldModel mMagneticFieldModel;
	
	/** switch for auto/manual mode **/
	private boolean mUseAutoSensorMode;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		settingsFragment = new SettingsFragment();

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		//Debug
//		setContentView(R.layout.title);
		
  		mGLSurfaceView = new MyGLSurfaceView(this);
		// Create an OpenGL ES 2.0 context
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setPreserveEGLContextOnPause(true);
//		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setContentView(mGLSurfaceView);
		
		mStarMapperRenderer = new StarMapperRenderer(this);
		
		// Set the StarMapperRenderer for drawing on the GLSurfaceView
		mGLSurfaceView.setRenderer(mStarMapperRenderer);
		
		// The app user object
		mUser = new User();

		// Retrieving location
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria locCriteria = new Criteria();
		// gps
		locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		locCriteria.setBearingRequired(false);
		locCriteria.setAltitudeRequired(false);
		locCriteria.setCostAllowed(true);
		locCriteria.setSpeedRequired(false);
		locCriteria.setPowerRequirement(Criteria.POWER_LOW);
		
		String locProvider = mLocationManager.getBestProvider(locCriteria, true);
		Location loc = mLocationManager.getLastKnownLocation(locProvider);
		
		mUser.setGeoLocation((float) loc.getLatitude(), (float) loc.getLongitude());
		mUser.setGeomagneticField();
		mUser.setZenith();
		
		// Initializing touch variables
		mPreviousX1 = 0;
		mPreviousY1 = 0;
		mPreviousX2 = 0;
		mPreviousY2 = 0;
		mPointerIndex = 0;
		mCurrentTouchState = TouchState.INIT;
		mZoomer = new Zoom();
		
		// Initializing auto sensors
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometerSensor = null;
		mMagneticFieldSensor = null;
		mAccelerometerModel = null;
		mMagneticFieldModel = null;
		// Check for necessary sensors
		if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
			// phone doesn't have appropriate sensors, only use manual mode
			mUseAutoSensorMode = false;
		} else {
			// phone has sensors, initialize them
			mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			mUseAutoSensorMode = true;
			mAccelerometerModel = new AccelerometerModel(mUser, mStarMapperRenderer, mGLSurfaceView);
			mMagneticFieldModel = new MagneticFieldModel(mUser, mStarMapperRenderer, mGLSurfaceView);
		}
		
		if (mAccelerometerSensor != null) {
			mSensorManager.registerListener(mAccelerometerModel, mAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
		}
		if (mMagneticFieldSensor != null) {
			mSensorManager.registerListener(mMagneticFieldModel, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);
		}
		//****** DEBUG ******
		//mUseAutoSensorMode = false;
		
		// flinger
		flinger = new GestureDetector(this, new Flinger(new FlingListener() {
			public void fling(float distanceX, float distanceY) {
				float radsPerPixel = mStarMapperRenderer.mFovYRad / mStarMapperRenderer.mScreenHeight;
				changeMapX(-distanceX * radsPerPixel);
				changeMapY(-distanceY * radsPerPixel);
				
				mStarMapperRenderer.mLookX = mUser.getLookX();
				mStarMapperRenderer.mLookY = mUser.getLookY();
				mStarMapperRenderer.mLookZ = mUser.getLookZ();
				mStarMapperRenderer.mUpX   = mUser.getNormalX();
				mStarMapperRenderer.mUpY   = mUser.getNormalY();
				mStarMapperRenderer.mUpZ   = mUser.getNormalZ();
			}
		}));

	}

	// inner class definition for GLSurfaceView
	class MyGLSurfaceView extends GLSurfaceView {	
		public MyGLSurfaceView(Context context) {
			super(context);
		}
	}
	
	@Override
	protected void onResume() {
		// must call GLSurfaceView's onResume()
		super.onResume();
		mGLSurfaceView.onResume();
	}
	
	@Override
	protected void onPause() {
		// must call GLSurfaceView's onPause()
		super.onPause();
		mGLSurfaceView.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_star_mapper, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		    case R.id.menu_settings:
		    	getFragmentManager().beginTransaction().addToBackStack(null).replace(android.R.id.content, settingsFragment).commit();
		    	break;
		    case R.id.menu_auto_sensor_mode:
		    	mUseAutoSensorMode = !mUseAutoSensorMode;
		    	if (mUseAutoSensorMode) {
		    		mSensorManager.registerListener(mAccelerometerModel, mAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
		    		mSensorManager.registerListener(mMagneticFieldModel, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);
		    	} else {
		    		mSensorManager.unregisterListener(mAccelerometerModel);
		    		mSensorManager.unregisterListener(mMagneticFieldModel);
		    	}
		    	break;
		    case R.id.menu_test:
		    	//this won't do anything
		    	break;
		    default:
		    	return false;
		}
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
//		Log.d("onTouchEvent", "Current mCurrentTouchState value");
//		Log.d("onTouchEvent", String.valueOf(mCurrentTouchState));
		
		// Check whether in touch/sensor mode
		if (mUseAutoSensorMode) { return true; }
		
		boolean touchResult = false;
		if (flinger.onTouchEvent(e)) {
			touchResult = true;
		}
		int action = e.getAction() & MotionEvent.ACTION_MASK;
		if (processTouch(action, e)) {
			touchResult = true;
		}
		return touchResult;
	}
	
	private void rotate(float radians) {
		Geocentric lookVector = mUser.getLookDir();
		Geocentric upVector   = mUser.getLookNormal();
		
		Matrix3x3 rotationMatrix = MathUtils.createRotationMatrix(radians, lookVector);
		Geocentric newUpVector = MathUtils.multiplyGeocentricAndMatrix3x3(rotationMatrix, upVector);
		
		newUpVector = MathUtils.normalize(newUpVector);
		
		mUser.setLookNormal(newUpVector);
	}
	
	private void changeMapX(float radians) {
		Geocentric lookVector  = mUser.getLookDir();
		Geocentric crossVector = MathUtils.crossProduct(lookVector, mUser.getLookNormal());
		Geocentric deltaLookVector = new Geocentric(crossVector.x * radians, crossVector.y * radians, crossVector.z * radians);
		
		Geocentric newLookDir = MathUtils.add(lookVector, deltaLookVector);
		newLookDir = MathUtils.normalize(newLookDir);
		
		mUser.setLookDir(newLookDir);
	}
	
	private void changeMapY(float radians) {
		Geocentric lookVector  = mUser.getLookDir();
		Geocentric upVector    = mUser.getLookNormal();
		
		Geocentric deltaLookVector = new Geocentric(upVector.x * -radians, upVector.y * -radians, upVector.z * -radians);
		Geocentric newLookDir = MathUtils.add(lookVector, deltaLookVector);
		newLookDir = MathUtils.normalize(newLookDir);
		
		Geocentric deltaUpVector = new Geocentric(lookVector.x * radians, lookVector.y * radians, lookVector.z * radians);
		Geocentric newUpDir = MathUtils.add(upVector, deltaUpVector);
		newUpDir = MathUtils.normalize(newUpDir);
		
		mUser.setLookDir(newLookDir);
		mUser.setLookNormal(newUpDir);
	}
	
	private boolean processTouch(int action, MotionEvent e) {
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (mCurrentTouchState == TouchState.INIT) {
				mCurrentTouchState = TouchState.DRAG_ONE;
			    mPreviousX1 = e.getX();
			    mPreviousY1 = e.getY();
			    return true;
			}
		case MotionEvent.ACTION_POINTER_DOWN:
			if (mCurrentTouchState == TouchState.DRAG_ONE) {
				mCurrentTouchState = TouchState.DRAG_TWO;
				mPointerIndex = e.getActionIndex();
				mPreviousX1 = e.getX();
				mPreviousY1 = e.getY();
				mPreviousX2 = e.getX(mPointerIndex);
				mPreviousY2 = e.getY(mPointerIndex);
				return true;
			}
		case MotionEvent.ACTION_MOVE:
			if (mCurrentTouchState == TouchState.DRAG_ONE) {
			    float currentX = e.getX();
			    float currentY = e.getY();
			    float deltaX   = currentX - mPreviousX1;
			    float deltaY   = currentY - mPreviousY1;
			
			    float radsPerPixel = mStarMapperRenderer.mFovYRad / mStarMapperRenderer.mScreenHeight;
			    changeMapX(-deltaX * radsPerPixel);
			    changeMapY(-deltaY * radsPerPixel);
			
			    mStarMapperRenderer.mLookX = mUser.getLookX();
			    mStarMapperRenderer.mLookY = mUser.getLookY();
			    mStarMapperRenderer.mLookZ = mUser.getLookZ();
			    mStarMapperRenderer.mUpX   = mUser.getNormalX();
			    mStarMapperRenderer.mUpY   = mUser.getNormalY();
			    mStarMapperRenderer.mUpZ   = mUser.getNormalZ();
			
			    mPreviousX1 = currentX;
			    mPreviousY1 = currentY;
			    return true;
			} else if (mCurrentTouchState == TouchState.DRAG_TWO) {
			    float currentX1 = e.getX();
			    float currentY1 = e.getY();
			    float currentX2 = e.getX(mPointerIndex);
			    float currentY2 = e.getY(mPointerIndex);
			    float deltaX1   = currentX1 - mPreviousX1;
			    float deltaY1   = currentY1 - mPreviousY1;
			    float deltaX2   = currentX2 - mPreviousX2;
			    float deltaY2   = currentY2 - mPreviousY2;
			    
			    float radsPerPixel = mStarMapperRenderer.mFovYRad / mStarMapperRenderer.mScreenHeight;
			    changeMapX(-((deltaX1 + deltaX2) / 2) * radsPerPixel);
			    changeMapY(-((deltaY1 + deltaY2) / 2) * radsPerPixel);
			    
			    float vPreviousX = mPreviousX1 - mPreviousX2;
			    float vPreviousY = mPreviousY1 - mPreviousY2;
			    float vCurrentX  = currentX1 - currentX2;
			    float vCurrentY  = currentY1 - currentY2;
			    
			    
			    /* Need a 'stretch' function here for field of view once zoom is implemented */
			    float vectorRatio = (float) Math.sqrt((vCurrentX * vCurrentX + vCurrentY * vCurrentY) / (vPreviousX * vPreviousX + vPreviousY * vPreviousY));
			    mStarMapperRenderer.mFovYRad = mZoomer.zoomBy(mStarMapperRenderer.mFovYRad, 1.0f / vectorRatio);
			    mStarMapperRenderer.mUpdatePerspective = true;
			    
			    double anglePrevious = Math.atan2(vPreviousX, vPreviousY);
			    double angleCurrent  = Math.atan2(vCurrentX, vCurrentY);
			    float angleDelta     = (float) (anglePrevious - angleCurrent);
			    
			    rotate(angleDelta);
			    
			    mStarMapperRenderer.mLookX = mUser.getLookX();
			    mStarMapperRenderer.mLookY = mUser.getLookY();
			    mStarMapperRenderer.mLookZ = mUser.getLookZ();
			    mStarMapperRenderer.mUpX   = mUser.getNormalX();
			    mStarMapperRenderer.mUpY   = mUser.getNormalY();
			    mStarMapperRenderer.mUpZ   = mUser.getNormalZ();
			    
			    mPreviousX1 = currentX1;
			    mPreviousY1 = currentY1;
			    mPreviousX2 = currentX2;
			    mPreviousY2 = currentY2;
			    return true;
			}
		case MotionEvent.ACTION_UP:
			if (mCurrentTouchState != TouchState.INIT) {
				mCurrentTouchState = TouchState.INIT;
				return true;
			}
		case MotionEvent.ACTION_POINTER_UP:
			if (mCurrentTouchState == TouchState.DRAG_TWO) {
				mCurrentTouchState = TouchState.INIT;
			}
		}
		return false;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = settingsFragment.findPreference(key);
		if (key.equals(PREF_KEY_STARS)) { mStarMapperRenderer.setStarsIsEnabled(sharedPreferences.getBoolean(PREF_KEY_STARS, true)); }
		else if (key.equals(PREF_KEY_BACKGROUND_STARS)) { mStarMapperRenderer.setBGStarsIsEnabled(sharedPreferences.getBoolean(PREF_KEY_BACKGROUND_STARS, true)); }
		else if (key.equals(PREF_KEY_CONSTELLATIONS)) { mStarMapperRenderer.setConstellationsIsEnabled(sharedPreferences.getBoolean(PREF_KEY_CONSTELLATIONS, true)); }
		else if (key.equals(PREF_KEY_SUN)) { mStarMapperRenderer.setSunIsEnabled(sharedPreferences.getBoolean(PREF_KEY_SUN, true)); }
		else if (key.equals(PREF_KEY_MOON)) { mStarMapperRenderer.setMoonIsEnabled(sharedPreferences.getBoolean(PREF_KEY_MOON, true)); }
		else if (key.equals(PREF_KEY_PLANETS)) { mStarMapperRenderer.setPlanetsIsEnabled(sharedPreferences.getBoolean(PREF_KEY_PLANETS, true)); }
		else if (key.equals(PREF_KEY_CELESTIAL_GRID)) { mStarMapperRenderer.setCelestialGridIsEnabled(sharedPreferences.getBoolean(PREF_KEY_CELESTIAL_GRID, true)); }
	}
}
