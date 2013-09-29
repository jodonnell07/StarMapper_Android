package com.starmapper.android.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class Flinger extends GestureDetector.SimpleOnGestureListener {

	public interface FlingListener {
		void fling(float distanceX, float distanceY);
	}
	
	private FlingListener listener;
	private int updatesPerSecond = 50;
	private int timeIntervalMs   = 1000 / updatesPerSecond;
	private ScheduledExecutorService executor;
	private ScheduledFuture<?> flingJob;
	
	// Constructors
	public Flinger(FlingListener listener) {
		this.listener = listener;
		executor = Executors.newScheduledThreadPool(1);
	}
	
	public void fling(float velocityX, float velocityY) {
		class flingUpdater implements Runnable {
			private float myVelocityX;
			private float myVelocityY;
			private float deceleration = 1.05f;
			private float TOL = 10;
			
			// Constructor
			public flingUpdater(float velocityX, float velocityY) {
				this.myVelocityX = velocityX;
				this.myVelocityY = velocityY;
			}
			
			// Run method
			public void run() {
				if (myVelocityX * myVelocityX + myVelocityY * myVelocityY  < TOL) {
					stop();
				}
				listener.fling(myVelocityX / updatesPerSecond, myVelocityY / updatesPerSecond);
				myVelocityX /= deceleration;
				myVelocityY /= deceleration;
			}
		}
		flingJob = executor.scheduleAtFixedRate(new flingUpdater(velocityX, velocityY), 0, timeIntervalMs, TimeUnit.MILLISECONDS);
	}
	
	public void stop() {
		if (flingJob != null) flingJob.cancel(true);
	}
	
	// Gesture Overrides
	@Override
	public boolean onDown(MotionEvent e) {
		stop();
		return true;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		fling(velocityX, velocityY);
		return true;
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return true;
	}
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		return false;
	}
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}
}
