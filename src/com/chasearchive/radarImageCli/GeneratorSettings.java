package com.chasearchive.radarImageCli;

public class GeneratorSettings {
	private AspectRatio aspectRatio = AspectRatio.FOUR_THREE;
	private Moment moment = Moment.REFLECTIVITY;
	private double size = 8; // domain height in degrees
	private double resolution = 1080; // height in pixels
	
	public AspectRatio getAspectRatio() {
		return aspectRatio;
	}
	
	public double getAspectRatioFloat() {
		switch(aspectRatio) {
		case FOUR_THREE:
			return 4.0/3.0;
		case SIXTEEN_NINE:
			return 16.0/9.0;
		case SQUARE:
			return 1;
		default:
			return 1;
		}
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
}
