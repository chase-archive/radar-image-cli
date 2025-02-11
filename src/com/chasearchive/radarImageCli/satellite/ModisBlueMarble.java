package com.chasearchive.radarImageCli.satellite;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ModisBlueMarble {
	public static BufferedImage blueMarble;
	
	public static float[] lon;
	public static float[] lat;
	
	public static float dlon;
	public static float dlat;
	
	static {
		try {
			// using the may one, but interchangeable as long as it's 5400x2700
			blueMarble = ImageIO.read(new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/src/com/chasearchive/radarImageCli/res/modis.bluemarble.world.topo.bathy.200405.3x5400x2700.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int width = blueMarble.getWidth();
		int height = blueMarble.getHeight();
		
		dlon = 360.0f / width;
		dlat = -180.0f / height;
		
		lon = new float[(int) Math.round(360.0f / dlon)];
		lat = new float[(int) Math.round(-180.0f / dlat)];
		
		for(int i = 0; i < lon.length; i++) {
			lon[i] = -180.0f + dlon * (i + 0.5f);
		}
		
		for(int i = 0; i < lat.length; i++) {
			lat[i] = 90.0f + dlat * (i + 0.5f);
		}
	}
	
	public static Color getColor(GeoCoord p) {
		int i = (int) ((p.getLon() + 180) / dlon - 0.5f);
		int j = (int) ((p.getLat() - 90) / dlat - 0.5f);
		
		return new Color(blueMarble.getRGB(i, j));
	}
	
	public static Color getColor(float lat, float lon) {
		return getColor(new GeoCoord(lat, lon));
	}
}
