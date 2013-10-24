package com.starmapper.android.constants;

import java.util.LinkedHashSet;
import java.util.Set;

public interface BayerFileConstants {

	public static final int CONSTELLATION_NAME	= 0;
	
	public static final int STAR_NAME			= 0;
	public static final int RA_HOUR				= 1;
	public static final int RA_MINUTE			= 2;
	public static final int RA_SECOND			= 3;
	public static final int DEC_DEGREE			= 4;
	public static final int DEC_MINUTE			= 5;
	public static final int DEC_SECOND			= 6;
	public static final int MAGNITUDE			= 7;
	public static final int STAR_ID				= 8;
	
	public static final int STAR_1				= 0;
	public static final int STAR_2				= 1;
	
	// list of stars to be labeled
	public static enum NamedStarsEnum {
		ALDEBARAN, SIRIUS, VEGA, PROCYON, RIGEL, POLARIS, ARCTURUS, BETELGEUSE
	}
	
}
