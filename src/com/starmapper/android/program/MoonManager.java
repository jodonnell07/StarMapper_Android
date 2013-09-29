package com.starmapper.android.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.opengl.GLES20;

import com.starmapper.android.celestial.Moon;
import com.starmapper.android.constants.ArrayConstants;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.constants.OrbitalElementsConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.utils.MathUtils;

public class MoonManager implements MathConstants, ArrayConstants, OrbitalElementsConstants {

	// The Moon object
	private Moon moon;
	
	// Buffers for OpenGL drawing
	private FloatBuffer mMoonPositionBuffer;
	private FloatBuffer mMoonColorBuffer;
	private FloatBuffer mMoonTextureBuffer;
	
	// Renderer
	private StarMapperRenderer mRenderer;
	
	// Calendars
	private final GregorianCalendar J2000_epoch;
	private final GregorianCalendar currentCalendar;
	
	// delta time
	private final double mDaysSinceJ2000Epoch;
	//private final double mCenturiesSinceJ2000Epoch;
	
	// Orbital Elements Constants based on current date/time
	private final double[] mMoonOrbitalElementsConstants;
	
	// Obliquity of the J2000 Ecliptic
	private final double mJ2000EclipticObliquityRad;
	
	// Constructor
	public MoonManager(StarMapperRenderer renderer) {
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
		//mCenturiesSinceJ2000Epoch = mDaysSinceJ2000Epoch / 36525.0f;
		
		mJ2000EclipticObliquityRad = J2000_ECLIPTIC_OBLIQUITY * MathUtils.convertToRadians;
		
		mMoonOrbitalElementsConstants = new double[NUM_ORBITAL_ELEMENTS];
		
		mMoonOrbitalElementsConstants[MEAN_DISTANCE]   = MoonMeanOrbitalElementsConstants[MEAN_DISTANCE] + MoonOrbitalElementsRatesOfChange[MEAN_DISTANCE] * mDaysSinceJ2000Epoch;
		mMoonOrbitalElementsConstants[ECCENTRICITY]    = MoonMeanOrbitalElementsConstants[ECCENTRICITY] + MoonOrbitalElementsRatesOfChange[ECCENTRICITY] * mDaysSinceJ2000Epoch;
		mMoonOrbitalElementsConstants[INCLINATION]     = MoonMeanOrbitalElementsConstants[INCLINATION] + MoonOrbitalElementsRatesOfChange[INCLINATION] * mDaysSinceJ2000Epoch;
		mMoonOrbitalElementsConstants[ANODE_LONGITUDE] = MoonMeanOrbitalElementsConstants[ANODE_LONGITUDE] + MoonOrbitalElementsRatesOfChange[ANODE_LONGITUDE] * mDaysSinceJ2000Epoch;
		mMoonOrbitalElementsConstants[PERI_LONGITUDE]  = MoonMeanOrbitalElementsConstants[PERI_LONGITUDE] + MoonOrbitalElementsRatesOfChange[PERI_LONGITUDE] * mDaysSinceJ2000Epoch;
		mMoonOrbitalElementsConstants[MEAN_LONGITUDE]    = MoonMeanOrbitalElementsConstants[MEAN_LONGITUDE] + MoonOrbitalElementsRatesOfChange[MEAN_LONGITUDE] * mDaysSinceJ2000Epoch;
		if (mMoonOrbitalElementsConstants[ANODE_LONGITUDE] < 0.0) { mMoonOrbitalElementsConstants[ANODE_LONGITUDE] += 360.0; }
		if (mMoonOrbitalElementsConstants[PERI_LONGITUDE] < 0.0) { mMoonOrbitalElementsConstants[PERI_LONGITUDE] += 360.0; }
		if (mMoonOrbitalElementsConstants[MEAN_ANOMALY] < 0.0) { mMoonOrbitalElementsConstants[MEAN_ANOMALY] += 360.0; }
	}
	
	public void BuildMoonData() {
				
		// Moon Orbital Elements
		// TODO: Move these into OrbitalElementsConstants
		double moon_N = 125.1228 - 0.0529538083 * mDaysSinceJ2000Epoch;
		if (moon_N < 0.0) { moon_N += 360.0; }
		double moon_i = 5.1454;
		double moon_w = (318.0634 + 0.1643573223 * mDaysSinceJ2000Epoch) % 360.0;
		double moon_a = 60.2666; //Earth radii
		double moon_e = 0.0549;
		double moon_M = (MOON_OFFSET_FUDGE_ADDER + 115.3654 + 13.0649929509 * mDaysSinceJ2000Epoch) % 360.0;
		
		double moon_MRad = moon_M * MathUtils.convertToRadians;
		
		double moon_E = moon_M + moon_e * MathUtils.sind(moon_MRad) * (1.0 + moon_e * MathUtils.cosd(moon_MRad));
		double moon_ERad = moon_E * MathUtils.convertToRadians;
		
		for (int i = 0; i < 50; i++) {
		    moon_E = moon_E - (moon_E - moon_e * MathUtils.sind(moon_ERad) - moon_M) / (1.0 - moon_e * MathUtils.cosd(moon_ERad));
		    moon_ERad = moon_E * MathUtils.convertToRadians;
		}
		
		double moon_xv = moon_a * (MathUtils.cosd(moon_ERad) - moon_e);
		double moon_yv = moon_a * (Math.sqrt(1.0 - moon_e * moon_e) * MathUtils.sind(moon_ERad));
		
		double moon_v = (MathUtils.arctan2d(moon_yv, moon_xv)) * MathUtils.convertToDegrees;
		if (moon_v < 0.0) { moon_v += 360.0; }
		double moon_r = Math.sqrt(moon_xv * moon_xv + moon_yv * moon_yv);
		
		double moon_NRad = moon_N * MathUtils.convertToRadians;
		double moon_vRad = moon_v * MathUtils.convertToRadians;
		double moon_wRad = moon_w * MathUtils.convertToRadians;
		double moon_iRad = moon_i * MathUtils.convertToRadians;
		
		double moon_xg = moon_r * (MathUtils.cosd(moon_NRad) * MathUtils.cosd(moon_vRad + moon_wRad) - MathUtils.sind(moon_NRad) * MathUtils.sind(moon_vRad + moon_wRad) * MathUtils.cosd(moon_iRad));
		double moon_yg = moon_r * (MathUtils.sind(moon_NRad) * MathUtils.cosd(moon_vRad + moon_wRad) + MathUtils.cosd(moon_NRad) * MathUtils.sind(moon_vRad + moon_wRad) * MathUtils.cosd(moon_iRad));
		double moon_zg = moon_r * (MathUtils.sind(moon_vRad + moon_wRad) * MathUtils.sind(moon_iRad));
		
		double moon_lonecl = (MathUtils.arctan2d(moon_yg, moon_xg)) * MathUtils.convertToDegrees;
		if (moon_lonecl < 0.0) { moon_lonecl += 360.0; }
		double moon_latecl = (MathUtils.arctan2d(moon_zg, Math.sqrt(moon_xg * moon_xg + moon_yg * moon_yg))) * MathUtils.convertToDegrees;
		if (moon_latecl < 0.0) { moon_latecl += 360.0; }
		double moon_loneclRad = moon_lonecl * MathUtils.convertToRadians;
		double moon_lateclRad = moon_latecl * MathUtils.convertToRadians;
		
		double new_moon_xg = moon_r * MathUtils.cosd(moon_loneclRad) * MathUtils.cosd(moon_lateclRad);
		double new_moon_yg = moon_r * MathUtils.sind(moon_loneclRad) * MathUtils.cosd(moon_lateclRad);
		double new_moon_zg = moon_r * MathUtils.sind(moon_lateclRad);
		
/*		// Because Mercury
		double sun_x = sun_r * MathUtils.cosd(sun_lonsunRad);
		double sun_y = sun_r * MathUtils.sind(sun_lonsunRad);
		new_moon_xg = new_moon_xg + sun_x;
		new_moon_yg = new_moon_yg + sun_y;
		new_moon_zg = new_moon_zg;
		// End Because Mercury
*/		
		double moon_xe = new_moon_xg;
		double moon_ye = new_moon_yg * MathUtils.cosd(mJ2000EclipticObliquityRad) - new_moon_zg * MathUtils.sind(mJ2000EclipticObliquityRad);
		double moon_ze = new_moon_yg * MathUtils.sind(mJ2000EclipticObliquityRad) + new_moon_zg * MathUtils.cosd(mJ2000EclipticObliquityRad);
		
		double moon_RA = (MathUtils.arctan2d(moon_ye, moon_xe)) * MathUtils.convertToDegrees;
		if (moon_RA < 0.0) { moon_RA += 360.0; }
		int moon_RA_Hours = (int) (moon_RA / 15.0);
		int moon_RA_Minutes = (int) ((moon_RA % 15.0) / 15.0 * 60.0);
		double moon_Dec = (MathUtils.arctan2d(moon_ze, Math.sqrt(moon_xe * moon_xe + moon_ye * moon_ye))) * MathUtils.convertToDegrees;
		
		double moon_geoDistance = Math.sqrt(moon_xe*moon_xe + moon_ye*moon_ye + moon_ze*moon_ze);
		
		moon = new Moon((float) moon_RA, (float) moon_Dec, MOON_SCALE_FACTOR);
	
		/* TODO: Use the moon's topocentric position, as opposed to geocentric */
	}
	
	public void initializeBuffers() {
		mMoonPositionBuffer = ByteBuffer.allocateDirect(POSITION_LENGTH_PER_UNIT           * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mMoonColorBuffer    = ByteBuffer.allocateDirect(COLOR_LENGTH_PER_UNIT              * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mMoonTextureBuffer  = ByteBuffer.allocateDirect(TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();		
	}

	public void updateDrawData() {
	    mMoonPositionBuffer.position(0);
		mMoonColorBuffer.position(0);
		mMoonTextureBuffer.position(0);
		
		Geocentric position = moon.getCoords();
		Geocentric u = MathUtils.normalize(MathUtils.crossProduct(position, Z_UP_VECTOR));
		Geocentric v = MathUtils.crossProduct(u, position);
		
		float sizer = moon.getScale() * mRenderer.mPointSizeFactor;
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
		mMoonPositionBuffer.put(topLeft);
		mMoonPositionBuffer.put(bottomLeft);
		mMoonPositionBuffer.put(topRight);
		// Inserting second triangle (counter-clockwise)
		mMoonPositionBuffer.put(bottomLeft);
		mMoonPositionBuffer.put(bottomRight);
		mMoonPositionBuffer.put(topRight);
		
		// Inserting color for Moon (white)
		mMoonColorBuffer.put(MOON_COLOR_DATA_ARRAY);
		
		// Inserting texture coordinates per Planet
		mMoonTextureBuffer.put(TEX_COORD_ARRAY);
		
		mMoonPositionBuffer.position(0);
		mMoonColorBuffer.position(0);
		mMoonTextureBuffer.position(0);	
	}

    public void drawMoon(int positionHandle, int colorHandle, int textureCoordinateHandle) {
		// pass in position information
		mMoonPositionBuffer.position(0);
		GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, POSITION_STRIDE_BYTES, mMoonPositionBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		// pass in the color information
		mMoonColorBuffer.position(0);
		GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, COLOR_STRIDE_BYTES, mMoonColorBuffer);
		GLES20.glEnableVertexAttribArray(colorHandle);
		
		// pass in the texture coordinate information
		mMoonTextureBuffer.position(0);
		GLES20.glVertexAttribPointer(textureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, TEXTURE_COORDINATE_STRIDE_BYTES, mMoonTextureBuffer);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
		
		// draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, INDICES_PER_UNIT);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
   	
    }

}
