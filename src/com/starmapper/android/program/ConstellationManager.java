package com.starmapper.android.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.starmapper.android.celestial.ConstLine;
import com.starmapper.android.celestial.Constellation;
import com.starmapper.android.celestial.Star;
import com.starmapper.android.constants.ArrayConstants;
import com.starmapper.android.constants.BayerFileConstants;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;
import com.starmapper.android.utils.MathUtils;
import com.starmapper.android.utils.RawResourceUtils;

public class ConstellationManager implements BayerFileConstants , ArrayConstants , MathConstants {

	// Constellations to render
	Set<Constellation> ConstellationSet;
	
	// Total numbers of constellation data in Manager
	private int mNumStars;
	private int mNumConstLineObjs;

	// Buffers for OpenGL drawing
	private FloatBuffer mStarPositionsBuffer;
	private FloatBuffer mStarColorsBuffer;
	private FloatBuffer mStarTexturesBuffer;
	private FloatBuffer mConstLinePositionsBuffer;
	private FloatBuffer mConstLineColorsBuffer;
	private FloatBuffer mConstLineTexturesBuffer;
	
	// Renderer
	private StarMapperRenderer mRenderer;
	
	// Star Texture Coordinate Array (generic star)
/*	private final float[] TexCoordArray = {
			0.0f, 0.0f,
			0.0f, 1.0f,
			0.5f, 0.0f,
			0.0f, 1.0f,
			0.5f, 1.0f,
			0.5f, 0.0f,
	};
*/
	
	// Constructor
	public ConstellationManager(StarMapperRenderer renderer) {
		ConstellationSet = new LinkedHashSet<Constellation>();
		mNumStars = 0;
		mNumConstLineObjs = 0;
		mRenderer = renderer;
	}
	
	public void BuildConstellationsFromRawResource(final Context context, final int resourceID) {
		ArrayList<String> ConstellationArrayList = RawResourceUtils.genConstellationDataArrayFromRawResource(context, resourceID);
		for (String line : ConstellationArrayList) {
			Constellation constellation = new Constellation();
			String[] lineDataArray = line.split(":");
			constellation.addName(lineDataArray[CONSTELLATION_NAME]);
			for (int i = 1; i < lineDataArray.length - 1; i++) {
				Star star = new Star();
				String[] lineData = lineDataArray[i].split(" ");
				star.setName(lineData[STAR_NAME]);
				star.setStarID(Integer.parseInt(lineData[STAR_ID]));
				float mag = Float.parseFloat(lineData[MAGNITUDE]);
				// mag scaling done here, mag range is 6.0(min) -> (-1.5)(max)
				// mag val 6.0 is given 10 pixel size, mag val (-1.5) is given 50 pixel size
				mag = (MAGNITUDE_SCALE * (Math.abs(mag - MIN_MAGNITUDE) / MAGNITUDE_RANGE)) + POINT_SIZE_MIN_PIXELS;
				star.setMagnitude(mag);
				star.setUsingRaDec(new RaDec(Integer.parseInt(lineData[RA_HOUR]),
										Integer.parseInt(lineData[RA_MINUTE]),
										Integer.parseInt(lineData[RA_SECOND]),
										Integer.parseInt(lineData[DEC_DEGREE]),
										Integer.parseInt(lineData[DEC_MINUTE]),
										Integer.parseInt(lineData[DEC_SECOND])));
				constellation.addStar(star);
			}
			// line connection data for the constellation
			String[] connection_data = lineDataArray[lineDataArray.length - 1].split(",");
			for (String connection : connection_data) {
				String[] starIDArray = connection.split("-");
				int starID_1 = Integer.parseInt(starIDArray[STAR_1]);
				int starID_2 = Integer.parseInt(starIDArray[STAR_2]);
				Geocentric vertex1 = new Geocentric();
				Geocentric vertex2 = new Geocentric();
				for (Star star : constellation.getStars()) {
					if (star.starID == starID_1) {
						vertex1 = star.geoCoords;
					} else if (star.starID == starID_2) {
						vertex2 = star.geoCoords;
					}
				}
				ConstLine constLine = new ConstLine(vertex1, vertex2);
				constellation.addLine(constLine);
			}
			
			ConstellationSet.add(constellation);
		}
	}
	
	public void initializeBuffers() {
		mNumStars = 0;
		mNumConstLineObjs = 0;
		for (Constellation constellation : ConstellationSet) {
			mNumStars += constellation.getNumStars();
			mNumConstLineObjs += constellation.getNumLines();
		}
		
		mStarPositionsBuffer = ByteBuffer.allocateDirect(mNumStars * POSITION_LENGTH_PER_UNIT            * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mStarColorsBuffer    = ByteBuffer.allocateDirect(mNumStars * COLOR_LENGTH_PER_UNIT               * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mStarTexturesBuffer  = ByteBuffer.allocateDirect(mNumStars * TEXTURE_COORDINATE_LENGTH_PER_UNIT  * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mConstLinePositionsBuffer = ByteBuffer.allocateDirect(mNumConstLineObjs * POSITION_LENGTH_PER_UNIT			 * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mConstLineColorsBuffer    = ByteBuffer.allocateDirect(mNumConstLineObjs * COLOR_LENGTH_PER_UNIT              * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mConstLineTexturesBuffer  = ByteBuffer.allocateDirect(mNumConstLineObjs * TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();		
	}
	
	public void updateDrawData() {
		
		mNumStars = 0;
		mNumConstLineObjs = 0;
		for (Constellation constellation : ConstellationSet) {
			mNumStars += constellation.getNumStars();
			mNumConstLineObjs += constellation.getNumLines();
		}
		mStarPositionsBuffer.position(0);
		mStarColorsBuffer.position(0);
		mStarTexturesBuffer.position(0);
		mConstLinePositionsBuffer.position(0);
		mConstLineColorsBuffer.position(0);
		mConstLineTexturesBuffer.position(0);
				
		for (Constellation constellation : ConstellationSet) {
			// Update the star data
			for (Star star : constellation.getStars()) {
			    Geocentric position = star.getCoords();
			    Geocentric u = MathUtils.normalize(MathUtils.crossProduct(position, Z_UP_VECTOR));
			    Geocentric v = MathUtils.crossProduct(u, position);
			    
			    float sizer = star.magnitude * mRenderer.mPointSizeFactor;
			    //Log.d("LABELSIZE", "mRenderer.mPointSizeFactor : " + mRenderer.mPointSizeFactor);
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
/*			    
			    float[] bottomLeft  = { 0.5f, -0.05f,  0.05f };
			    float[] topLeft     = { 0.5f,  0.05f,  0.05f };
			    float[] bottomRight = { 0.5f, -0.05f, -0.05f };
			    float[] topRight    = { 0.5f,  0.05f, -0.05f };
*/			    
			    // Inserting first triangle (counter-clockwise)
			    mStarPositionsBuffer.put(topLeft);
			    mStarPositionsBuffer.put(bottomLeft);
			    mStarPositionsBuffer.put(topRight);
			    // Inserting second triangle (counter-clockwise)
			    mStarPositionsBuffer.put(bottomLeft);
			    mStarPositionsBuffer.put(bottomRight);
			    mStarPositionsBuffer.put(topRight);
			    
			    // Inserting Color per Star (white)
			    mStarColorsBuffer.put(STAR_COLOR_DATA_ARRAY);
			    
			    // Inserting Texture Coordinates per Star
			    mStarTexturesBuffer.put(TEX_COORD_ARRAY);
			}
			// Update the const line data
			for (ConstLine line : constellation.getLines()) {
				Geocentric geo1 = line.getVertex1();
				Geocentric geo2 = line.getVertex2();
				Geocentric u    = MathUtils.sub(geo2, geo1);
				Geocentric mid  = MathUtils.add(geo1, geo2);
				mid.resize(0.5f);
				Geocentric v    = MathUtils.normalize(MathUtils.crossProduct(u, mid));
				v.resize(LINE_SIZE_FACTOR);
				
				Geocentric bl = MathUtils.sub(geo1, v);
				Geocentric tl = MathUtils.add(geo1, v);
				Geocentric br = MathUtils.sub(geo2, v);
				Geocentric tr = MathUtils.add(geo2, v);
				
				float[] bottomLeft  = { bl.x, bl.y, bl.z };
				float[] topLeft     = { tl.x, tl.y, tl.z };
				float[] bottomRight = { br.x, br.y, br.z };
				float[] topRight    = { tr.x, tr.y, tr.z };
				
				// first triangle (counter-clockwise)
				mConstLinePositionsBuffer.put(topLeft);
				mConstLinePositionsBuffer.put(bottomLeft);
				mConstLinePositionsBuffer.put(topRight);
				// second triangle (counter-clockwise)
				mConstLinePositionsBuffer.put(bottomLeft);
				mConstLinePositionsBuffer.put(bottomRight);
				mConstLinePositionsBuffer.put(topRight);
				
				// color per line
				mConstLineColorsBuffer.put(CONST_LINE_COLOR_DATA_ARRAY);
				
				// textures per line
				mConstLineTexturesBuffer.put(TEX_COORD_ARRAY);
			}	
		}
		
		mStarPositionsBuffer.position(0);
		mStarColorsBuffer.position(0);
		mStarTexturesBuffer.position(0);
		mConstLinePositionsBuffer.position(0);
		mConstLineColorsBuffer.position(0);
		mConstLineTexturesBuffer.position(0);
	}
	
	public void drawConstellations(int positionHandle, int colorHandle, int textureCoordinateHandle) {
		
		// pass in position information
		mStarPositionsBuffer.position(0);
		GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, POSITION_STRIDE_BYTES, mStarPositionsBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		// pass in the color information
		mStarColorsBuffer.position(0);
		GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, COLOR_STRIDE_BYTES, mStarColorsBuffer);
		GLES20.glEnableVertexAttribArray(colorHandle);
		
		// pass in the texture coordinate information
		mStarTexturesBuffer.position(0);
		GLES20.glVertexAttribPointer(textureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, TEXTURE_COORDINATE_STRIDE_BYTES, mStarTexturesBuffer);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
		
		// draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumStars * INDICES_PER_UNIT);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
	}
	
	public void drawConstLines(int positionHandle, int colorHandle, int textureCoordinateHandle) {
		
		// pass in position information
		mConstLinePositionsBuffer.position(0);
		GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, POSITION_STRIDE_BYTES, mConstLinePositionsBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		// pass in color information
		mConstLineColorsBuffer.position(0);
		GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, COLOR_STRIDE_BYTES, mConstLineColorsBuffer);
		GLES20.glEnableVertexAttribArray(colorHandle);
		
		// pass in texture information
		mConstLineTexturesBuffer.position(0);
		GLES20.glVertexAttribPointer(textureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, TEXTURE_COORDINATE_STRIDE_BYTES, mConstLineTexturesBuffer);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
		
		// draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumConstLineObjs * INDICES_PER_UNIT);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
	}
	
	public void createLabels() {
		for (Constellation constellation : ConstellationSet) {
			// finding the center of each constellation, label will go there
			ArrayList<Float> RAlist = new ArrayList<Float>();
			ArrayList<Float> Declist = new ArrayList<Float>();
			
			for (Star star : constellation.getStars()) {
				// check for named stars in the constellation, they will have labels too
				for (NamedStarsEnum starEnum : NamedStarsEnum.values()) {
					String namedStar = starEnum.name();
					if (star.name.equalsIgnoreCase(namedStar)) {
						mRenderer.mLabelManager.addLabel(star.name, star.getCoords(), STAR_COLOR, STAR_TEXTSIZE, LabelTypeEnum.STAR);
						break;
					}
				}
				RAlist.add(star.getRA()); Declist.add(star.getDec());
			}
			Collections.sort(RAlist);
			Collections.sort(Declist);
			int midRA = (int) Math.ceil(RAlist.size() / 2);
			int midDec = (int) Math.ceil(Declist.size() / 2);
			Geocentric labelPos = new Geocentric(new RaDec(RAlist.get(midRA), Declist.get(midDec)));
			labelPos = MathUtils.normalize(labelPos);
			String name = constellation.getName();
			mRenderer.mLabelManager.addLabel(name, labelPos, CONSTELLATION_COLOR, CONSTELLATION_TEXTSIZE, LabelTypeEnum.CONSTELLATION);
		}
	}
}
