package com.chasearchive.radarImageCli;

public class RadarGeneratorSettings {
	private AspectRatio aspectRatio = AspectRatio.FOUR_THREE;
	private Moment moment = Moment.REFLECTIVITY;
	private Source source = Source.NEXRAD;
	private double size = 0.5; // domain height in degrees
	private double resolution = 1080; // height in pixels
	private Layering layering = Layering.COMPOSITE_ONLY;
	
	public AspectRatio getAspectRatio() {
		return aspectRatio;
	}
	
	public double getAspectRatioFloat() {
		switch(aspectRatio) {
		case SQUARE:
			return 1;
		case FOUR_THREE:
			return 4.0/3.0;
		case THREE_TWO:
			return 3.0/2.0;
		case SIXTEEN_NINE:
			return 16.0/9.0;
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
	
	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Layering getLayering() {
		return layering;
	}

	public void setLayering(Layering layering) {
		this.layering = layering;
	}
}
