package com.starmapper.android.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.starmapper_android.R;
import com.starmapper.android.utils.RawResourceUtils;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

public class StarMapperRenderer_test implements GLSurfaceView.Renderer {

	/** Store the Context of StarMapper activity **/
	private final Context mActivityContext;
	
	/** Store our model data in a float buffer **/
	private final FloatBuffer mTriangle1Vertices;
	
	/** How many bytes per float **/
	private final int mBytesPerFloat = 4;
	
	/** Store the model matrix. This matrix is used to move models from object space (where each model is located at the center of the universe) to world space. **/
	private float[] mModelMatrix = new float[16];

	/** Store the view matrix. Can be thought of as the camera. Transforms world space to eye space; positions things relative to our eye. **/
	private float[] mViewMatrix = new float[16];
	
	/** Store the projection matrix. Projects the scene onto a 2D viewport. **/
	private float[] mProjectionMatrix = new float[16];
	
	/** Store the final combined (MVP) matrix. This is passed into the shader program **/
	private float[] mMVPMatrix = new float [16];
	
	/** Elements per vertex **/
	private final int mStrideBytes = 7 * mBytesPerFloat;
	
	/** Offset of position data **/
	private final int mPositionOffset = 0;
	
	/** Offset of color data **/
	private final int mColorOffset = 3;
	
	/** Size of position data in elements **/
	private final int mPositionDataSize = 3;
	
	/** Size of color data in elements **/
	private final int mColorDataSize = 4;
	
	/** Handle to pass in the transformation matrix **/
	private int mMVPMatrixHandle;
	
	/** Handle to pass in model position information **/
	private int mPositionHandle;
	
	/** Handle to pass in model color information **/
	private int mColorHandle;
	
	/** Initialize the model data **/
	public StarMapperRenderer_test(Context context) {
		// This triangle is red, green, and blue
		final float[] triangle1VerticesData = {
				// X, Y, Z
				// R, G, B, A
				-0.5f, -0.25f, 0.0f,
				1.0f, 0.0f, 0.0f, 1.0f,
				
				0.5f, -0.25f, 0.0f,
				0.0f, 0.0f, 1.0f, 1.0f,
				
				0.0f, 0.559016994f, 0.0f,
				0.0f, 1.0f, 0.0f, 1.0f};
		// Initialize the buffer
		mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangle1Vertices.put(triangle1VerticesData).position(0);
		mActivityContext = context;
	}

	protected String getVertexShader() {
		return RawResourceUtils.readTextFileFromRawResource(mActivityContext, R.raw.test_vertex_shader);
	}
	
	protected String getFragmentShader() {
		return RawResourceUtils.readTextFileFromRawResource(mActivityContext, R.raw.test_fragment_shader);
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
		// Do a complete rotation every 10 seconds
		long time = SystemClock.uptimeMillis() % 10000L;
		float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
		
		// Draw the triangle facing straight on
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
		drawTriangle(mTriangle1Vertices);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface
		GLES20.glViewport(0, 0, width, height);
		
		// Create a new perspective projection matrix. The height will stay the same while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;
		
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		// Set the background color to grey
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
		
		// Position the eye behind the origin
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 1.5f;
		
		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;
		
		// Set the UP vector. This is where our head would be pointing if we were holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;
		
		// Set the view matrix. This matrix represents camera position. 
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
		
		/** Vertex Shader **/
		final String vertexShader = getVertexShader();
/*		final String vertexShader = 
				  "uniform mat4 u_MVPMatrix;			\n"			// A constant representing the combined model/view/projection matrix.
				
				+ "attribute vec4 a_Position;			\n"			// Per-vertex position information we will pass in.
				+ "attribute vec4 a_Color;				\n"			// Per-vertex color information we will pass in.
				
				+ "varying vec4 v_Color;				\n"			// This will be passed into the fragment shader.
				
				+ "void main()							\n"			// Entry point for the vertex shader.
				+ "{									\n"
				+ "   v_Color = a_Color;				\n"			// Pass the color through to the fragment shader.
																	// It will be interpolated across the triangle.
				+ "   gl_Position = u_MVPMatrix			\n"			// gl_Position is a special variable used to store the final position.
				+ "               * a_Position;			\n"			// Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
				+ "}									\n";
*/		
		/** Fragment Shader **/
		final String fragmentShader = getFragmentShader();
/*		final String fragmentShader =
				  "precision mediump float;				\n"			// Set the default precision to medium. We don't need high precision in the fragment shader
				
				+ "varying vec4 v_Color;				\n"			// This is the color from the vertex shader interpolated across the triangle per fragment.
				
				+ "void main()							\n"			// Entry point of the fragment shader.
				+ "{									\n"
				+ "   gl_FragColor = v_Color;			\n"			// Pass the color directly through the pipeline.
				+ "}									\n";
*/		
		// Load the vertex shader
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		if (vertexShaderHandle != 0) {
			// Pass in the shader source
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);
			// Compile the shader
			GLES20.glCompileShader(vertexShaderHandle);
			// Get compilation status
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			// If compilation failed, delete shader
			if (compileStatus[0] == 0) {
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}
		if (vertexShaderHandle == 0) {
			throw new RuntimeException("Error creating vertex shader");
		}

		// Load the fragment shader
		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		if (fragmentShaderHandle != 0) {
			// Pass in shader source
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
			// Compile shader
			GLES20.glCompileShader(fragmentShaderHandle);
			// Compilation status
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			// delete shader if failed
			if (compileStatus[0] == 0) {
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}
		if (fragmentShaderHandle == 0) {
			throw new RuntimeException("Error creating fragment shader");
		}
		
		// Create a shader program, bind vertex & fragment shaders to it
		int programHandle = GLES20.glCreateProgram();
		if (programHandle != 0) {
			// Bind the vertex shader to the program
			GLES20.glAttachShader(programHandle, vertexShaderHandle);
			// Bind the fragment shader to the program
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			// Bind attributes
			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
			GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
			// Link the two shaders together into a program
			GLES20.glLinkProgram(programHandle);
			// Get link status
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
			// If link failed, delete program
			if (linkStatus[0] == 0) {
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		if (programHandle == 0) {
			throw new RuntimeException("Error creating shader program");
		}
		
		// Set shader program handles. These will later be used to pass in values to the program.
		mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
		mPositionHandle  = GLES20.glGetAttribLocation(programHandle, "a_Position");
		mColorHandle     = GLES20.glGetAttribLocation(programHandle, "a_Color");
		// Tell OpenGL to use this program when rendering
		GLES20.glUseProgram(programHandle);
	}

	/**
	 * Draws a triangle from the given vertex data
	 * @param aTriangleBuffer The buffer containing the vertex data
	 */
	private void drawTriangle(final FloatBuffer aTriangleBuffer) {
		// Pass in position information
		aTriangleBuffer.position(mPositionOffset);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, mStrideBytes, aTriangleBuffer);
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		// Pass in color information
		aTriangleBuffer.position(mColorOffset);
		GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, mStrideBytes, aTriangleBuffer);
		GLES20.glEnableVertexAttribArray(mColorHandle);
		
		// Multiply view matrix by model matrix, store result in MVP matrix (which currently holds model*view)
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
		// Multiply modelview matrix by projection matrix, store result in MVP matrix (which now contains model*view*projection)
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
	}
}
