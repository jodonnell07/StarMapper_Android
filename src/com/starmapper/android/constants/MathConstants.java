package com.starmapper.android.constants;

import android.graphics.Color;

import com.starmapper.android.math.Geocentric;
import com.starmapper.android.utils.MathUtils;

public interface MathConstants {

	// ****label-related stuff**** //
	public static enum LabelTypeEnum {
		STAR, CONSTELLATION, SUN, MOON, PLANET, GRID
	}
	public static final int CONSTELLATION_COLOR 	= Color.BLUE;
	public static final int STAR_COLOR				= Color.WHITE;
	public static final int PLANET_COLOR			= Color.MAGENTA;
	public static final int SUN_COLOR				= Color.RED;
	public static final int MOON_COLOR				= Color.CYAN;
	public static final int GRID_COLOR				= Color.YELLOW;	
	public static final int CONSTELLATION_TEXTSIZE	= 32;
	public static final int STAR_TEXTSIZE			= 32;
	public static final int PLANET_TEXTSIZE			= 32;
	public static final int SUN_TEXTSIZE			= 32;
	public static final int MOON_TEXTSIZE			= 32;
	public static final int GRID_TEXTSIZE			= 32;
	// ****End of label-related stuff**** //
	
	public static final float PI 					= (float) Math.PI;
	public static final float TWOPI 				= 2.0f * PI;
	public static final float INIT_FOV_Y_RAD		= 45.0f * PI / 180.0f;
	public static final float SCREEN_HEIGHT			= 1920.0f;
	public static final float MIN_MAGNITUDE			= 6.0f;
	public static final float MAX_MAGNITUDE			= -1.5f;
	public static final float MAGNITUDE_RANGE		= 7.5f;
	public static final float MAGNITUDE_SCALE		= 160.0f;
	public static final float POINT_SIZE_MIN_PIXELS = 40.0f;
	public static final float POINT_SIZE_MAX_PIXELS = MAGNITUDE_SCALE + POINT_SIZE_MIN_PIXELS;
	public static final float POINT_SIZE_FACTOR 	= MathUtils.tan(INIT_FOV_Y_RAD / 2.0f) / SCREEN_HEIGHT;
	public static final float LINE_SIZE_FACTOR  	= POINT_SIZE_FACTOR * 2;
	public static final Geocentric Z_DOWN_VECTOR	= new Geocentric(0.0f, 0.0f, -1.0f);
	public static final Geocentric Z_UP_VECTOR		= new Geocentric(0.0f, 0.0f, 1.0f);
	public static final Geocentric INIT_LOOK_VECTOR = new Geocentric(0.0f, 0.0f, 1.0f);
	public static final Geocentric INIT_UP_VECTOR   = new Geocentric(0.0f, 1.0f, 0.0f);
	public static final Geocentric INIT_ACCEL		= new Geocentric(0.0f, -1.0f, 0.0f);
	public static final Geocentric INIT_MAG_FIELD	= new Geocentric(0.0f, -9.0f, -1.0f);
	public static final Geocentric EYE_POSITION     = new Geocentric(0.0f, 0.0f, 0.0f);
	public static final int J2000_YEAR				= 2000;
	public static final int J2000_MONTH				= 0;		//0 is field value for Jan
	public static final int J2000_DAY				= 1;
	public static final int J2000_HOUR				= 12;
	public static final int J2000_MINUTE			= 0;
	public static final int J2000_SECOND			= 0;
}
