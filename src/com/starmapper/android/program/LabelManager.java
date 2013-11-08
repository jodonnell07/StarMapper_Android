package com.starmapper.android.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.starmapper.android.constants.ArrayConstants;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.Matrix3x3;
import com.starmapper.android.math.Matrix4x4;
import com.starmapper.android.user.User;
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
	
	private Bitmap mBitmap;
	private Canvas mCanvas;
	
	private Paint mTextPaint;
	
	private String mTestString;
	
	private User mUser;
	
	private Matrix4x4 mProjectionMatrix4x4;
	private Matrix4x4 mViewMatrix4x4;
	private final Matrix4x4 mTranslatorMatrix4x4;
	private final Matrix4x4 mScalingMatrix4x4;
	
	// buffers used for OpenGL drawing
	private FloatBuffer mLabelsPositionBuffer;
	private FloatBuffer mLabelsColorBuffer;
	private FloatBuffer mLabelsTextureBuffer;
	
	private FloatBuffer mLabelsPositionBufferTest;
	private FloatBuffer mLabelsColorBufferTest;
	private FloatBuffer mLabelsTextureBufferTest;

	// All labels to be displayed will go here
	public Set<Label> LabelSet;
	Label testLabel;
	
	// rectangle is 0.6 x 0.4
	float[] bottomLeftTest  = { -0.3f, -0.2f, 0.0f };
	float[] topLeftTest     = { -0.3f,  0.2f, 0.0f };
	float[] bottomRightTest = {  0.3f, -0.2f, 0.0f };
	float[] topRightTest    = {  0.3f,  0.2f, 0.0f };
    float[] mBottomLeftRect  = { -0.5f, -0.5f, -1.0f, 1.0f };
    float[] mTopLeftRect     = { -0.5f,  0.5f, -1.0f, 0.0f };
    float[] mBottomRightRect = {  0.5f, -0.5f, -1.0f, 0.0f };
    float[] mTopRightRect    = {  0.5f,  0.5f, -1.0f, 0.0f };
    float[] mBottomLeftTranslated =  new float[4];
    float[] mTopLeftTranslated    =  new float[4];
    float[] mBottomRightTranslated = new float[4];
    float[] mTopRightTranslated    = new float[4];
    float[] mFinalBottomLeft  = new float[3];
    float[] mFinalTopLeft     = new float[3];
    float[] mFinalBottomRight = new float[3];
    float[] mFinalTopRight    = new float[3];

	private int mNumberOfLabels;
	
	// offset so labels will be underneath celestial object
	private Geocentric mLabelOffset;
	// dot product used to determine if label is on-screen
	private float mDotProductThreshold;
	
	// Constructors
	public LabelManager(StarMapperRenderer renderer, Resources res, User user) {
		mRenderer = renderer;
		mRes = res;
		mUser = user;
		
		LabelSet = new LinkedHashSet<Label>();
		
		mProjectionMatrix4x4 = new Matrix4x4();
		mViewMatrix4x4 = new Matrix4x4();

		mTranslatorMatrix4x4 = new Matrix4x4(new float[] {
									1, 0, 0, 0,
									0, 1, 0, 0,
									0, 0, 1, 0,
									1, 1, 0, 1});
		mScalingMatrix4x4 = new Matrix4x4(new float[] {
									mRenderer.mScreenWidth * 0.5f, 0, 0, 0,
									0, mRenderer.mScreenHeight * 0.5f, 0, 0,
									0, 0, 1, 0,
									0, 0, 0, 1});

		// Real settings here
		mBitmapWidth = 1024;
		mBitmapHeight = 1024;
		mTexelWidth = (float) (1.0f / mBitmapWidth);
		mTexelHeight = (float) (1.0f / mBitmapHeight);
		
		mBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_4444);
		
		mCanvas = new Canvas(mBitmap);
		mBitmap.eraseColor(0);
		
		mTextPaint = new Paint();
		//mTextPaint.setTypeface(Typeface.create("Verdana", Typeface.NORMAL));
		mTextPaint.setTextSize(24);
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
		
		mLabelsPositionBufferTest = ByteBuffer.allocateDirect(POSITION_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mLabelsColorBufferTest = ByteBuffer.allocateDirect(COLOR_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mLabelsTextureBufferTest = ByteBuffer.allocateDirect(TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();

		//Debug
//		mLabelsPositionBuffer = ByteBuffer.allocateDirect(POSITION_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
//		mLabelsColorBuffer = ByteBuffer.allocateDirect(COLOR_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
//		mLabelsTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();

	}
	
	public void updateDrawData() {
	
		// hack so labels always appear underneath celestial object
		updateLabelOffset();
		
		mProjectionMatrix4x4.setMatrix4x4(mRenderer.getProjectionMatrix());
		Geocentric lookDir = mUser.getLookDir();
		Geocentric lookNormal = mUser.getLookNormal();
		Geocentric crossDirNormal = MathUtils.crossProduct(lookDir, lookNormal);
		mViewMatrix4x4.setMatrix4x4(lookDir, lookNormal, crossDirNormal);
		Matrix4x4 PhoneTransform = MathUtils.multiplyMatrices(mProjectionMatrix4x4, mViewMatrix4x4);
		Matrix4x4 ScalingXTranslatorMatrix4x4 = MathUtils.multiplyMatrices(mScalingMatrix4x4, mTranslatorMatrix4x4);
		Matrix4x4 ScreenTransform = MathUtils.multiplyMatrices(ScalingXTranslatorMatrix4x4, PhoneTransform);
		
		// An inefficient way to reset the buffers, but clear() did not clear the content of the buffers
		mLabelsPositionBuffer = ByteBuffer.allocateDirect(POSITION_LENGTH_PER_UNIT * BYTES_PER_FLOAT * mNumberOfLabels).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mLabelsColorBuffer = ByteBuffer.allocateDirect(COLOR_LENGTH_PER_UNIT * BYTES_PER_FLOAT * mNumberOfLabels).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mLabelsTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDINATE_LENGTH_PER_UNIT * BYTES_PER_FLOAT * mNumberOfLabels).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
	    mLabelsPositionBuffer.position(0);
		mLabelsColorBuffer.position(0);
		mLabelsTextureBuffer.position(0);
		
		for (Label label : LabelSet) {
		    Geocentric position = label.getPosition();
		    Geocentric offsetPosition = new Geocentric();
		    float offsetFactor = label.getOffset();
		    offsetPosition.x = position.x + mLabelOffset.x * offsetFactor;
		    offsetPosition.y = position.y + mLabelOffset.y * offsetFactor;
		    offsetPosition.z = position.z + mLabelOffset.z * offsetFactor;
		    
		    Geocentric screenPosition = MathUtils.transformLabelToScreenPosition(ScreenTransform, offsetPosition);
		    int widthInPixels = label.getPixelWidth();
		    int heightInPixels = label.getPixelHeight();
		    int sizedWidthInPixels = (int) (widthInPixels * mRenderer.mLabelSizeFactor);
		    int sizedHeightInPixels = (int) (heightInPixels * mRenderer.mLabelSizeFactor);

		    int onScreenPos_x = (int) screenPosition.x;
		    int onScreenPos_y = (int) (SCREEN_HEIGHT - screenPosition.y);
		    
		    // update onScreen coordinates for touch
		    label.screenPos_xll = onScreenPos_x - sizedWidthInPixels / 2;
		    label.screenPos_xur = onScreenPos_x + sizedWidthInPixels / 2;
		    label.screenPos_yll = onScreenPos_y - sizedHeightInPixels / 2;
		    label.screenPos_yur = onScreenPos_y + sizedHeightInPixels / 2;
		    
		    boolean labelOnScreen;
		    if (lookDir.x * position.x + lookDir.y * position.y + lookDir.z * position.z < mDotProductThreshold) {
		    	labelOnScreen = false;
		    } else {
		    	labelOnScreen = true;
		    }
		    label.setOnScreen(labelOnScreen);
  
		    float[] mat = new float[16];
		    Matrix.setIdentityM(mat, 0);
		    // rotate matrix to orient labels
		    float rotAngle = mUser.mTextUpAngle + 2.0f * MathUtils.PI_OVER_TWO;
		    Matrix.rotateM(mat, 0, rotAngle * MathUtils.convertToDegrees, 0, 0, -1);
		    // scale the labels by their pixel sizes
		    float screenWidth = (float) mRenderer.mScreenWidth;
		    float screenHeight = (float) mRenderer.mScreenHeight;
		    float widthRatio = (widthInPixels / screenWidth) * mRenderer.mLabelSizeFactor;
		    float heightRatio = (heightInPixels / screenHeight) * mRenderer.mLabelSizeFactor;
		    
		    Matrix.scaleM(mat, 0, widthRatio, heightRatio, 1);
		    
		    Matrix.multiplyMV(mBottomLeftTranslated, 0, mat, 0, mBottomLeftRect, 0);
		    Matrix.multiplyMV(mTopLeftTranslated, 0, mat, 0, mTopLeftRect, 0);
		    Matrix.multiplyMV(mBottomRightTranslated, 0, mat, 0, mBottomRightRect, 0);
		    Matrix.multiplyMV(mTopRightTranslated, 0, mat, 0, mTopRightRect, 0);
		    // translate
		    int x = 0; int y = 1;
		    float x_translation = (screenPosition.x / screenWidth) * 2.0f - 1.0f;
		    float y_translation = (screenPosition.y / screenHeight) * 2.0f - 1.0f;
		    mBottomLeftTranslated[x]  += x_translation; mBottomLeftTranslated[y]  += y_translation;
		    mTopLeftTranslated[x]     += x_translation; mTopLeftTranslated[y]     += y_translation;
		    mBottomRightTranslated[x] += x_translation; mBottomRightTranslated[y] += y_translation;
		    mTopRightTranslated[x]    += x_translation; mTopRightTranslated[y]    += y_translation;
		    
		    for (int i = 0; i < 3; i++) {
		    	mFinalBottomLeft[i] =  mBottomLeftTranslated[i];
		    	mFinalTopLeft[i]    =  mTopLeftTranslated[i];
		    	mFinalBottomRight[i] = mBottomRightTranslated[i];
		    	mFinalTopRight[i]    = mTopRightTranslated[i];
		    }
		if (labelOnScreen) {
		    // Inserting first triangle
		    mLabelsPositionBuffer.put(mFinalTopLeft);
		    mLabelsPositionBuffer.put(mFinalBottomLeft);
		    mLabelsPositionBuffer.put(mFinalTopRight);
		    // Inserting second triangle
		    mLabelsPositionBuffer.put(mFinalBottomLeft);
		    mLabelsPositionBuffer.put(mFinalBottomRight);
		    mLabelsPositionBuffer.put(mFinalTopRight);
		    // Color Array
		    mLabelsColorBuffer.put(SUN_COLOR_DATA_ARRAY);
		    // Texture Array
		    mLabelsTextureBuffer.put(label.getTexCoords());
		}
/***** TESTING STUFF
			Label label = testLabel;
			mLabelsPositionBufferTest.position(0);
			mLabelsColorBufferTest.position(0);
			mLabelsTextureBufferTest.position(0);
						
		    // Inserting first triangle (counter-clockwise)
		    mLabelsPositionBufferTest.put(topLeftTest);
		    mLabelsPositionBufferTest.put(bottomLeftTest);
		    mLabelsPositionBufferTest.put(topRightTest);
		    // Inserting second triangle (counter-clockwise)
		    mLabelsPositionBufferTest.put(bottomLeftTest);
		    mLabelsPositionBufferTest.put(bottomRightTest);
		    mLabelsPositionBufferTest.put(topRightTest);
		
		    // Re-using sun color array for all the labels (white)
		    mLabelsColorBufferTest.put(SUN_COLOR_DATA_ARRAY);
		
		    // Inserting texture coordinates per label
		    mLabelsTextureBufferTest.put(label.getTexCoords());
*** END OF TESTING STUFF */
		    
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
	
	public void drawLabelsTest(int positionHandle, int colorHandle, int textureCoordinateHandle) {
		// pass in position information
		mLabelsPositionBufferTest.position(0);
		GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, POSITION_STRIDE_BYTES, mLabelsPositionBufferTest);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		// pass in the color information
		mLabelsColorBufferTest.position(0);
		GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, COLOR_STRIDE_BYTES, mLabelsColorBufferTest);
		GLES20.glEnableVertexAttribArray(colorHandle);
		
		// pass in the texture coordinate information
		mLabelsTextureBufferTest.position(0);
		GLES20.glVertexAttribPointer(textureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, TEXTURE_COORDINATE_STRIDE_BYTES, mLabelsTextureBufferTest);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
		
		// draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, INDICES_PER_UNIT);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
	}
	
	
	public void addLabel(String text, Geocentric pos, int color, int textSize, LabelTypeEnum type) {
	    float offset = 0.0f;
		if (type == LabelTypeEnum.CONSTELLATION) { offset = CONSTELLATION_OFFSET; }
		else if (type == LabelTypeEnum.STAR)     { offset = STAR_OFFSET; }
		else if (type == LabelTypeEnum.SUN)      { offset = SUN_OFFSET; }
		else if (type == LabelTypeEnum.MOON)     { offset = MOON_OFFSET; }
		else if (type == LabelTypeEnum.PLANET)   { offset = PLANET_OFFSET; }
		else if (type == LabelTypeEnum.GRID)     { offset = GRID_OFFSET; }
		Label newLabel = new Label(text, pos, color, textSize, type, offset);
		LabelSet.add(newLabel);
		// Debug
		String testString = "Orion";
		if (testString.equalsIgnoreCase(text)) {
			testLabel = newLabel;
		}
	}
	
	public void updateLabelOffset() {
		Geocentric accel = mUser.getAcceleration();

		float textUpAngle = (float) MathUtils.arctan2(accel.x, accel.y);
		// round angle to nearest nearest 90 degrees make it more readable
		textUpAngle = Math.round(textUpAngle * MathUtils.TWO_OVER_PI) * MathUtils.PI_OVER_TWO;
		mUser.mTextUpAngle = textUpAngle;
		
		Geocentric lookDir = mUser.getLookDir();
		Geocentric lookNormal = mUser.getLookNormal();
		
		Matrix3x3 rotationMatrix = MathUtils.createRotationMatrix(Math.abs(textUpAngle), lookDir);
		mLabelOffset = MathUtils.multiplyGeocentricAndMatrix3x3(rotationMatrix, lookNormal);
		
		// dot product used to determine if label is on-screen
		mDotProductThreshold = MathUtils.cos(120 * MathUtils.convertToRadians * (1 + mRenderer.mScreenWidth / mRenderer.mScreenHeight) * 0.5f);
	}
}
