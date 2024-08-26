package com.chasearchive.radarImageCli;

public class RadarSite {
	private String siteCode;
	private String siteCity;
	private PointD siteCoords;
	
	public RadarSite(String siteCode, String siteCity, PointD siteCoords) {
		this.siteCode = siteCode;
		this.siteCity = siteCity;
		this.siteCoords = siteCoords;
	}
	
	public RadarSite(String siteCode, String siteCity, double latitude, double longitude) {
		this(siteCode, siteCity, new PointD(latitude, longitude));
	}

	public String getSiteCode() {
		return siteCode;
	}

	public String getSiteCity() {
		return siteCity;
	}

	public PointD getSiteCoords() {
		return siteCoords;
	}
	
	public String toString() {
		return siteCode + " - " + siteCity;
	}
}
