package com.chasearchive.radarImageCli.satellite;

import com.ameliaWx.wxArchives.earthWeather.goes.SatelliteSector;
import com.chasearchive.radarImageCli.AspectRatio;

public class SatelliteGeneratorSettings {
	private AspectRatio aspectRatio = AspectRatio.SQUARE;
	private SatelliteImageType imageType = SatelliteImageType.GEOCOLOR;
	private SatelliteSource source = SatelliteSource.GOES_EAST;
	private SatelliteSector sector = SatelliteSector.GOES_CONUS;
	private double size = 0.5; // domain height in degreesMoment
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
	
	public SatelliteImageType getImageType() {
		return imageType;
	}
	
	public void setImageType(SatelliteImageType imageType) {
		this.imageType = imageType;
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

	public SatelliteSource getSource() {
		return source;
	}

	public void setSource(SatelliteSource source) {
		this.source = source;
	}

	public SatelliteSector getSector() {
		return sector;
	}

	public void setSector(SatelliteSector sector) {
		this.sector = sector;
	}
}
