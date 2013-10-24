package com.starmapper.android.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;

import android.opengl.GLES20;
import android.util.Log;

import com.starmapper.android.celestial.Planet;
import com.starmapper.android.constants.ArrayConstants;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.constants.OrbitalElementsConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;
import com.starmapper.android.utils.MathUtils;

public class PlanetManager implements ArrayConstants, MathConstants, OrbitalElementsConstants {

	// Planets to render
	Set<Planet> PlanetSet;
		
	// Buffers for OpenGL drawing
	private FloatBuffer[] mPlanetPositionsBuffer;
	private FloatBuffer[] mPlanetColorsBuffer;
	private FloatBuffer[] mPlanetTexturesBuffer;
	
	// Renderer
	private StarMapperRenderer mRenderer;
	
	// Calendars
	private final GregorianCalendar J2000_epoch;
	private final GregorianCalendar currentCalendar;
	
	// delta time
	private final double mDaysSinceJ2000Epoch;
	private final double mCenturiesSinceJ2000Epoch;
	
	// Orbital Elements Constants based on current date/time
	private final double[][] mOrbitalElementsConstants;
	
	// Earth heliocentric coordinates
	private double mEarthHelioCoord_X;
	private double mEarthHelioCoord_Y;
	private double mEarthHelioCoord_Z;
	
	// Obliquity of the J2000 Ecliptic
	private final double mJ2000EclipticObliquityRad;
	
	// Constructor
	public PlanetManager(StarMapperRenderer renderer) {
		PlanetSet = new LinkedHashSet<Planet>();
		mRenderer = renderer;
		
		J2000_epoch = new GregorianCalendar(J2000_YEAR, J2000_MONTH, J2000_DAY, J2000_HOUR, J2000_MINUTE, J2000_SECOND);
		currentCalendar = new GregorianCalendar(TimeZone.getDefault());
		
		int deltaYears = currentCalendar.get(Calendar.YEAR) - J2000_epoch.get(Calendar.YEAR);
		int deltaMonths = currentCalendar.get(Calendar.MONTH) - J2000_epoch.get(Calendar.MONTH);
		int deltaDays = currentCalendar.get(Calendar.DAY_OF_MONTH) - J2000_epoch.get(Calendar.DAY_OF_MONTH);
		int deltaHours = currentCalendar.get(Calendar.HOUR_OF_DAY) - J2000_epoch.get(Calendar.HOUR_OF_DAY);
		int deltaMinutes = currentCalendar.get(Calendar.MINUTE) - J2000_epoch.get(Calendar.MINUTE);
		int deltaSeconds = currentCalendar.get(Calendar.SECOND) - J2000_epoch.get(Calendar.SECOND);
		
		// h = fraction of current day
		float h = deltaHours + deltaMinutes / 60.0f + deltaSeconds / 3600.0f;
		
		int tempVar1 = 7 * (deltaYears + (deltaMonths + 9) / 12) / 4;
		int tempVar2 = 275 * deltaMonths / 9;
		
		mDaysSinceJ2000Epoch = 367.0f * deltaYears - tempVar1 + tempVar2 + deltaDays + h / 24.0f;
		mCenturiesSinceJ2000Epoch = mDaysSinceJ2000Epoch / 36525.0f;
		
		mJ2000EclipticObliquityRad = J2000_ECLIPTIC_OBLIQUITY * MathUtils.convertToRadians;
		
		Set<PlanetEnum> PlanetEnumSet = EnumSet.allOf(PlanetEnum.class);
		int PlanetEnumSize = PlanetEnumSet.size();
		PlanetEnumSet.clear();
		
		mOrbitalElementsConstants = new double[PlanetEnumSize][NUM_ORBITAL_ELEMENTS];
		
		mPlanetPositionsBuffer = new FloatBuffer[NUM_PLANETS];
		mPlanetColorsBuffer = new FloatBuffer[NUM_PLANETS];
		mPlanetTexturesBuffer = new FloatBuffer[NUM_PLANETS];
		
		for (PlanetEnum planetEnum : PlanetEnum.values()) {
			int planetIdx = planetEnum.ordinal();
			mOrbitalElementsConstants[planetIdx][MEAN_DISTANCE]   = MeanOrbitalElementsConstants[planetIdx][MEAN_DISTANCE] + OrbitalElementsRatesOfChange[planetIdx][MEAN_DISTANCE] * mCenturiesSinceJ2000Epoch;
			mOrbitalElementsConstants[planetIdx][ECCENTRICITY]    = MeanOrbitalElementsConstants[planetIdx][ECCENTRICITY] + OrbitalElementsRatesOfChange[planetIdx][ECCENTRICITY] * mCenturiesSinceJ2000Epoch;
			mOrbitalElementsConstants[planetIdx][INCLINATION]     = MeanOrbitalElementsConstants[planetIdx][INCLINATION] + OrbitalElementsRatesOfChange[planetIdx][INCLINATION] * mCenturiesSinceJ2000Epoch / 3600.0f;
			mOrbitalElementsConstants[planetIdx][ANODE_LONGITUDE] = MeanOrbitalElementsConstants[planetIdx][ANODE_LONGITUDE] + OrbitalElementsRatesOfChange[planetIdx][ANODE_LONGITUDE] * mCenturiesSinceJ2000Epoch / 3600.0f;
			mOrbitalElementsConstants[planetIdx][PERI_LONGITUDE]  = MeanOrbitalElementsConstants[planetIdx][PERI_LONGITUDE] + OrbitalElementsRatesOfChange[planetIdx][PERI_LONGITUDE] * mCenturiesSinceJ2000Epoch / 3600.0f;
			mOrbitalElementsConstants[planetIdx][MEAN_LONGITUDE]  = MeanOrbitalElementsConstants[planetIdx][MEAN_LONGITUDE] + OrbitalElementsRatesOfChange[planetIdx][MEAN_LONGITUDE] * mCenturiesSinceJ2000Epoch / 3600.0f;
		}
		Log.d("END", "End of PlanetManager Constructor");
	}
	
	// Construct all the planet data
	public void BuildPlanetData() {
		// Need to find heliocentric coordinates of Earth first
		// these are used to calculate RA & Dec of the other planets on equatorial coordinate system
		int Earth = PlanetEnum.EARTH.ordinal();
		double meanAnomalyEarth = (mOrbitalElementsConstants[Earth][MEAN_LONGITUDE] - mOrbitalElementsConstants[Earth][PERI_LONGITUDE]) % 360.0f;
		double trueAnomalyEarth = MathUtils.calculateTrueAnomaly(meanAnomalyEarth, mOrbitalElementsConstants[Earth][ECCENTRICITY], NUM_E_ANOMALY_ITERATIONS);
		if (trueAnomalyEarth < 0.0) { trueAnomalyEarth += 360.0; }
		double trueAnomalyEarthRad = trueAnomalyEarth * MathUtils.convertToRadians;
		
		double helioRadiusEarth = (mOrbitalElementsConstants[Earth][MEAN_DISTANCE] * (1 - mOrbitalElementsConstants[Earth][ECCENTRICITY] * mOrbitalElementsConstants[Earth][ECCENTRICITY])) /
				(1.0f + mOrbitalElementsConstants[Earth][ECCENTRICITY] * MathUtils.cosd(trueAnomalyEarthRad));

		double aNodeLongitudeEarthRad = mOrbitalElementsConstants[Earth][ANODE_LONGITUDE] * MathUtils.convertToRadians;
		double periLongitudeEarthRad  = mOrbitalElementsConstants[Earth][PERI_LONGITUDE]  * MathUtils.convertToRadians;
		double inclinationEarthRad    = mOrbitalElementsConstants[Earth][INCLINATION]     * MathUtils.convertToRadians;

	    mEarthHelioCoord_X = helioRadiusEarth * (MathUtils.cosd(aNodeLongitudeEarthRad) * MathUtils.cosd(trueAnomalyEarthRad + periLongitudeEarthRad - aNodeLongitudeEarthRad) -
								MathUtils.sind(aNodeLongitudeEarthRad) * MathUtils.sind(trueAnomalyEarthRad + periLongitudeEarthRad - aNodeLongitudeEarthRad) * MathUtils.cosd(inclinationEarthRad));
		mEarthHelioCoord_Y = helioRadiusEarth * (MathUtils.sind(aNodeLongitudeEarthRad) * MathUtils.cosd(trueAnomalyEarthRad + periLongitudeEarthRad - aNodeLongitudeEarthRad) +
								MathUtils.cosd(aNodeLongitudeEarthRad) * MathUtils.sind(trueAnomalyEarthRad + periLongitudeEarthRad - aNodeLongitudeEarthRad) * MathUtils.cosd(inclinationEarthRad));
		mEarthHelioCoord_Z = helioRadiusEarth * (MathUtils.sind(trueAnomalyEarthRad + periLongitudeEarthRad - aNodeLongitudeEarthRad) * MathUtils.sind(inclinationEarthRad));
			
		for (PlanetEnum planetEnum : PlanetEnum.values()) {
			// skip Earth
			if (planetEnum == PlanetEnum.EARTH) { continue; }
			
			int planetIdx = planetEnum.ordinal();
			
			// mean anomaly should be in range 0 < M < 360
			double meanAnomaly = (mOrbitalElementsConstants[planetIdx][MEAN_LONGITUDE] - mOrbitalElementsConstants[planetIdx][PERI_LONGITUDE]) % 360.0f;
			double trueAnomaly = MathUtils.calculateTrueAnomaly(meanAnomaly, mOrbitalElementsConstants[planetIdx][ECCENTRICITY], NUM_E_ANOMALY_ITERATIONS);
			if (trueAnomaly < 0.0) { trueAnomaly += 360.0; }
			double trueAnomalyRad = trueAnomaly * MathUtils.convertToRadians;
			
			double helioRadius = (mOrbitalElementsConstants[planetIdx][MEAN_DISTANCE] * (1 - mOrbitalElementsConstants[planetIdx][ECCENTRICITY] * mOrbitalElementsConstants[planetIdx][ECCENTRICITY])) /
								(1.0f + mOrbitalElementsConstants[planetIdx][ECCENTRICITY] * MathUtils.cosd(trueAnomalyRad));
			
			double aNodeLongitudeRad = mOrbitalElementsConstants[planetIdx][ANODE_LONGITUDE] * MathUtils.convertToRadians;
			double periLongitudeRad  = mOrbitalElementsConstants[planetIdx][PERI_LONGITUDE]  * MathUtils.convertToRadians;
			double inclinationRad    = mOrbitalElementsConstants[planetIdx][INCLINATION]     * MathUtils.convertToRadians;
			
			double helioCoord_X = helioRadius * (MathUtils.cosd(aNodeLongitudeRad) * MathUtils.cosd(trueAnomalyRad + periLongitudeRad - aNodeLongitudeRad) -
												MathUtils.sind(aNodeLongitudeRad) * MathUtils.sind(trueAnomalyRad + periLongitudeRad - aNodeLongitudeRad) * MathUtils.cosd(inclinationRad));
			double helioCoord_Y = helioRadius * (MathUtils.sind(aNodeLongitudeRad) * MathUtils.cosd(trueAnomalyRad + periLongitudeRad - aNodeLongitudeRad) +
												MathUtils.cosd(aNodeLongitudeRad) * MathUtils.sind(trueAnomalyRad + periLongitudeRad - aNodeLongitudeRad) * MathUtils.cosd(inclinationRad));
			double helioCoord_Z = helioRadius * (MathUtils.sind(trueAnomalyRad + periLongitudeRad - aNodeLongitudeRad) * MathUtils.sind(inclinationRad));
			
			double geoEclipticCoord_X = helioCoord_X - mEarthHelioCoord_X;
			double geoEclipticCoord_Y = helioCoord_Y - mEarthHelioCoord_Y;
			double geoEclipticCoord_Z = helioCoord_Z - mEarthHelioCoord_Z;
			
			double geoCoord_X = geoEclipticCoord_X;
			double geoCoord_Y = geoEclipticCoord_Y * MathUtils.cosd(mJ2000EclipticObliquityRad) - geoEclipticCoord_Z * MathUtils.sind(mJ2000EclipticObliquityRad);
			double geoCoord_Z = geoEclipticCoord_Y * MathUtils.sind(mJ2000EclipticObliquityRad) + geoEclipticCoord_Z * MathUtils.cosd(mJ2000EclipticObliquityRad);
			
			double planet_RA = MathUtils.arctand(geoCoord_Y / geoCoord_X) * MathUtils.convertToDegrees;
			if (geoCoord_X > 0.0f && geoCoord_Y < 0.0f) {
				planet_RA += 360.0f;
			} else if (geoCoord_X < 0.0f) {
				planet_RA += 180.0f;
			}
			int planet_RA_Hours = (int) (planet_RA / 15);
			int planet_RA_Minutes = (int) (((planet_RA % 15) / 15.0f) * 60);
			double planet_Dec = MathUtils.arctan2d(geoCoord_Z, Math.sqrt(geoCoord_X * geoCoord_X + geoCoord_Y * geoCoord_Y)) * MathUtils.convertToDegrees;
			
			double distance = (float) Math.sqrt(geoCoord_X * geoCoord_X + geoCoord_Y * geoCoord_Y + geoCoord_Z * geoCoord_Z);
			
			String planetName = planetEnum.name().toLowerCase();
			planetName = Character.toUpperCase(planetName.charAt(0)) + planetName.substring(1);
			Planet planet = new Planet(planetName, new RaDec((float) planet_RA, (float) planet_Dec), distance, planetIdx, PLANET_SCALE_FACTOR[planetIdx]);
			
			PlanetSet.add(planet);
		}
		Log.d("BuildPlanetData", "BuildPlanetData");
	}
	
	public void initializeBuffers() {
		
		for (int i = 0; i < NUM_PLANETS; i++) {
		    mPlanetPositionsBuffer[i] = ByteBuffer.allocateDirect(POSITION_LENGTH_PER_UNIT *           BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		    mPlanetColorsBuffer[i]    = ByteBuffer.allocateDirect(COLOR_LENGTH_PER_UNIT    *           BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		    mPlanetTexturesBuffer[i]  = ByteBuffer.allocateDirect(TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
	}
	
	public void updateDrawData() {
		
		for (Planet planet : PlanetSet) {
			int idx = planet.getIdx();
			
		    mPlanetPositionsBuffer[idx].position(0);
			mPlanetColorsBuffer[idx].position(0);
			mPlanetTexturesBuffer[idx].position(0);
			
			Geocentric position = planet.getCoords();
			Geocentric u = MathUtils.normalize(MathUtils.crossProduct(position, Z_UP_VECTOR));
			Geocentric v = MathUtils.crossProduct(u, position);
			
			float sizer = planet.getScale() * mRenderer.mPointSizeFactor;
			Geocentric sized_u = new Geocentric(u.x * sizer, u.y * sizer, u.z * sizer);
			Geocentric sized_v = new Geocentric(v.x * sizer, v.y * sizer, v.z * sizer);
			
			float[] bottomLeft  = { position.x - sized_u.x - sized_v.x,
								    position.y - sized_u.y - sized_v.y,
								    position.z - sized_u.z - sized_v.z };
			float[] topLeft     = { position.x - sized_u.x + sized_v.x,
								    position.y - sized_u.y + sized_v.y,
								    position.z - sized_u.z + sized_v.z };
			float[] bottomRight = { position.x + sized_u.x - sized_v.x,
									position.y + sized_u.y - sized_v.y,
									position.z + sized_u.z - sized_v.z };
			float[] topRight    = { position.x + sized_u.x + sized_v.x,
									position.y + sized_u.y + sized_v.y,
									position.z + sized_u.z + sized_v.z };
			
			// Inserting first triangle (counter-clockwise)
			mPlanetPositionsBuffer[idx].put(topLeft);
			mPlanetPositionsBuffer[idx].put(bottomLeft);
			mPlanetPositionsBuffer[idx].put(topRight);
			// Inserting second triangle (counter-clockwise)
			mPlanetPositionsBuffer[idx].put(bottomLeft);
			mPlanetPositionsBuffer[idx].put(bottomRight);
			mPlanetPositionsBuffer[idx].put(topRight);
			
			// Inserting color per Planet (white)
			mPlanetColorsBuffer[idx].put(PLANET_COLOR_DATA_ARRAY);
			
			// Inserting texture coordinates per Planet
			mPlanetTexturesBuffer[idx].put(TEX_COORD_ARRAY);
			
			mPlanetPositionsBuffer[idx].position(0);
			mPlanetColorsBuffer[idx].position(0);
			mPlanetTexturesBuffer[idx].position(0);	
		}
		Log.d("END", "end of setup");
	}
	
	public void drawPlanet(int positionHandle, int colorHandle, int textureCoordinateHandle, int idx) {
		
		// pass in position information
		mPlanetPositionsBuffer[idx].position(0);
		GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, POSITION_STRIDE_BYTES, mPlanetPositionsBuffer[idx]);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		// pass in the color information
		mPlanetColorsBuffer[idx].position(0);
		GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, COLOR_STRIDE_BYTES, mPlanetColorsBuffer[idx]);
		GLES20.glEnableVertexAttribArray(colorHandle);
		
		// pass in the texture coordinate information
		mPlanetTexturesBuffer[idx].position(0);
		GLES20.glVertexAttribPointer(textureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, TEXTURE_COORDINATE_STRIDE_BYTES, mPlanetTexturesBuffer[idx]);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
		
		// draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, INDICES_PER_UNIT);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
	}
	
	public void createLabels() {
		for (Planet planet : PlanetSet) {
			mRenderer.mLabelManager.addLabel(planet.getName(), planet.getCoords(), PLANET_COLOR, PLANET_TEXTSIZE, LabelTypeEnum.PLANET);
		}
	}

}
