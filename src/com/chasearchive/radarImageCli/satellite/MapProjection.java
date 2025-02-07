package com.chasearchive.radarImageCli.satellite;

public interface MapProjection {
	public VirtualCoord projectLatLonToXY(double latitude, double longitude);
	public VirtualCoord projectLatLonToXY(GeoCoord p);
	public GeoCoord projectXYToLatLon(double i, double j);
	public GeoCoord projectXYToLatLon(VirtualCoord p);
	public boolean inDomain(double i, double j);
	public boolean inDomain(VirtualCoord p);

	default double sin(double theta) {
		return (Math.sin(Math.toRadians(theta)));
	}

	default double cos(double theta) {
		return (Math.cos(Math.toRadians(theta)));
	}

	default double tan(double theta) {
		return (Math.tan(Math.toRadians(theta)));
	}

	default double sec(double theta) {
		return 1 / cos(theta);
	}

	default double csc(double theta) {
		return 1 / sin(theta);
	}

	default double cot(double theta) {
		return 1 / tan(theta);
	}
}
