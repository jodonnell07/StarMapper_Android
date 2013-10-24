package com.starmapper.android.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.starmapper.android.constants.ArrayConstants;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.utils.Label;
import com.starmapper.android.utils.MathUtils;

public class LabelManager implements ArrayConstants, MathConstants {

	private StarMapperRenderer mRenderer;
	private Resources mRes;
		
	private int mBitmapWidth;
	private int mBitmapHeight;
	private float mTexelWidth;
	private float mTexelHeight;
	
	private float mTextOrigin_x;
	private float mTextOrigin_y;
	
	private Geocentric mTempPosition;
	private int mTempScale;
	
	private Bitmap mBitmap;
	private Canvas mCanvas;
	
	private Paint mTextPaint;
	
	private String mTestString;
	
	// buffers used for OpenGL drawing
	private FloatBuffer mLabelsPositionBuffer;
	private FloatBuffer mLabelsColorBuffer;
	private FloatBuffer mLabelsTextureBuffer;
	
	// All labels to be displayed will go here
	Set<Label> LabelSet;
	
	private int mNumberOfLabels;
	
	// Constructors
	public LabelManager(StarMapperRenderer renderer, Resources res) {
		mRenderer = renderer;
		mRes = res;
		
		LabelSet = new LinkedHashSet<Label>();
		
		// Real settings here
		mBitmapWidth = 1024;
		mBitmapHeight = 1024;
		mTexelWidth = (float) (1.0f / mBitmapWidth);
		mTexelHeight = (float) (1.0f / mBitmapHeight);
		
/*		// temporary values for testing
		mBitmapWidth = 256;
		mBitmapHeight = 256;
		mTextOrigin_x = 16.0f;
		mTextOrigin_y = 112.0f;
		mTestString = "TEST";
		mTempPosition = new Geocentric(0.0f, 1.0f, 0.0f);
		mTempScale = 2000;
		int fontSize = 12;
*/		
		mTempPosition = new Geocentric(0.0f, 1.0f, 0.0f);
		mTempScale = 100;
		mBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_4444);
		
		mCanvas = new Canvas(mBitmap);
		mBitmap.eraseColor(0);
		
		mTextPaint = new Paint();
		//mTextPaint.setTypeface(Typeface.create("Verdana", Typeface.NORMAL));
		mTextPaint.setTextSize(32);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setARGB(0xff, 0x00, 0xff, 0x00);
		//mTextPaint.setTextSize(fontSize * mRes.getDisplayMetrics().density);
		//mTextPaint.setTextSize(12);
		//float density = mRes.getDisplayMetrics().density;
	}
	
	
	
	public void drawLabelsToCanvas() {
		int u = 0;
		int v = 0;
		int currentHeight = 0;
		int ascent = (int) Math.ceil(-mTextPaint.ascent());
		int descent = (int) Math.ceil(mTextPaint.descent());
		int lineHeight = ascent + descent;
		for (Label label : LabelSet) {
			mTextPaint.setColor(label.getColor());
			int labelTextWidth = (int) Math.ceil(mTextPaint.measureText(label.getText()));
			
			int nextU;
			
			if (u + labelTextWidth > mBitmapWidth) {
				u = 0;
				nextU = labelTextWidth;
				v += lineHeight;
			} else {
				nextU = u + labelTextWidth;
			}
			
			// check to make sure texture is large enough to hold all text labels
			if (v + lineHeight > mBitmapHeight) {
				throw new IllegalArgumentException("Out of Texture Space");
			}
			
			int vMid = v + ascent;
			
			// draw the label to the canvas
			mCanvas.drawText(label.getText(), u, vMid, mTextPaint);
			// store the location of the label text in the texture
			label.setTextureData(labelTextWidth, lineHeight, u, v + lineHeight, labelTextWidth, -lineHeight, mTexelWidth, mTexelHeight);
			u = nextU;
		}
		mNumberOfLabels = LabelSet.size();
	}
	
	public void printLabelsToTexture(int textureID) {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
		mBitmap.recycle();
		mBitmap = null;
		mCanvas = null;
	}
	
	public int debugTexture() {
		// create empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
		
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0);
		
		// draw the text
		Paint textPaint = new Paint();
		textPaint.setTextSize(32);
		textPaint.setAntiAlias(true);
		textPaint.setARGB(0xff, 0x00, 0xff, 0x00);
		
		// draw the text centered
		canvas.drawText("Hello World", 16, 112, textPaint);
		
		// generate the texture pointer
		final int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);
		
		// bind texture
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
		
		// texture parameters
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		
		// specify 2D texture image
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		
		bitmap.recycle();
		
		return textureHandle[0];
	}
	
	public void initializeBuffers() {
		mLabelsPositionBuffer = ByteBuffer.allocateDirect(POSITION_LENGTH_PER_UNIT * BYTES_PER_FLOAT * mNumberOfLabels).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mLabelsColorBuffer = ByteBuffer.allocateDirect(COLOR_LENGTH_PER_UNIT * BYTES_PER_FLOAT * mNumberOfLabels).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mLabelsTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT * mNumberOfLabels).order(ByteOrder.nativeOrder()).asFloatBuffer();
		//Debug
//		mLabelsPositionBuffer = ByteBuffer.allocateDirect(POSITION_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
//		mLabelsColorBuffer = ByteBuffer.allocateDirect(COLOR_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
//		mLabelsTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();

	}
	
	public void updateDrawData() {
	    mLabelsPositionBuffer.position(0);
		mLabelsColorBuffer.position(0);
		mLabelsTextureBuffer.position(0);
		
		for (Label label : LabelSet) {
		
		    Geocentric position = label.getPosition();
		    //Debug
//		    Geocentric position = mTempPosition;
		    Geocentric u = MathUtils.normalize(MathUtils.crossProduct(position, Z_UP_VECTOR));
		    Geocentric v = MathUtils.crossProduct(u, position);
		
		    float sizer = mTempScale * mRenderer.mPointSizeFactor;
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
		    mLabelsPositionBuffer.put(topLeft);
		    mLabelsPositionBuffer.put(bottomLeft);
		    mLabelsPositionBuffer.put(topRight);
		    // Inserting second triangle (counter-clockwise)
		    mLabelsPositionBuffer.put(bottomLeft);
		    mLabelsPositionBuffer.put(bottomRight);
		    mLabelsPositionBuffer.put(topRight);
		
		    // Re-using sun color array for all the labels (white)
		    mLabelsColorBuffer.put(SUN_COLOR_DATA_ARRAY);
		
		    // Inserting texture coordinates per label
		    mLabelsTextureBuffer.put(label.getTexCoords());
		    //Debug
//		    mLabelsTextureBuffer.put(TEX_COORD_ARRAY);
		}
		mLabelsPositionBuffer.position(0);
		mLabelsColorBuffer.position(0);
		mLabelsTextureBuffer.position(0);	
	}
	
	public void drawLabels(int positionHandle, int colorHandle, int textureCoordinateHandle) {
		// pass in position information
		mLabelsPositionBuffer.position(0);
		GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, POSITION_STRIDE_BYTES, mLabelsPositionBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		// pass in the color information
		mLabelsColorBuffer.position(0);
		GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, COLOR_STRIDE_BYTES, mLabelsColorBuffer);
		GLES20.glEnableVertexAttribArray(colorHandle);
		
		// pass in the texture coordinate information
		mLabelsTextureBuffer.position(0);
		GLES20.glVertexAttribPointer(textureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, TEXTURE_COORDINATE_STRIDE_BYTES, mLabelsTextureBuffer);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
		
		// draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, INDICES_PER_UNIT * mNumberOfLabels);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
	}
	
	public void addLabel(String text, Geocentric pos, int color, int textSize, LabelTypeEnum type) {
		LabelSet.add(new Label(text, pos, color, textSize, type));
	}	
}
