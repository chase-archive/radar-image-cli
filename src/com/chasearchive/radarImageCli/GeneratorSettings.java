package com.chasearchive.radarImageCli;

public class GeneratorSettings {
	private AspectRatio aspectRatio = AspectRatio.SQUARE;
	private Moment moment = Moment.REFLECTIVITY;
	private double size = 0.5; // domain height in degrees
	private double resolution = 720; // height in pixels
	private boolean debugMode = false;
	
	public AspectRatio getAspectRatio() {
		return aspectRatio;
	}
	public void setAspectRatio(AspectRatio aspectRatio) {
		this.aspectRatio = aspectRatio;
	}
	public Moment getMoment() {
		return moment;
	}
	public void setMoment(Moment moment) {
		this.moment = moment;
	}
	public double getSize() {
		return size;
	}
	public void setSize(double size) {
		this.size = size;
	}
	public double getResolution() {
		return resolution;
	}
	public void getSize(double resolution) {
		this.resolution = resolution;
	}
	public boolean isDebugMode() {
		return debugMode;
	}
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
}
