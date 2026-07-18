package com.chasearchive.radarImageCli.satellite;

import com.ameliaWx.wxArchives.earthWeather.goes.SatelliteSector;
import com.chasearchive.radarImageCli.AspectRatio;
import com.chasearchive.radarImageCli.Layering;

public class SatelliteGeneratorSettings {
	private AspectRatio aspectRatio = AspectRatio.FOUR_THREE;
	private SatelliteImageType imageType = SatelliteImageType.GEOCOLOR;
	private SatelliteSource source = SatelliteSource.GOES_EAST;
	private SatelliteSector sector = SatelliteSector.GOES_CONUS;
	private double size = 0.5; // domain height in degreesMoment
	private double resolution = 1080; // height in pixels
	private Layering layering = Layering.COMPOSITE_ONLY;
	private boolean drawTimestamp = true;
	private boolean useClass = false;
	private String classOrderId = "";
	
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

	public Layering getLayering() {
		return layering;
	}

	public void setLayering(Layering layering) {
		this.layering = layering;
	}

	public boolean isDrawTimestamp() {
		return drawTimestamp;
	}

	public void setDrawTimestamp(boolean drawTimestamp) {
		this.drawTimestamp = drawTimestamp;
	}

	public boolean isUseClass() { return useClass; }

	public void setUseClass(boolean useClass) {
		this.useClass = useClass;
	}

	public String getClassOrderId() {
		return classOrderId;
	}

	public void setClassOrderId(String classOrderId) {
		this.classOrderId = classOrderId;
	}
}
