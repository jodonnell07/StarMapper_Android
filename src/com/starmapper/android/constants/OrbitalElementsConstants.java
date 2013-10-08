package com.starmapper.android.constants;

public interface OrbitalElementsConstants {

	public static final int NUM_PLANETS = 8;
	public static final int NUM_ORBITAL_ELEMENTS = 6;
	public static final int NUM_E_ANOMALY_ITERATIONS = 10;
	public static final double J2000_ECLIPTIC_OBLIQUITY = 23.439281;		// degrees
	
	public static final double MOON_OFFSET_FUDGE_ADDER = 35.0;
	
	public static enum PlanetEnum {
		MERCURY, VENUS, EARTH, MARS, JUPITER, SATURN, URANUS, NEPTUNE
	}
	
	public static final float   SUN_SCALE_FACTOR = 200f;
	public static final float	MOON_SCALE_FACTOR = 180f;
	public static final float[] PLANET_SCALE_FACTOR = {
		85f, 95f, 100f, 90f, 120f, 130f, 130f, 105f
	};
	
	/* PLANETARY MEAN ORBITS */
	/*
	 * Ordering of orbital element values for each planet:
	 * 1) mean distance
	 * 2) eccentricity
	 * 3) inclination
	 * 4) ascending node longitude
	 * 5) perihelion longitude
	 * 6) mean longitude
	 */
    // Mercury
	public static final double[] MERCURY = {
		0.38709893,
		0.20563069,
		7.00487,
		48.33167,
		77.45645,
		252.25084		
	};
	
	// Venus
	public static final double[] VENUS = {
		0.72333199,
		0.00677323,
		3.39471,
		76.68069,
		131.53298,
		181.97973
	};
	
	// Earth
	public static final double[] EARTH = {
		1.00000011,
		0.01671022,
		0.00005,
		-11.26064,
		102.94719,
		100.46435
	};
	
	// Mars
	public static final double[] MARS = {
		1.52366231,
		0.09341233,
		1.85061,
		49.57854,
		336.04084,
		355.45332
	};
	
	// Jupiter
	public static final double[] JUPITER = {
	    5.20336301,
	    0.04839266,
	    1.30530,
	    100.55615,
	    14.75385,
	    34.40438
	};
	
	// Saturn
	public static final double[] SATURN = {
		9.53707032,
		0.05415060,
		2.48446,
		113.71504,
		92.43194,
		49.94432		
	};
	
	// Uranus
	public static final double[] URANUS = {
		19.19126393,
		0.04716771,
		0.76986,
		74.22988,
		170.96424,
		313.23218
	};
	
	// Neptune
	public static final double[] NEPTUNE = {
		30.06896348,
		0.00858587,
		1.76917,
		131.72169,
		44.97135,
		304.88003
	};
	
	// Moon will not be part of planet arrays
	public static final double[] MoonMeanOrbitalElementsConstants = {
		60.2666,							// mean distance for moon is in Earth radii (not AU)
		//0.002521,							// mean distance in AU
		0.0549,
		5.1454,
		125.1228,
		83.1862,
		73.4288
	};
	
	
	
	/* PLANETARY ORBITAL ELEMENTS CENTENNIAL RATES OF CHANGE */
	/*
	 * Ordering of orbital element values for each planet:
	 * 1) mean distance
	 * 2) eccentricity
	 * 3) inclination
	 * 4) ascending node longitude
	 * 5) perihelion longitude
	 * 6) mean longitude
	 */
	// Mercury
	public static final double[] MERCURY_RATES = {
		0.00000066,
		0.00002527,
		-23.51,
		-446.30,
		573.57,
		538101628.29 
	};
	
	// Venus
	public static final double[] VENUS_RATES = {
		0.00000092,
		-0.00004938,
		-2.86,
		-996.89,
		-108.80,
		210664136.06 		
	};
	
	// Earth
	public static final double[] EARTH_RATES = {
		-0.00000005,
		-0.00003804,
		-46.94,
		-18228.25,
		1198.28,
		129597740.63 
	};
	
	// Mars
	public static final double[] MARS_RATES = {
	    -0.00007221,
	    0.00011902,
	    -25.47,
	    -1020.19,
	    1560.78,
	    68905103.78 
	};
	
	// Jupiter
	public static final double[] JUPITER_RATES = {
		0.00060737,
		-0.00012880,
		-4.15,
		1217.17,
		839.93,
		10925078.35 
	};
	
	// Saturn
	public static final double[] SATURN_RATES = {
		-0.00301530,
		-0.00036762,
		6.11,
		-1591.05,
		-1948.89,
		4401052.95 
	};
	
	// Uranus
	public static final double[] URANUS_RATES = {
		0.00152025,
		-0.00019150,
		-2.09,
		-1681.40,
		1312.56,
		1542547.79 
	};
	
	// Neptune
	public static final double[] NEPTUNE_RATES = {
		-0.00125196,
		0.0000251,
		-3.64,
		-151.25,
		-844.43,
		786449.21 
	};
	
	// moon will not be part of planet arrays
	public static final double[] MoonOrbitalElementsRatesOfChange = {
		0.0,
		0.0,
		0.0,
		-0.0529538083,
		0.1643573223,
		13.22935
	};

	
	
	public static final double[][] MeanOrbitalElementsConstants = { MERCURY, VENUS, EARTH, MARS, JUPITER, SATURN, NEPTUNE, URANUS, NEPTUNE };
	public static final double[][] OrbitalElementsRatesOfChange = { MERCURY_RATES, VENUS_RATES, EARTH_RATES, MARS_RATES, JUPITER_RATES, SATURN_RATES, URANUS_RATES, NEPTUNE_RATES, NEPTUNE_RATES };
	
	// Pluto
	//	MEAN : Pluto	39.48168677	0.24880766	17.14175	110.30347	224.06676	238.92881
	//  ROC  : Pluto -0.00076912 0.00006465 11.07 -37.33 -132.25 522747.90 

}
