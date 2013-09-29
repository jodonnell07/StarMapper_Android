package com.starmapper.android.math;

public class Matrix3x3 {

	public float xx;
	public float xy;
	public float xz;
	public float yx;
	public float yy;
	public float yz;
	public float zx;
	public float zy;
	public float zz;
	
	// Constructors
	public Matrix3x3(float xx, float xy, float xz,
					 float yx, float yy, float yz,
					 float zx, float zy, float zz) {
		this.xx = xx;
		this.xy = xy;
		this.xz = xz;
		this.yx = yx;
		this.yy = yy;
		this.yz = yz;
		this.zx = zx;
		this.zy = zy;
		this.zz = zz;
	}
	public Matrix3x3() {
		new Matrix3x3(0,0,0,0,0,0,0,0,0);
	}
}
