package com.chasearchive.radarImageCli.satellite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class RotateLatLonProjection implements MapProjection {
	private double latitudeRotation;
	private double longitudeRotation;
	public double dx;
	public double dy;
	private double offsetX;
	private double offsetY;
	private int nx;
	private int ny;

	private double[][] rotationMatrixLon;
	private double[][] rotationMatrixLat;

	// lonRot = -65.30511474609375
	public static final RotateLatLonProjection HRDPS_CONT_PROJ = new RotateLatLonProjection(36.08852005004883-90,
			97.56 + 17.18, 2.5, 2.5, -613, 98, 2540, 1290);
	public static final RotateLatLonProjection BLANK_PROJ = new RotateLatLonProjection(-90, 0, 2.5, 2.5, 2540, 1290);
	public static final RotateLatLonProjection TEST_PROJ = new RotateLatLonProjection(60, -90, 111.32, 111.32, 2540, 1290);

	public static void main(String[] args) throws IOException {
		BufferedImage img = new BufferedImage(360, 181, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();

		System.out.println(TEST_PROJ.rotateLatLon(90, -60));
		System.out.println(TEST_PROJ.rotateLatLon(-90, 60));
		System.out.println(TEST_PROJ.rotateLatLon(0, 0));
		System.out.println(TEST_PROJ.rotateLatLon(-97.56, 54));

		for (int i = 0; i < 360; i++) {
			for (int j = 0; j < 181; j++) {
				double lon = i - 180;
				double lat = 90 - j;

				GeoCoord llP = TEST_PROJ.rotateLatLon(lon, lat);
				double latP = llP.getLat();
				double lonP = llP.getLon();

				if(lonP > -91 && lonP < -89 && latP > 59 && latP < 61) {
					g.setColor(Color.WHITE);
				} else if(lonP > -150 && lonP < -50) {
					if (latP > 85) {
						g.setColor(Color.RED);
					} else if (latP > 45) {
						g.setColor(Color.ORANGE);
					} else if (latP > 0) {
						g.setColor(Color.YELLOW);
					} else if (latP > -45) {
						g.setColor(Color.GREEN);
					} else if (latP > -85) {
						g.setColor(Color.BLUE);
					} else {
						g.setColor(Color.MAGENTA);
					}
				} else {
					g.setColor(Color.DARK_GRAY);
				}

				g.fillRect(i, j, 1, 1);
			}
		}

		ImageIO.write(img, "PNG", new File("rotateLatLon_testHrdps.png"));
	}


	public RotateLatLonProjection(double latitudeRotation, double longitudeRotation, double dx, double dy, int nx, int ny) {
		this(latitudeRotation, longitudeRotation, dx, dy, 0, 0, nx, ny);
	}
	
	public RotateLatLonProjection(double latitudeRotation, double longitudeRotation, double dx, double dy, double offsetX, double offsetY, int nx,
			int ny) {
		this.latitudeRotation = latitudeRotation;
		this.longitudeRotation = longitudeRotation;
		this.dx = dx;
		this.dy = dy;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.nx = nx;
		this.ny = ny;

		double tht = -this.latitudeRotation;
		double phi = -this.longitudeRotation;

		double cosTht = Math.cos(Math.toRadians(tht));
		double sinTht = Math.sin(Math.toRadians(tht));
		double cosPhi = Math.cos(Math.toRadians(phi));
		double sinPhi = Math.sin(Math.toRadians(phi));

		rotationMatrixLat = new double[][] { { cosTht, 0, sinTht, }, { 0, 1, 0 }, { -sinTht, 0, cosTht } };

		rotationMatrixLon = new double[][] { { cosPhi, sinPhi, 0, }, { -sinPhi, cosPhi, 0 }, { 0, 0, 1 } };

//		rotationMatrix = new double[][] {
//			{ 
//				Math.cos(Math.toRadians(tht)) * Math.cos(Math.toRadians(phi)), 
//				-Math.cos(Math.toRadians(tht)) * Math.sin(Math.toRadians(phi)), 
//				-Math.sin(Math.toRadians(phi))
//			},
//			{ 
//				Math.sin(Math.toRadians(phi)), 
//				Math.cos(Math.toRadians(phi)), 
//				0
//			},
//			{ 
//				Math.sin(Math.toRadians(tht)) * Math.cos(Math.toRadians(phi)), 
//				-Math.sin(Math.toRadians(tht)) * Math.sin(Math.toRadians(phi)), 
//				Math.cos(Math.toRadians(phi))
//			}
//		};
	}

	public VirtualCoord projectLatLonToIJ(double longitude, double latitude) {
		GeoCoord latLonPrime = rotateLatLon(longitude, latitude);

		double lonPrime = latLonPrime.getLon();
		double latPrime = latLonPrime.getLat();

		double i = nx / 2.0 + lonPrime * (111.32 / dx);
		double j = ny - (ny / 2.0 + latPrime * (111.32 / dy));

		return new VirtualCoord(i + offsetX, j + offsetY);
	}

	public GeoCoord rotateLatLon(double longitude, double latitude) {
		double x = Math.cos(Math.toRadians(longitude)) * Math.cos(Math.toRadians(latitude));
		double y = Math.sin(Math.toRadians(longitude)) * Math.cos(Math.toRadians(latitude));
		double z = Math.sin(Math.toRadians(latitude));

		double xM = rotationMatrixLon[0][0] * x + rotationMatrixLon[1][0] * y + rotationMatrixLon[2][0] * z;
		double yM = rotationMatrixLon[0][1] * x + rotationMatrixLon[1][1] * y + rotationMatrixLon[2][1] * z;
		double zM = rotationMatrixLon[0][2] * x + rotationMatrixLon[1][2] * y + rotationMatrixLon[2][2] * z;

		double xP = rotationMatrixLat[0][0] * xM + rotationMatrixLat[1][0] * yM + rotationMatrixLat[2][0] * zM;
		double yP = rotationMatrixLat[0][1] * xM + rotationMatrixLat[1][1] * yM + rotationMatrixLat[2][1] * zM;
		double zP = rotationMatrixLat[0][2] * xM + rotationMatrixLat[1][2] * yM + rotationMatrixLat[2][2] * zM;

		double lonPrime = Math.toDegrees(Math.atan2(yP, xP));
		double rPlanar = Math.hypot(xP, yP);
		double latPrime = Math.toDegrees(Math.atan2(zP, rPlanar));

		return new GeoCoord(latPrime, lonPrime);
	}

	public GeoCoord rotateLatLon(float longitude, float latitude) {
		double x = Math.cos(Math.toRadians(longitude)) * Math.cos(Math.toRadians(latitude));
		double y = Math.sin(Math.toRadians(longitude)) * Math.cos(Math.toRadians(latitude));
		double z = Math.sin(Math.toRadians(latitude));

		double xM = rotationMatrixLon[0][0] * x + rotationMatrixLon[1][0] * y + rotationMatrixLon[2][0] * z;
		double yM = rotationMatrixLon[0][1] * x + rotationMatrixLon[1][1] * y + rotationMatrixLon[2][1] * z;
		double zM = rotationMatrixLon[0][2] * x + rotationMatrixLon[1][2] * y + rotationMatrixLon[2][2] * z;

		double xP = rotationMatrixLat[0][0] * xM + rotationMatrixLat[1][0] * yM + rotationMatrixLat[2][0] * zM;
		double yP = rotationMatrixLat[0][1] * xM + rotationMatrixLat[1][1] * yM + rotationMatrixLat[2][1] * zM;
		double zP = rotationMatrixLat[0][2] * xM + rotationMatrixLat[1][2] * yM + rotationMatrixLat[2][2] * zM;

		double lonPrime = Math.toDegrees(Math.atan2(yP, xP));
		double rPlanar = Math.hypot(xP, yP);
		double latPrime = Math.toDegrees(Math.atan2(zP, rPlanar));

		return new GeoCoord(latPrime, lonPrime);
	}

	@Override
	public VirtualCoord projectLatLonToXY(GeoCoord p) {
		return projectLatLonToIJ(p.getLon(), p.getLat());
	}

	public GeoCoord rotateLatLon(GeoCoord p) {
		return rotateLatLon(p.getLon(), p.getLat());
	}
	@Override
	public boolean inDomain(double i, double j) {
		if (i >= 0 && i <= nx - 1 && j >= 0 && j <= ny - 1)
			return true;

		return false;
	}

	@Override
	public boolean inDomain(VirtualCoord p) {
		return inDomain(p.getX(), p.getY());
	}


	@Override
	public VirtualCoord projectLatLonToXY(double latitude, double longitude) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public GeoCoord projectXYToLatLon(double i, double j) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public GeoCoord projectXYToLatLon(VirtualCoord p) {
		// TODO Auto-generated method stub
		return null;
	}
}
