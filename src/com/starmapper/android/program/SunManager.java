package com.starmapper.android.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.opengl.GLES20;

import com.starmapper.android.celestial.Sun;
import com.starmapper.android.constants.ArrayConstants;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.constants.OrbitalElementsConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.utils.MathUtils;

public class SunManager implements MathConstants, ArrayConstants, OrbitalElementsConstants {

	// The Sun object
	private Sun sun;
	
	// Buffers for OpenGL drawing
	private FloatBuffer mSunPositionBuffer;
	private FloatBuffer mSunColorBuffer;
	private FloatBuffer mSunTextureBuffer;
	
	// Renderer
	private StarMapperRenderer mRenderer;
	
	// Calendars
	private final GregorianCalendar J2000_epoch;
	private final GregorianCalendar currentCalendar;
	
	// delta time
	private final double mDaysSinceJ2000Epoch;
	
	
	
	// Constructor
	public SunManager(StarMapperRenderer renderer) {
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
	}
	
	public void BuildSunData() {
		/* All these hard-coded numbers taken from http://aa.usno.navy.mil/faq/docs/SunApprox.php */
		// g = mean anomaly
		double g = (357.529 + 0.98560028 * mDaysSinceJ2000Epoch) % 360.0f;
		double gRad = g * MathUtils.convertToRadians;
		// q = mean longitude
		double q = (280.459 + 0.98564736 * mDaysSinceJ2000Epoch) % 360.0f;
		// L = apparent ecliptic longitude
		double L = (q + 1.915 * MathUtils.sind(gRad) + 0.020 * MathUtils.sind(2.0 * gRad)) % 360.0f;
		double LRad = L * MathUtils.convertToRadians;
		
		// R = distance between Earth & Sun in AU
		//double R = 1.00014 - 0.01671 * MathUtils.cosd(gRad) - 0.00014 * MathUtils.cosd(2.0 * gRad);
		// e = mean obliquity of the Sun's ecliptic
		double e = (23.439 - 0.00000036 * mDaysSinceJ2000Epoch) % 360.0f;
		double eRad = e * MathUtils.convertToRadians;
		
		double sun_RA = (MathUtils.arctan2d(MathUtils.cosd(eRad) * MathUtils.sind(LRad), MathUtils.cosd(LRad))) * MathUtils.convertToDegrees;
		double sun_Dec = (MathUtils.arcsind(MathUtils.sind(eRad) * MathUtils.sind(LRad))) * MathUtils.convertToDegrees;
		
		sun = new Sun((float) sun_RA, (float) sun_Dec, SUN_SCALE_FACTOR);
	}
	
	public void initializeBuffers() {
		mSunPositionBuffer = ByteBuffer.allocateDirect(POSITION_LENGTH_PER_UNIT           * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mSunColorBuffer    = ByteBuffer.allocateDirect(COLOR_LENGTH_PER_UNIT              * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mSunTextureBuffer  = ByteBuffer.allocateDirect(TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();		
	}
	
	public void updateDrawData() {
	    mSunPositionBuffer.position(0);
		mSunColorBuffer.position(0);
		mSunTextureBuffer.position(0);
		
		Geocentric position = sun.getCoords();
		Geocentric u = MathUtils.normalize(MathUtils.crossProduct(position, Z_UP_VECTOR));
		Geocentric v = MathUtils.crossProduct(u, position);
		
		float sizer = sun.getScale() * mRenderer.mPointSizeFactor;
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
		mSunPositionBuffer.put(topLeft);
		mSunPositionBuffer.put(bottomLeft);
		mSunPositionBuffer.put(topRight);
		// Inserting second triangle (counter-clockwise)
		mSunPositionBuffer.put(bottomLeft);
		mSunPositionBuffer.put(bottomRight);
		mSunPositionBuffer.put(topRight);
		
		// Inserting color for Sun (white)
		mSunColorBuffer.put(SUN_COLOR_DATA_ARRAY);
		
		// Inserting texture coordinates per Planet
		mSunTextureBuffer.put(TEX_COORD_ARRAY);
		
		mSunPositionBuffer.position(0);
		mSunColorBuffer.position(0);
		mSunTextureBuffer.position(0);	
	}

    public void drawSun(int positionHandle, int colorHandle, int textureCoordinateHandle) {
		// pass in position information
		mSunPositionBuffer.position(0);
		GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, POSITION_STRIDE_BYTES, mSunPositionBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		// pass in the color information
		mSunColorBuffer.position(0);
		GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, COLOR_STRIDE_BYTES, mSunColorBuffer);
		GLES20.glEnableVertexAttribArray(colorHandle);
		
		// pass in the texture coordinate information
		mSunTextureBuffer.position(0);
		GLES20.glVertexAttribPointer(textureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, TEXTURE_COORDINATE_STRIDE_BYTES, mSunTextureBuffer);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
		
		// draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, INDICES_PER_UNIT);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
   	
    }
    
    public void createLabels() {
    	mRenderer.mLabelManager.addLabel("Sun", sun.getCoords(), SUN_COLOR, SUN_TEXTSIZE, LabelTypeEnum.SUN);
    }
}
