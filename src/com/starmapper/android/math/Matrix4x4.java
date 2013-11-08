package com.starmapper.android.math;

public class Matrix4x4 {

	private float[] mElements = new float[16];
	
	// Constructors
	public Matrix4x4() {}
	public Matrix4x4(float[] values) {
		for (int i = 0; i < 16; i++) {
			mElements[i] = values[i];
		}
	}
	
	
	
	public float[] getElements() {
		return mElements;
	}
	
	public void setMatrix4x4(float[] values) {
		for (int i = 0; i < 16; i++) {
			mElements[i] = values[i];
		}
	}
	
	public void setMatrix4x4(Geocentric lookDir, Geocentric upDir, Geocentric crossProduct) {
		float [] values = new float[] {
				crossProduct.x, upDir.x, -lookDir.x, 0,
				crossProduct.y, upDir.y, -lookDir.y, 0,
				crossProduct.z, upDir.z, -lookDir.z, 0,
				0, 0, 0, 1
		};
		this.setMatrix4x4(values);
	}
}
