package com.starmapper.android.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.starmapper.android.constants.MathConstants;
import com.starmapper.android.math.Geocentric;

public class Label implements MathConstants {

	private String text;
	private Geocentric position;
	
	private int color;
	private int   size;
	
	private LabelTypeEnum type;
	
	private float offset;
	
	// texture-related data
	private int mWidthPxls;
	private int mHeightPxls;
	private float[] mTexCoords;
	
	// screen position in Y-direction is (1920 - Y) since top->bottom = 0->1
	private boolean onScreen;
	public int screenPos_xll;
	public int screenPos_yll;
	public int screenPos_xur;
	public int screenPos_yur;
	
	
	// Constructors
	public Label(String text, Geocentric pos, int color, int size, LabelTypeEnum type, float offset) {
		this.text = text;
		this.color = color;
		this.size = size;
		this.type = type;
		this.offset = offset;
		position = new Geocentric(pos.x, pos.y, pos.z);
		screenPos_xll = 0; screenPos_yll = 0;
		screenPos_xur = 0; screenPos_yur = 0;
		mTexCoords = new float[12];
	}
		
	// Get methods
	public String getText() {
		return text;
	}
	public Geocentric getPosition() {
		return position;
	}
	public int getColor() {
		return color;
	}
	public int getTextSize() {
		return size;
	}
	public LabelTypeEnum getType() {
		return type;
	}
	public int getPixelWidth() {
		return mWidthPxls;
	}
	public int getPixelHeight() {
		return mHeightPxls;
	}
	public float[] getTexCoords() {
		return mTexCoords;
	}
	public float getOffset() {
		return offset;
	}
	public boolean isOnScreen() {
		return onScreen;
	}
	
	// Set methods
	public void setText(String text) {
		this.text = text;
	}
	public void setPosition(Geocentric pos) {
		position = new Geocentric(pos.x, pos.y, pos.z);
	}
	public void setColor(int color) {
		this.color = color;
	}
	public void setTextSize(int size) {
		this.size = size;
	}
	public void setType(LabelTypeEnum type) {
		this.type = type;
	}
	public void setOffset(float offset) {
		this.offset = offset;
	}
	public void setOnScreen(boolean onScreen) {
		this.onScreen = onScreen;
	}
	
	// setting the texture data
	public void setTextureData(int widthPxls, int heightPxls, int cropU, int cropV, int cropW, int cropH, float texelWidth, float texelHeight) {
		mWidthPxls = widthPxls;
		mHeightPxls = heightPxls;
		
		float[] bottomLeft  = { cropU * texelWidth,
							    cropV * texelHeight };
		float[] topLeft     = { cropU * texelWidth,
							    (cropV + cropH) * texelHeight };
		float[] bottomRight = { (cropU + cropW) * texelWidth,
								cropV * texelHeight };
		float[] topRight    = { (cropU + cropW) * texelWidth,
								(cropV + cropH) * texelHeight };
		
		// first triangle
		mTexCoords[0] = topLeft[0];
		mTexCoords[1] = topLeft[1];
		mTexCoords[2] = bottomLeft[0];
		mTexCoords[3] = bottomLeft[1];
		mTexCoords[4] = topRight[0];
		mTexCoords[5] = topRight[1];
		// second triangle
		mTexCoords[6] = bottomLeft[0];
		mTexCoords[7] = bottomLeft[1];
		mTexCoords[8] = bottomRight[0];
		mTexCoords[9] = bottomRight[1];
		mTexCoords[10] = topRight[0];
		mTexCoords[11] = topRight[1];
	}
}
