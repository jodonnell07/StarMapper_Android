package com.starmapper.android.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import android.opengl.GLES20;

import com.starmapper.android.celestial.Star;
import com.starmapper.android.constants.ArrayConstants;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;
import com.starmapper.android.utils.MathUtils;

public class BGStarManager implements ArrayConstants, MathConstants {

	// Background (BG) stars to render
	Set<Star> BGStarSet;
	
	private final int mNumBGStars;
	
	// Buffers for OpenGL Data
	private FloatBuffer mBGStarPositionsBuffer;
	private FloatBuffer mBGStarColorsBuffer;
	private FloatBuffer mBGStarTexturesBuffer;
	
	// Renderer
	private StarMapperRenderer mRenderer;
	
	// Constructor
	public BGStarManager(StarMapperRenderer renderer) {
		BGStarSet = new LinkedHashSet<Star>();
		mNumBGStars = 500;
		mRenderer = renderer;
	}
	
	public void BuildBGStars() {
		for (int i = 0; i < mNumBGStars; i++) {
			// random.nextFloat() will return [0.0, 1.0)
			Random random = new Random();
			// RA must be 0 < RA < 360 degrees
			float randomRA = random.nextFloat() * 360.0f;
			// Declination is weighted to make background stars more visually appealing and spaced out
			/* randomCent Values:
			 * 0-3   : 0
			 * 4-9  : 1
			 * 10-18 : 2
			 * 19-30 : 3
			 * 31-42 : 4
			 * 43-57 : 5
			 * 58-69 : 6
			 * 70-81 : 7
			 * 82-90 : 8
			 * 91-96 : 9
			 * 97-100 : 10
			 */
			float r = random.nextFloat() * 100.0f;
			float randomDecZone = 0;
			if (r <= 3)         { randomDecZone = 0; }
			else if (r <= 9)   { randomDecZone = 1; }
			else if (r <= 18)   { randomDecZone = 2; }
			else if (r <= 30)   { randomDecZone = 3; }
			else if (r <= 42)   { randomDecZone = 4; }
			else if (r <= 57)   { randomDecZone = 5; }
			else if (r <= 69)   { randomDecZone = 6; }
			else if (r <= 81)   { randomDecZone = 7; }
			else if (r <= 90)   { randomDecZone = 8; }
			else if (r <= 96)   { randomDecZone = 9; }
			else if (r <= 100)  { randomDecZone = 10; }
			
			// Declination range of one "DecZone" =~ 16.3635 degrees
			// algorithm is (decZone number) + (range within decZone) - (offset == 90.0)
			float randomDec = (randomDecZone * 16.3635f) + (random.nextFloat() * 16.3635f) - 90.0f;
			// Dec must be (-90) < Dec < +90 degrees
			RaDec randomRaDec = new RaDec(randomRA, randomDec);
			// magnitude of background stars should be small, trying (minMag = 6.0) to (maxMag = 4.5)
			float randomMag = (random.nextFloat() * 1.5f) + 4.5f;
			randomMag = (MAGNITUDE_SCALE * (Math.abs(randomMag - MIN_MAGNITUDE) / MAGNITUDE_RANGE)) + POINT_SIZE_MIN_PIXELS;
			
			Star star = new Star(randomRaDec);
			star.setMagnitude(randomMag);
			
			BGStarSet.add(star);
		}
	}
	
	public void initializeBuffers() {
		mBGStarPositionsBuffer = ByteBuffer.allocateDirect(mNumBGStars * POSITION_LENGTH_PER_UNIT				* BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mBGStarColorsBuffer    = ByteBuffer.allocateDirect(mNumBGStars * COLOR_LENGTH_PER_UNIT					* BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mBGStarTexturesBuffer  = ByteBuffer.allocateDirect(mNumBGStars * TEXTURE_COORDINATE_LENGTH_PER_UNIT		* BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	public void updateDrawData() {
		mBGStarPositionsBuffer.position(0);
		mBGStarColorsBuffer.position(0);
		mBGStarTexturesBuffer.position(0);
		
		// Update the star data
		for (Star star : BGStarSet) {
		    Geocentric position = star.getCoords();
		    Geocentric u = MathUtils.normalize(MathUtils.crossProduct(position, Z_UP_VECTOR));
		    Geocentric v = MathUtils.crossProduct(u, position);
		    
		    float sizer = star.magnitude * mRenderer.mPointSizeFactor;
		    Geocentric sized_u = new Geocentric(u.x * sizer, u.y * sizer, u.z * sizer);
		    Geocentric sized_v = new Geocentric(v.x * sizer, v.y * sizer, v.z * sizer);
		    
		    float[] bottomLeft =  { position.x - sized_u.x - sized_v.x,
		    				        position.y - sized_u.y - sized_v.y,
		    				        position.z - sized_u.z - sized_v.z };
		    float[] topLeft    =  { position.x - sized_u.x + sized_v.x,
		    					    position.y - sized_u.y + sized_v.y,
		    					    position.z - sized_u.z + sized_v.z };
		    float[] bottomRight = { position.x + sized_u.x - sized_v.x,
		    						position.y + sized_u.y - sized_v.y,
		    						position.z + sized_u.z - sized_v.z };
		    float[] topRight    = { position.x + sized_u.x + sized_v.x,
		    						position.y + sized_u.y + sized_v.y,
		    						position.z + sized_u.z + sized_v.z };
		    
		    // Inserting first triangle (counter-clockwise)
		    mBGStarPositionsBuffer.put(topLeft);
		    mBGStarPositionsBuffer.put(bottomLeft);
		    mBGStarPositionsBuffer.put(topRight);
		    // Inserting second triangle (counter-clockwise)
		    mBGStarPositionsBuffer.put(bottomLeft);
		    mBGStarPositionsBuffer.put(bottomRight);
		    mBGStarPositionsBuffer.put(topRight);
		    
		    // Inserting Color per Star (white)
		    mBGStarColorsBuffer.put(STAR_COLOR_DATA_ARRAY);
		    
		    // Inserting Texture Coordinates per Star
		    mBGStarTexturesBuffer.put(TEX_COORD_ARRAY);
		}
		mBGStarPositionsBuffer.position(0);
		mBGStarColorsBuffer.position(0);
		mBGStarTexturesBuffer.position(0);
	}
	
	public void drawBGStars(int positionHandle, int colorHandle, int textureCoordinateHandle) {
		
		// pass in position information
		mBGStarPositionsBuffer.position(0);
		GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, POSITION_STRIDE_BYTES, mBGStarPositionsBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		// pass in the color information
		mBGStarColorsBuffer.position(0);
		GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, COLOR_STRIDE_BYTES, mBGStarColorsBuffer);
		GLES20.glEnableVertexAttribArray(colorHandle);
		
		// pass in the texture coordinate information
		mBGStarTexturesBuffer.position(0);
		GLES20.glVertexAttribPointer(textureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, TEXTURE_COORDINATE_STRIDE_BYTES, mBGStarTexturesBuffer);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
		
		// draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumBGStars * INDICES_PER_UNIT);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(textureCoordinateHandle);

	}
}
