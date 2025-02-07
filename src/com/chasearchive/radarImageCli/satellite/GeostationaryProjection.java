package com.chasearchive.radarImageCli.satellite;

public class GeostationaryProjection implements MapProjection {
	private double perspectiveHeight;
	private double semiMajorAxis;
	private double semiMinorAxis;
	private double longitudeOrigin;
	
	public static final GeostationaryProjection GOES_EAST = new GeostationaryProjection(3.5786023E7 + 6378137.0, 6378137.0, 6356752.31414, -75.0);
	public static final GeostationaryProjection GOES_WEST = new GeostationaryProjection(3.5786023E7 + 6378137.0, 6378137.0, 6356752.31414, -137.0);

	public static void main(String[] args) {
		VirtualCoord test1 = new VirtualCoord(-0.054124, -0.055748);
		
		System.out.println(GOES_EAST.projectXYToLatLon(test1));
	}
	
	public GeostationaryProjection(double perspectiveHeight, double semiMajorAxis, double semiMinorAxis, double longitudeOrigin) {
		this.perspectiveHeight = perspectiveHeight;
		this.semiMajorAxis = semiMajorAxis;
		this.semiMinorAxis = semiMinorAxis;
		this.longitudeOrigin = longitudeOrigin;
	}

	@Override
	public VirtualCoord projectLatLonToXY(double latitude, double longitude) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualCoord projectLatLonToXY(GeoCoord p) {
		return projectLatLonToXY(p.getLat(), p.getLon());
	}

	/**
	 * @param x Units: Radians
	 * @param y Units: Radians
	 */
	@Override
	public GeoCoord projectXYToLatLon(double x, double y) {
		double lambda0 = Math.toRadians(longitudeOrigin);

		final double H = perspectiveHeight;
		final double r_eq = semiMajorAxis;
		final double r_pol = semiMinorAxis;

		double a = Math.pow(Math.sin(x), 2) + (Math.pow(Math.cos(x), 2) * Math.pow(Math.cos(y), 2)
				+ (((Math.pow(r_eq, 2)) / (Math.pow(r_pol, 2))) * Math.pow(Math.sin(y), 2)));
		double b = -2 * H * Math.cos(x) * Math.cos(y);
		double c = Math.pow(H, 2) - Math.pow(r_eq, 2);

		double r_s = (-b - Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);

		double s_x = r_s * Math.cos(x) * Math.cos(y);
		double s_y = r_s * Math.sin(x);
		double s_z = r_s * Math.cos(x) * Math.sin(y);

		double phi = (Math.atan(
				(Math.pow(r_eq, 2) / Math.pow(r_pol, 2)) * ((s_z / Math.sqrt(((H - s_x) * (H - s_x)) + (s_y * s_y))))));
		double lambda = lambda0 - Math.atan(s_y / (H - s_x));

		double lat = Math.toDegrees(phi);
		double lon = Math.toDegrees(lambda);

		return new GeoCoord(lat, lon);
	}

	@Override
	public GeoCoord projectXYToLatLon(VirtualCoord p) {
		return projectXYToLatLon(p.getX(), p.getY());
	}

	@Override
	public boolean inDomain(double latitude, double longitude) {
		return false;
	}

	@Override
	public boolean inDomain(VirtualCoord p) {
		return false;
	}
}
