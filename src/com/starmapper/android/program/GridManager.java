package com.starmapper.android.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import android.opengl.GLES20;

import com.starmapper.android.constants.ArrayConstants;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.grid.Grid;
import com.starmapper.android.grid.GridLine;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;
import com.starmapper.android.utils.MathUtils;

public class GridManager implements ArrayConstants, MathConstants {

	// The RA/Dec grid object
	private final Grid skyGrid;
	
	// Total number of line objs in the grid
	private int mNumLineObjs;
	
	// Buffers for OpenGL drawing
	private FloatBuffer mLinePositionsBuffer;
	private FloatBuffer mLineColorsBuffer;
	private FloatBuffer mLineTexturesBuffer;
	
	// Renderer
	private StarMapperRenderer mRenderer;
		
	// Constructor
	public GridManager(StarMapperRenderer renderer) {
		skyGrid = new Grid();
		mRenderer = renderer;
	}
	
	public void BuildGrid() {
		skyGrid.buildGrid(RA_LINES, DEC_LINES);
	}
	
	public void initializeBuffers() {
		mNumLineObjs = (skyGrid.getNumRALines() * (RA_VERTICES - 1)) + (skyGrid.getNumDecLines() * DEC_VERTICES);
		
		mLinePositionsBuffer = ByteBuffer.allocateDirect(mNumLineObjs * POSITION_LENGTH_PER_UNIT           * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mLineColorsBuffer    = ByteBuffer.allocateDirect(mNumLineObjs * COLOR_LENGTH_PER_UNIT              * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mLineTexturesBuffer  = ByteBuffer.allocateDirect(mNumLineObjs * TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();		
	}
	
	public void updateDrawData() {
		
		// line segments * number of lines = number of line objects, "+1" for last vertex in Dec line back to origin
		mNumLineObjs = (skyGrid.getNumRALines() * (RA_VERTICES - 1)) + (skyGrid.getNumDecLines() * DEC_VERTICES);
		
		ArrayList<GridLine> allLines = new ArrayList<GridLine>();
		allLines.addAll(skyGrid.getRALines());
		allLines.addAll(skyGrid.getDecLines());
		mLinePositionsBuffer.position(0);
		mLineColorsBuffer.position(0);
		mLineTexturesBuffer.position(0);
		
		for (GridLine line : allLines) {
			ArrayList<Geocentric> vertices = line.getVertices();
			
			for (int i = 0; i < vertices.size() - 1; i++) {
				Geocentric geo1 = vertices.get(i);
				Geocentric geo2 = vertices.get(i + 1);
				Geocentric u    = MathUtils.sub(geo2, geo1);
				Geocentric mid  = MathUtils.add(geo1, geo2);
				mid.resize(0.5f);
				Geocentric v    = MathUtils.normalize(MathUtils.crossProduct(u, mid));
				v.resize(mRenderer.mLineSizeFactor);
				
				Geocentric bl = MathUtils.sub(geo1, v);
				Geocentric tl = MathUtils.add(geo1, v);
				Geocentric br = MathUtils.sub(geo2, v);
				Geocentric tr = MathUtils.add(geo2, v);
				
				float[] bottomLeft  = { bl.x, bl.y, bl.z };
				float[] topLeft     = { tl.x, tl.y, tl.z };
				float[] bottomRight = { br.x, br.y, br.z };
				float[] topRight    = { tr.x, tr.y, tr.z };
				
				// first triangle (counter-clockwise)
				mLinePositionsBuffer.put(topLeft);
				mLinePositionsBuffer.put(bottomLeft);
				mLinePositionsBuffer.put(topRight);
				// second triangle (counter-clockwise)
				mLinePositionsBuffer.put(bottomLeft);
				mLinePositionsBuffer.put(bottomRight);
				mLinePositionsBuffer.put(topRight);
				
				// color per line
				mLineColorsBuffer.put(GRID_COLOR_DATA_ARRAY);
				
				// texture per line
				mLineTexturesBuffer.put(TEX_COORD_ARRAY);
				
			}
		}
        allLines.clear();
		mLinePositionsBuffer.position(0);
		mLineColorsBuffer.position(0);
		mLineTexturesBuffer.position(0);
	}
	
	public void drawGrid(int positionHandle, int colorHandle, int textureCoordinateHandle) {
		
		// pass in position information
		mLinePositionsBuffer.position(0);
		GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, POSITION_STRIDE_BYTES, mLinePositionsBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		// pass in color information
		mLineColorsBuffer.position(0);
		GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, COLOR_STRIDE_BYTES, mLineColorsBuffer);
		GLES20.glEnableVertexAttribArray(colorHandle);
		
		// pass in texture information
		mLineTexturesBuffer.position(0);
		GLES20.glVertexAttribPointer(textureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, TEXTURE_COORDINATE_STRIDE_BYTES, mLineTexturesBuffer);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
		
		// draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumLineObjs * INDICES_PER_UNIT);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
	}
	
	public void createLabels() {
		// currently hard-coding positions of grid labels to RA = (0h, 6h, 12h, 18h), Dec = 0 degrees
		mRenderer.mLabelManager.addLabel("0h", new Geocentric(new RaDec(0,0,0,0,0,0)), GRID_COLOR, GRID_TEXTSIZE, LabelTypeEnum.GRID);
		mRenderer.mLabelManager.addLabel("6h", new Geocentric(new RaDec(6,0,0,0,0,0)), GRID_COLOR, GRID_TEXTSIZE, LabelTypeEnum.GRID);
		mRenderer.mLabelManager.addLabel("12h", new Geocentric(new RaDec(12,0,0,0,0,0)), GRID_COLOR, GRID_TEXTSIZE, LabelTypeEnum.GRID);
		mRenderer.mLabelManager.addLabel("18h", new Geocentric(new RaDec(18,0,0,0,0,0)), GRID_COLOR, GRID_TEXTSIZE, LabelTypeEnum.GRID);
	}
}
