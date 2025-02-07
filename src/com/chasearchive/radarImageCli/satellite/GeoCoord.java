package com.chasearchive.radarImageCli.satellite;

public class GeoCoord {
	private float lat; // units are ALWAYS degrees
	private float lon;

	public GeoCoord(double lat, double lon) {
		this.lat = (float) lat;
		this.lon = (float) lon;
	}

	public GeoCoord(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLon() {
		return lon;
	}

	public void setLon(float lon) {
		this.lon = lon;
	}

	@Override
	public String toString() {
		return "GeoCoord [lat=" + lat + ", lon=" + lon + "]";
	}
}
