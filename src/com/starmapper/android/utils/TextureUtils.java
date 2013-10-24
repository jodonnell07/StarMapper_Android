package com.starmapper.android.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public abstract class TextureUtils {
	
	// Texture loading method
	public static int loadTexture(final Context context, final int resourceID) {
		
//		Log.d("LOAD_TEXTURE", "Just entered loadTexture method");
		final int[] textureHandle = new int[1];
		Bitmap bitmap;
		
		GLES20.glGenTextures(1, textureHandle, 0);
		
		if (textureHandle[0] != 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;	// no pre-scaling
			
			// Read in the resource
			//final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceID, options);
			
			// Test read in the resource
			InputStream is = context.getResources().openRawResource(resourceID);
			try {
				bitmap = BitmapFactory.decodeStream(is);
			}
			finally  {
				// Always clear and close
				try {
					is.close();
					is = null;
				}
				catch (IOException e) {
				}
			}
			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			
			// Set filtering
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			
			// Load the bitmap into the bound texture
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			
			// Recycle the bitmap, since it is now loaded into OpenGL
			bitmap.recycle();
		}
		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture");
		}
//		Log.d("LOAD_TEXTURE", "Loaded texture OK");
		return textureHandle[0];
	}
	
	// Texture creation method
	public static int createTexture() {
		final int[] textureHandle = new int[1];
		
		GLES20.glGenTextures(1, textureHandle, 0);
		
		// Bind to the texture in OpenGL
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
		
		// Set filtering
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		
		return textureHandle[0];
	}
}
