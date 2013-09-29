package com.starmapper.android.utils;

import com.starmapper.android.utils.MathUtils;
import com.starmapper.android.constants.MathConstants;

import java.lang.Math;

public class Zoom implements MathConstants {

	public static final float MAX_ZOOM = 90.0f * MathUtils.convertToRadians;
	public static final float MIN_ZOOM = 15.0f * MathUtils.convertToRadians;
	
	public float mPreviousFOV;
	
	// Constructors
	public Zoom() {
		mPreviousFOV = INIT_FOV_Y_RAD;
	}
	
	public float zoomBy(float currentFOV, float ratio) {
		if (currentFOV > mPreviousFOV) {
			mPreviousFOV = currentFOV;
		    return Math.min(currentFOV * ratio, MAX_ZOOM);
		} else {
			mPreviousFOV = currentFOV;
			return Math.max(currentFOV * ratio, MIN_ZOOM);
		}
		
	}
}
