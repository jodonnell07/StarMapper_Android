package com.starmapper.android.constants;

public interface ArrayConstants {

	// OpenGL Vertex Array Values
	public static final int NUM_TRIANGLES						= 2;
	public static final int VERTICES_PER_TRIANGLE				= 3;
	public static final int INDICES_PER_UNIT					= 6;
	public static final int BYTES_PER_FLOAT						= 4;
	public static final int POSITION_DATA_SIZE					= 3;
	public static final int COLOR_DATA_SIZE						= 4;
	public static final int TEXTURE_COORDINATE_DATA_SIZE		= 2;
	public static final int POSITION_LENGTH_PER_UNIT			= NUM_TRIANGLES * VERTICES_PER_TRIANGLE * POSITION_DATA_SIZE;
	public static final int COLOR_LENGTH_PER_UNIT				= NUM_TRIANGLES * VERTICES_PER_TRIANGLE * COLOR_DATA_SIZE;
	public static final int TEXTURE_COORDINATE_LENGTH_PER_UNIT  = NUM_TRIANGLES * VERTICES_PER_TRIANGLE * TEXTURE_COORDINATE_DATA_SIZE;
	public static final int POSITION_STRIDE_BYTES				= POSITION_DATA_SIZE * BYTES_PER_FLOAT;					// 12
	public static final int COLOR_STRIDE_BYTES					= COLOR_DATA_SIZE * BYTES_PER_FLOAT;					// 16
	public static final int TEXTURE_COORDINATE_STRIDE_BYTES 	= TEXTURE_COORDINATE_DATA_SIZE * BYTES_PER_FLOAT;		// 8
	
	// Grid Values
	public static final int RA_VERTICES							= 3;
	public static final int DEC_VERTICES						= 36;
	public static final int RA_LINES							= 24;
	public static final int DEC_LINES							= 10;
	
	// Orbital Element Array Indices
	public static final int MEAN_DISTANCE						= 0;
	public static final int ECCENTRICITY						= 1;
	public static final int INCLINATION							= 2;
	public static final int ANODE_LONGITUDE						= 3;
	public static final int PERI_LONGITUDE						= 4;
	public static final int MEAN_LONGITUDE						= 5;
	// for the moon
	public static final int MEAN_ANOMALY						= 5;
	
	// Various Arrays
	public static final float[] GRID_COLOR_DATA_ARRAY = {
			0.4f, 0.3f, 0.9f, 1.0f,
			0.4f, 0.3f, 0.9f, 1.0f,
			0.4f, 0.3f, 0.9f, 1.0f,
			0.4f, 0.3f, 0.9f, 1.0f,
			0.4f, 0.3f, 0.9f, 1.0f,
			0.4f, 0.3f, 0.9f, 1.0f,
	};
	
	public static final float[] CONST_LINE_COLOR_DATA_ARRAY = {
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
	};
	
	public static final float[] STAR_COLOR_DATA_ARRAY = {
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
	};
	
	public static final float[] PLANET_COLOR_DATA_ARRAY = {
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
	};	

	public static final float[] SUN_COLOR_DATA_ARRAY = {
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
};	

	public static final float[] MOON_COLOR_DATA_ARRAY = {
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 1.0f,
};	

	public static final float[] TEX_COORD_ARRAY = {
			0.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 1.0f,
			1.0f, 0.0f,
	};

}
