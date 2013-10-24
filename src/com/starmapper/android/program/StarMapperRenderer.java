package com.starmapper.android.program;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.starmapper_android.R;
import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.constants.OrbitalElementsConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.utils.MathUtils;
import com.starmapper.android.utils.RawResourceUtils;
import com.starmapper.android.utils.ShaderUtils;
import com.starmapper.android.utils.TextureUtils;

public class StarMapperRenderer implements GLSurfaceView.Renderer, MathConstants, OrbitalElementsConstants {
	
	private static final String TAG = "StarMapperRenderer";

	private final Context mActivityContext;
	private final Resources mActivityResources;
	
	private SharedPreferences sharedPreferences;
	
	public int mScreenHeight;
	public int mScreenWidth;
	
	/** Perspective-related variables **/
	public boolean mUpdatePerspective;
	public float mFovYRad;
	public float mPointSizeFactor;
	public float mBGPointSizeFactor;
	public float mLineSizeFactor;
	
	/** View Matrix values **/
	public float mEyeX;
	public float mEyeY;
	public float mEyeZ;
	public float mLookX;
	public float mLookY;
	public float mLookZ;
	public float mUpX;
	public float mUpY;
	public float mUpZ;

	/** Rendering Managers **/
	private ConstellationManager mConstellationManager;
	private BGStarManager mBGStarManager;
	private GridManager mGridManager;
	private PlanetManager mPlanetManager;
	private SunManager mSunManager;
	private MoonManager mMoonManager;
	// public because other managers will be adding labels of their objects through this field
	public LabelManager mLabelManager;
	
	/*** DEBUG ***/
//	private float[][] mLookVectorTracker = new float[50][3];
	
/*
	private final int mBytesPerFloat = 4;
	private final int mPositionDataSize = 3;			//X,Y,Z per vertex
	private final int mColorDataSize = 4;				//R,G,B,alpha per vertex
	private final int mTextureCoordinateDataSize = 2;	//(s,t) coordinates
	
	private final int mPositionStrideBytes = mPositionDataSize * mBytesPerFloat;
	private final int mColorStrideBytes = mColorDataSize * mBytesPerFloat;
	private final int mTextureStrideBytes = mTextureCoordinateDataSize * mBytesPerFloat;
*/
	
	/** Projection Matrix. Projects the scene on a 2D viewport. **/
	private float[] mProjectionMatrix = new float[16];
	/** View Matrix. Represents camera position and direction **/
	private float[] mViewMatrix = new float[16];
	/** Model Matrix. Used to move models from object space (where the object is the center of the universe) to world space **/
	private float[] mModelMatrix = new float[16];
	/** Model/View/Projection Matrix. Will be passed into the shader program **/
	private float[] mMVPMatrix = new float[16];
	
	/** Store model data in a FloatBuffer **/
/*
 	private final FloatBuffer mStarPositions;
	private final FloatBuffer mStarColors;
	private final FloatBuffer mStarTextureCoordinates;
*/
	
	/** Handle passes transformation matrix to shader **/
	private int mMVPMatrixHandle;
	
	/** Handle passes texture data to shader **/
	private int mStarTextureDataHandle;
	private int mBGStarTextureDataHandle;
	private int mGridTextureDataHandle;
	private int mConstLineTextureDataHandle;
	private int[] mPlanetTextureDataHandles;
	private int mSunTextureDataHandle;
	private int mMoonTextureDataHandle;
	private int mLabelTextureDataHandle;
	
	/** Handle to the star shader program **/
	private int mProgramHandle;
	
	/** Handle to pass in a texture uniform to the shader program **/
	private int mTextureUniformHandle;
	
	/** Handle to pass in position attribute to the shader program **/
	private int mPositionHandle;
	
	/** Handle to pass in color attribute to the shader program **/
	private int mColorHandle;
	
	/** Handle to pass in texture coordinate attribute to the shader program **/
	private int mTextureCoordinateHandle;

	/** Draw switches **/
	private boolean mBGStarsIsEnabled;
	private boolean mStarsIsEnabled;
	private boolean mConstellationsIsEnabled;
	private boolean mCelestialGridIsEnabled;
	private boolean mMoonIsEnabled;
	private boolean mPlanetsIsEnabled;
	private boolean mSunIsEnabled;
	private boolean mLabelsIsEnabled;
	
	/*
	 * This constructor is only temporary to define the square the star texture will be written on
	 */
	public StarMapperRenderer(final Context activityContext) {
		
		mActivityContext = activityContext;
		mActivityResources = activityContext.getResources();
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activityContext);
		
		mBGStarsIsEnabled        = sharedPreferences.getBoolean(StarMapper.PREF_KEY_BACKGROUND_STARS, true);
		mStarsIsEnabled          = sharedPreferences.getBoolean(StarMapper.PREF_KEY_STARS, true);
		mConstellationsIsEnabled = sharedPreferences.getBoolean(StarMapper.PREF_KEY_CONSTELLATIONS, true);
		mCelestialGridIsEnabled  = sharedPreferences.getBoolean(StarMapper.PREF_KEY_CELESTIAL_GRID, true);
		mMoonIsEnabled           = sharedPreferences.getBoolean(StarMapper.PREF_KEY_MOON, true);
		mPlanetsIsEnabled        = sharedPreferences.getBoolean(StarMapper.PREF_KEY_PLANETS, true);
		mSunIsEnabled            = sharedPreferences.getBoolean(StarMapper.PREF_KEY_SUN, true);
		mLabelsIsEnabled		 = sharedPreferences.getBoolean(StarMapper.PREF_KEY_LABELS, true);
		
		mPlanetTextureDataHandles = new int[NUM_PLANETS];
		
		// Eye position (Z==1.5==behind origin)
		mEyeX = EYE_POSITION.x;
		mEyeY = EYE_POSITION.y;
		mEyeZ = EYE_POSITION.z;
		// Look direction (towards distance, into phone)
		mLookX = INIT_LOOK_VECTOR.x;
		mLookY = INIT_LOOK_VECTOR.y;
		mLookZ = INIT_LOOK_VECTOR.z;
		// UP vector
		mUpX = INIT_UP_VECTOR.x;
		mUpY = INIT_UP_VECTOR.y;
		mUpZ = INIT_UP_VECTOR.z;
	}
	
	protected String getVertexShader() {
		return RawResourceUtils.readTextFileFromRawResource(mActivityContext, R.raw.vertex_shader);
	}
	
	protected String getFragmentShader() {
		return RawResourceUtils.readTextFileFromRawResource(mActivityContext, R.raw.fragment_shader);
	}
		
	@Override
	public void onDrawFrame(GL10 glUnused) {
		
//		Log.d("onDrawFrame", "Inside onDrawFrame");
//		System.out.println(mScreenHeight);
//		System.out.println(mScreenWidth);
		
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		// Set the star drawing shader program
		GLES20.glUseProgram(mProgramHandle);
		
		// Set program handles for star drawing
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
		mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
		mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
		mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");
				
//		mStarTextureDataHandle = TextureUtils.loadTexture(mActivityContext, R.raw.stars_texture);
		
		/* Matrix Updating */
		if (mUpdatePerspective) {
		    updatePerspectiveMatrix();
			mConstellationManager.updateDrawData();
			mBGStarManager.updateDrawData();
			mGridManager.updateDrawData();
			mPlanetManager.updateDrawData();
			mSunManager.updateDrawData();
			mMoonManager.updateDrawData();
			mLabelManager.updateDrawData();
		    mUpdatePerspective = false;
		}
/*		for (int i = 49; i > 0; --i) {
			mLookVectorTracker[i+1][0] = mLookVectorTracker[i][0];
			mLookVectorTracker[i+1][1] = mLookVectorTracker[i][1];
			mLookVectorTracker[i+1][2] = mLookVectorTracker[i][2];
		}
		mLookVectorTracker[0][0] = mLookX;
		mLookVectorTracker[0][1] = mLookY;
		mLookVectorTracker[0][2] = mLookZ;
*/		
		// Set the View Matrix (camera position) (OnTouchEvent in main activity will change the look & up values)
//		Log.d("TAG", "BEFORE UP FIXING   : " + "mUpX: " + String.valueOf(mUpX) + " mLookY: " + String.valueOf(mUpY) + " mLookZ: " + String.valueOf(mUpZ));
		fixPerpendicular();
//		Log.d("StarMapperRenderer", "mLookX: " + String.valueOf(mLookX) + " mLookY: " + String.valueOf(mLookY) + " mLookZ: " + String.valueOf(mLookZ));
//		Log.d("StarMapperRenderer", "mUpX: " + String.valueOf(mUpX) + " mUpY: " + String.valueOf(mUpY) + " mUpZ: " + String.valueOf(mUpZ));
		
/*		//DEBUG
		double planet_RA = MathUtils.arctand(mLookY / mLookX) * MathUtils.convertToDegrees;
		if (mLookX > 0.0f && mLookY < 0.0f) {
			planet_RA += 360.0f;
		} else if (mLookX < 0.0f) {
			planet_RA += 180.0f;
		}
		int planet_RA_Hours = (int) (planet_RA / 15);
		int planet_RA_Minutes = (int) (((planet_RA % 15) / 15.0f) * 60);
		double planet_Dec = MathUtils.arctan2d(mLookZ, Math.sqrt(mLookX * mLookX + mLookY * mLookY)) * MathUtils.convertToDegrees;
		Log.d("StarMapperRenderer", "RaDec LookDir:  RA: " + String.valueOf(planet_RA_Hours) + "h " + String.valueOf(planet_RA_Minutes) + "'   Dec: " + String.valueOf(planet_Dec));
*/
		Matrix.setLookAtM(mViewMatrix, 0, mEyeX, mEyeY, mEyeZ, mLookX, mLookY, mLookZ, mUpX, mUpY, mUpZ);
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 0.0f);
		// multiply view & model matrix, creating modelview matrix (temp stored in mMVPMatrix)
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
		// multiply modelview & projection matrix, creating final MVP matrix
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		// Pass in the MVP matrix
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		
		// Set active texture unit (both grid and star textures available in GL_TEXTURE0)
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mStarTextureDataHandle);				
		// Tell the texture uniform sampler to use this texture in the shader program by binding to texture unit 0
		GLES20.glUniform1f(mTextureUniformHandle, GLES20.GL_TEXTURE0);
		
		if (mCelestialGridIsEnabled) {
		    // Bind grid texture to the texture target
		    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGridTextureDataHandle);
		    mGridManager.drawGrid(mPositionHandle, mColorHandle, mTextureCoordinateHandle);
		}
		if (mConstellationsIsEnabled) {
		    // Bind constLine texture to the texture target
		    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mConstLineTextureDataHandle);
		    mConstellationManager.drawConstLines(mPositionHandle, mColorHandle, mTextureCoordinateHandle);
		}
		if (mStarsIsEnabled) {
		    // Bind star texture to the texture target
		    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mStarTextureDataHandle);
		    mConstellationManager.drawConstellations(mPositionHandle, mColorHandle, mTextureCoordinateHandle);
		}
		if (mBGStarsIsEnabled) {
		    // Bind background star texture to the texture target
		    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBGStarTextureDataHandle);
		    mBGStarManager.drawBGStars(mPositionHandle, mColorHandle, mTextureCoordinateHandle);
		}
		if (mPlanetsIsEnabled) {
		    // Bind planet texture to the texture target
		    for (PlanetEnum planetEnum : PlanetEnum.values()) {
			    if (planetEnum == PlanetEnum.EARTH) { continue; }
			    int planetIdx = planetEnum.ordinal();
			    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPlanetTextureDataHandles[planetIdx]);
			    mPlanetManager.drawPlanet(mPositionHandle, mColorHandle, mTextureCoordinateHandle, planetIdx);
		    }
		}
		if (mSunIsEnabled) {
		    // Bind sun texture to the texture target
		    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSunTextureDataHandle);
		    mSunManager.drawSun(mPositionHandle, mColorHandle, mTextureCoordinateHandle);
		}
		if (mMoonIsEnabled) {
		    // Bind moon texture to the texture target
		    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mMoonTextureDataHandle);
		    mMoonManager.drawMoon(mPositionHandle, mColorHandle, mTextureCoordinateHandle);
		}
		if (mLabelsIsEnabled) {
			// Bind labels texture to the texture target
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLabelTextureDataHandle);
			mLabelManager.drawLabels(mPositionHandle, mColorHandle, mTextureCoordinateHandle);
		}
		//draw();		
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		
		// Set the OpenGL viewport to the same size as the surface
		GLES20.glViewport(0, 0, width, height);
		mScreenHeight = height;
		mScreenWidth = width;
/*
		// Create a new perspective projection matrix. Height will stay the same while width will vary as per aspect ratio
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 0.75f;
		final float far = 5.0f;
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
*/
		if (mUpdatePerspective) {
		    updatePerspectiveMatrix();
			mConstellationManager.updateDrawData();
			mBGStarManager.updateDrawData();
			mGridManager.updateDrawData();
			mPlanetManager.updateDrawData();
			mSunManager.updateDrawData();
			mMoonManager.updateDrawData();
			mLabelManager.updateDrawData();
		    mUpdatePerspective = false;
		}
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		
		Log.d("onSurfaceCreated", "Inside onSurfaceCreated");
		
		// Set the background to black
		GLES20.glClearColor(0.1f, 0.0f, 0.15f, 1.0f);
//		GLES20.glClearColor(0.4f, 0.0f, 0.6f, 1.0f);
		
		// Alpha blending
//		GLES20.glEnable(GLES20.GL_TEXTURE_2D);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		// Set the View Matrix (camera position)
		Matrix.setLookAtM(mViewMatrix, 0, mEyeX, mEyeY, mEyeZ, mLookX, mLookY, mLookZ, mUpX, mUpY, mUpZ);
		
		mUpdatePerspective = true;
		mFovYRad = INIT_FOV_Y_RAD;
		mPointSizeFactor = MathUtils.tan(INIT_FOV_Y_RAD / 2.0f) / SCREEN_HEIGHT;
		mBGPointSizeFactor = mPointSizeFactor / 3;
		mLineSizeFactor  = mPointSizeFactor * 2;
		
		// Initialize all the managers
		mLabelManager = new LabelManager(this, mActivityResources);
		mConstellationManager = new ConstellationManager(this);
		mConstellationManager.BuildConstellationsFromRawResource(mActivityContext, R.raw.constellation_bayer_list);
		mBGStarManager = new BGStarManager(this);
		mBGStarManager.BuildBGStars();
		mGridManager = new GridManager(this);
		mGridManager.BuildGrid();
		mPlanetManager = new PlanetManager(this);
		mPlanetManager.BuildPlanetData();
		mSunManager = new SunManager(this);
		mSunManager.BuildSunData();
		mMoonManager = new MoonManager(this);
		mMoonManager.BuildMoonData();
		
		// Create all the labels and add them into the label manager
		mConstellationManager.createLabels();
		mGridManager.createLabels();
		mPlanetManager.createLabels();
		mSunManager.createLabels();
		mMoonManager.createLabels();
		
		// Initialize all the rendering buffers
		mConstellationManager.initializeBuffers();
		mBGStarManager.initializeBuffers();
		mGridManager.initializeBuffers();
		mPlanetManager.initializeBuffers();
		mSunManager.initializeBuffers();
		mMoonManager.initializeBuffers();
		
		// updateDrawData will probably move into onDrawFrame, putting it here in onSurfaceCreated for now since it isn't changing
		mConstellationManager.updateDrawData();
		mBGStarManager.updateDrawData();
		mGridManager.updateDrawData();
		mPlanetManager.updateDrawData();
		mSunManager.updateDrawData();
		mMoonManager.updateDrawData();
		
		// Load the textures data
		mStarTextureDataHandle = TextureUtils.loadTexture(mActivityContext, R.drawable.brightstar_new4);
		mBGStarTextureDataHandle = TextureUtils.loadTexture(mActivityContext, R.drawable.brightstar_new4);
		mGridTextureDataHandle = TextureUtils.loadTexture(mActivityContext, R.drawable.gridline_texture);
		mConstLineTextureDataHandle = TextureUtils.loadTexture(mActivityContext, R.drawable.constline_texture);
		mPlanetTextureDataHandles[PlanetEnum.MERCURY.ordinal()] = TextureUtils.loadTexture(mActivityContext, R.drawable.mercury);
		mPlanetTextureDataHandles[PlanetEnum.VENUS.ordinal()] = TextureUtils.loadTexture(mActivityContext, R.drawable.venus);
		mPlanetTextureDataHandles[PlanetEnum.MARS.ordinal()] = TextureUtils.loadTexture(mActivityContext, R.drawable.mars);
		mPlanetTextureDataHandles[PlanetEnum.JUPITER.ordinal()] = TextureUtils.loadTexture(mActivityContext, R.drawable.jupiter);
		mPlanetTextureDataHandles[PlanetEnum.SATURN.ordinal()] = TextureUtils.loadTexture(mActivityContext, R.drawable.saturn);
		mPlanetTextureDataHandles[PlanetEnum.URANUS.ordinal()] = TextureUtils.loadTexture(mActivityContext, R.drawable.uranus);
		mPlanetTextureDataHandles[PlanetEnum.NEPTUNE.ordinal()] = TextureUtils.loadTexture(mActivityContext, R.drawable.neptune);
		mSunTextureDataHandle = TextureUtils.loadTexture(mActivityContext, R.drawable.sun);
		mMoonTextureDataHandle = TextureUtils.loadTexture(mActivityContext, R.drawable.moon);		
		mLabelTextureDataHandle = TextureUtils.createTexture();
		
		// Apply labels to texture
		mLabelManager.drawLabelsToCanvas();
		mLabelManager.printLabelsToTexture(mLabelTextureDataHandle);
		// initialize buffers after drawing to canvas because we need the total number of labels
		mLabelManager.initializeBuffers();
		mLabelManager.updateDrawData();
		
		// debugging text-based textures
//		mLabelTextureDataHandle = mLabelManager.debugTexture();

		//set all the OpenGL stuff
		final String vertexShader = getVertexShader();
		Log.i(TAG, "Vertex Shader:" + vertexShader);
		final String fragmentShader = getFragmentShader();
		Log.i(TAG, "Fragment Shader:" + fragmentShader);
		
		final int vertexShaderHandle = ShaderUtils.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
		final int fragmentShaderHandle = ShaderUtils.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
		
		mProgramHandle = ShaderUtils.createAndLinkShaderProgram(vertexShaderHandle, fragmentShaderHandle,
				         new String[] {"a_Position", "a_Color", "a_TexCoordinate"});
		}

	public void updatePerspectiveMatrix() {
		// Perspective Projection matrix based on Stardroid
		final float near = 0.1f;
		final float far = 10000.0f;
		final float inverseAspectRatio = mScreenHeight / mScreenWidth;
		final float halfFOVInRadians = mFovYRad / 2.0f;
		final float OneOverTanHalfFOVYRad = 1.0f / MathUtils.tan(halfFOVInRadians);
		
		// Set the Projection Matrix
		mProjectionMatrix = new float[] {
				inverseAspectRatio * OneOverTanHalfFOVYRad,
				0,
				0,
				0,
				
				0,
				OneOverTanHalfFOVYRad,
				0,
				0,
				
				0,
				0,
				-(far + near) / (far - near),
				-1,
				
				0,
				0,
				-2 * far * near / (far - near),
				0
		};
		float viewSizeFactor = (120.0f - mFovYRad * MathUtils.convertToDegrees) * MathUtils.convertToRadians;
		if (viewSizeFactor < 45.0f) {
			viewSizeFactor = 45.0f;
		} else if (viewSizeFactor > 90.0f) {
			viewSizeFactor = 90.0f;
		}
		mPointSizeFactor = MathUtils.tan(viewSizeFactor / 2.0f) / mScreenHeight;
		mLineSizeFactor  = mPointSizeFactor * 2;
		Log.d("POINT_SIZE_FACTOR", String.valueOf(mPointSizeFactor));
	}
	
	public void fixPerpendicular() {		
		Geocentric mLookDir = new Geocentric(mLookX, mLookY, mLookZ);
		Geocentric mUpDir = new Geocentric(mUpX, mUpY, mUpZ);
		
		// dot product of look & up, make up perpendicular to look
		float dotLookUp = MathUtils.dotProduct(mLookDir, mUpDir);
//		Log.d("TAG", "dotLookUp: " + String.valueOf(dotLookUp));
		this.mUpX -= dotLookUp * mLookX;
		this.mUpY -= dotLookUp * mLookY;
		this.mUpZ -= dotLookUp * mLookZ;
//		Log.d("TAG", "AFTER SUBTRACTING DOTLOOKUP: " + "mUpX: " + String.valueOf(mUpX) + " mUpY: " + String.valueOf(mUpY) + " mUpZ: " + String.valueOf(mUpZ));
		
		// normalize up
		float upLength = (float) Math.sqrt(mUpX * mUpX + mUpY * mUpY + mUpZ * mUpZ);
		this.mUpX /= upLength;
		this.mUpY /= upLength;
		this.mUpZ /= upLength;
//		Log.d("TAG", "AFTER NORMALIZING UP: " + "mUpX: " + String.valueOf(mUpX) + " mUpY: " + String.valueOf(mUpY) + " mUpZ: " + String.valueOf(mUpZ));
		
	}
	
	public void resetPerspective() {
		mFovYRad = INIT_FOV_Y_RAD;
		mPointSizeFactor = MathUtils.tan(INIT_FOV_Y_RAD / 2.0f) / SCREEN_HEIGHT;
		mBGPointSizeFactor = mPointSizeFactor / 3;
		mLineSizeFactor  = mPointSizeFactor * 2;
		mUpdatePerspective = true;
	}
	
	// Preference methods to set/disable drawing of different objects
	public void setStarsIsEnabled(boolean isEnabled) {
		mStarsIsEnabled = isEnabled;
	}
	public void setBGStarsIsEnabled(boolean isEnabled) {
		mBGStarsIsEnabled = isEnabled;
	}
	public void setConstellationsIsEnabled(boolean isEnabled) {
		mConstellationsIsEnabled = isEnabled;
	}
	public void setCelestialGridIsEnabled(boolean isEnabled) {
		mCelestialGridIsEnabled = isEnabled;
	}
	public void setSunIsEnabled(boolean isEnabled) {
		mSunIsEnabled = isEnabled;
	}
	public void setMoonIsEnabled(boolean isEnabled) {
		mMoonIsEnabled = isEnabled;
	}
	public void setPlanetsIsEnabled(boolean isEnabled) {
		mPlanetsIsEnabled = isEnabled;
	}
	public void setLabelsIsEnabled(boolean isEnabled) {
		mLabelsIsEnabled = isEnabled;
	}
	
	/*
	 * NOT USED, KEPT FOR REFERENCE
	 */
	/* ENABLED FOR DEBUGGING */
	//private void draw() {
		/*
		// Pass in position information
		mStarPositions.position(0);
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, mPositionStrideBytes, mStarPositions);		
		
		// Pass in the color information
		mStarColors.position(0);
		GLES20.glEnableVertexAttribArray(mColorHandle);
		GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, mColorStrideBytes, mStarColors);
		
		// Pass in the texture coordinate information
		mStarTextureCoordinates.position(0);
		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, mTextureStrideBytes, mStarTextureCoordinates);
				
		// Draw the star
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mColorHandle);
		GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
*/
		
	//}
    /* END OF DEBUGGING */
}
