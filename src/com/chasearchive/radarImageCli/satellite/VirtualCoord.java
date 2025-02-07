package com.chasearchive.radarImageCli.satellite;

public class VirtualCoord {
	private float x; // units are whatever the user wants, sometimes km
	private float y;

	public VirtualCoord(double x, double y) {
		this.x = (float) x;
		this.y = (float) y;
	}

	public VirtualCoord(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "VirtualCoord [x=" + x + ", y=" + y + "]";
	}
}
