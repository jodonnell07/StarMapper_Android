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
	
	// texture-related data
	private int mWidthPxls;
	private int mHeightPxls;
	private float[] mTexCoords;
	
	
	// Constructors
	public Label(String text, Geocentric pos, int color, int size, LabelTypeEnum type) {
		this.text = text;
		this.color = color;
		this.size = size;
		this.type = type;
		position = new Geocentric(pos.x, pos.y, pos.z);
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
	public float[] getTexCoords() {
		return mTexCoords;
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
