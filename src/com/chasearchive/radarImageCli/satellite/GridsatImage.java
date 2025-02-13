package com.chasearchive.radarImageCli.satellite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.chasearchive.radarImageCli.ColorTable;
import com.chasearchive.radarImageCli.RadarImageGenerator;

import ucar.nc2.NetcdfFile;

public class GridsatImage extends CdmFile implements SatelliteImage {
	private static final ColorTable brightnessTemperatureColorTable = new ColorTable(
			RadarImageGenerator.loadResourceAsFile("res/aru-br-temp.pal"), 0.1f, 10, "dBZ");
	
	public static void main(String[] args) throws IOException {
		File datafile = new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/gridsat/GridSat-CONUS.goes13.2016.04.11.2330.v01.nc");
		
		@SuppressWarnings("deprecation")
		NetcdfFile ncfile = NetcdfFile.open(datafile.getAbsolutePath());
		System.out.println(ncfile);
		
		GridsatImage gridsat = GridsatImage.loadFromFile(datafile);
		
		float[][] vis = gridsat.field("vis").array3D()[0];
		float[] lat = gridsat.field("lat").array1D();
		float[] lon = gridsat.field("lon").array1D();
		
		System.out.println(lat[0]);
		System.out.println(lat[649]);
		System.out.println(lon[0]);
		System.out.println(lon[1299]);
		System.out.println(lat[1] - lat[0]);
		System.out.println(lon[1] - lon[0]);
		
		BufferedImage visImg = new BufferedImage(vis[0].length, vis.length, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = visImg.createGraphics();
		
		for(int i = 0; i < vis.length; i++) {
			for(int j = 0; j < vis[i].length; j++) {
				int gray = (int) (255 * vis[i][j]);

				if(gray < 0) gray = 0;
				if(gray > 255) gray = 255;
				
				g.setColor(new Color(gray, gray, gray));
				g.fillRect(j, vis.length - i - 1, 1, 1);
			}
		}
		
		ImageIO.write(visImg, "PNG", new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/gridsat/vis-20160411.png"));
		
		float[][] lir = gridsat.field("lir").array3D()[0];
		
		BufferedImage lirImg = new BufferedImage(lir[0].length, lir.length, BufferedImage.TYPE_3BYTE_BGR);
		g = lirImg.createGraphics();
		
		for(int i = 0; i < lir.length; i++) {
			for(int j = 0; j < lir[i].length; j++) {
				g.setColor(brightnessTemperatureColorTable.getColor(lir[i][j]));
				g.fillRect(j, lir.length - i - 1, 1, 1);
			}
		}
		
		ImageIO.write(lirImg, "PNG", new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/gridsat/lir-20160411.png"));
	}

	@SuppressWarnings("deprecation")
	public static GridsatImage loadFromFile(File f) throws IOException {
		GridsatImage image = new GridsatImage();
		
		image.locationOnDisk = f.getAbsolutePath();
		
		NetcdfFile ncfile = NetcdfFile.open(image.locationOnDisk);
		
		image.permaFields.put("lat", DataField.fromCdmVar(ncfile.findVariable("lat")));
		image.permaFields.put("lon", DataField.fromCdmVar(ncfile.findVariable("lon")));
		
		image.permaFields.put("vis", DataField.fromCdmVar(ncfile.findVariable("ch1")));
		image.permaFields.get("vis").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("ch1").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("vis").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("ch1").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("vis").bundleField("fill_value", DataField.fromNumber(ncfile.findVariable("ch1").findAttributeDouble("missing_value", -1024)));
		image.permaFields.get("vis").processOffsets();
		
		image.permaFields.put("sir", DataField.fromCdmVar(ncfile.findVariable("ch2")));
		image.permaFields.get("sir").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("ch2").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("sir").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("ch2").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("sir").bundleField("fill_value", DataField.fromNumber(ncfile.findVariable("ch2").findAttributeDouble("missing_value", -1024)));
		image.permaFields.get("sir").processOffsets();
		
		image.permaFields.put("lir", DataField.fromCdmVar(ncfile.findVariable("ch4")));
		image.permaFields.get("lir").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("ch4").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("lir").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("ch4").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("lir").bundleField("fill_value", DataField.fromNumber(ncfile.findVariable("ch4").findAttributeDouble("missing_value", -1024)));
		image.permaFields.get("lir").processOffsets();

		image.permaFields.put("dlat", DataField.fromNumber(image.dataFromField("lat", 1) - image.dataFromField("lat", 0)));
		image.permaFields.put("dlon", DataField.fromNumber(image.dataFromField("lon", 1) - image.dataFromField("lon", 0)));
		
		ncfile.close();
		return image;
	}
	
	public DataField field(String key) {
		if(permaFields.containsKey(key)) {
			return permaFields.get(key);
		} else {
			if (!swapFields.containsKey(key)) {
				try {
					loadIntoSwap(key);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return swapFields.get(key);
		}
	}
	
	public float dataFromField(String key) {
		return dataFromField(key, 0);
	}
	
	public float dataFromField(String key, int... indices) {
		if(permaFields.containsKey(key)) {
			return permaFields.get(key).getData(indices);
		} else {
			return fromSwap(key, indices);
		}
	}
	
	public float fromSwap(String key) {
		return fromSwap(key, 0);
	}
	
	public float fromSwap(String key, int... indices) {
		if (!swapFields.containsKey(key)) {
			try {
				loadIntoSwap(key);
			} catch (IOException e) {
				// pass up the chain actually this just makes it compile for now
			}
		}
		
		return swapFields.get(key).getData(indices);
	}
	
	public void loadIntoSwap(String... keys) throws IOException {
		
	}
}
