package com.chasearchive.radarImageCli.satellite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ucar.nc2.NetcdfFile;

public class GoesMultibandImage extends CdmFile implements SatelliteImage {
	public static void main(String[] args) throws IOException {
		GoesMultibandImage goes = GoesMultibandImage.loadFromFile(new File("/home/a-urq/eclipse-workspace/RadarViewTakeFour/goesTestData/conusSector/20250205/OR_ABI-L2-MCMIPC-M6_G16_s20250361601171_e20250361603544_c20250361604065.nc"));
		
		float[] x = goes.field("x").array1D();
		float[] y = goes.field("y").array1D();
		
		BufferedImage test = new BufferedImage(x.length, y.length, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = test.createGraphics();
		
		for(int i = 0; i < x.length; i++) {
			for(int j = 0; j < y.length; j++) {
				GeoCoord gc = GeostationaryProjection.GOES_EAST.projectXYToLatLon(x[i], y[j]);
				
				double red = (gc.getLat() + 360) % 10;
				double greenBlue = (gc.getLon() + 720) % 10;
				
				if(Double.isNaN(gc.getLat())) {
					red = 0;
					greenBlue = 0;
				}

				if(Double.isNaN(gc.getLon())) {
					red = 0;
					greenBlue = 0;
				}
				
				g.setColor(new Color((int) (25.5 * red), (int) (25.5 * greenBlue), (int) (25.5 * greenBlue)));
				g.fillRect(i, j, 1, 1);
			}
		}
		
		ImageIO.write(test, "PNG", new File("latLonTest.png"));
		
		System.out.println("dx: " + goes.dataFromField("dx"));
		System.out.println("dy: " + goes.dataFromField("dy"));
	}

	@SuppressWarnings("deprecation")
	public static GoesMultibandImage loadFromFile(File f) throws IOException {
		GoesMultibandImage image = new GoesMultibandImage();
		
		image.locationOnDisk = f.getAbsolutePath();
		
		NetcdfFile ncfile = NetcdfFile.open(image.locationOnDisk);
		
//		System.out.println(ncfile);
		
		image.permaFields.put("band_1", DataField.fromCdmVar(ncfile.findVariable("CMI_C01")));
		image.permaFields.get("band_1").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("CMI_C01").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("band_1").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("CMI_C01").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("band_1").bundleField("fill_value", DataField.fromNumber(ncfile.findVariable("CMI_C01").findAttributeDouble("_FillValue", -1024)));
		image.permaFields.get("band_1").bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable("band_wavelength_C01")));
		image.permaFields.get("band_1").processOffsets();
		
		image.permaFields.put("band_2", DataField.fromCdmVar(ncfile.findVariable("CMI_C02")));
		image.permaFields.get("band_2").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("CMI_C02").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("band_2").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("CMI_C02").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("band_2").bundleField("fill_value", DataField.fromNumber(ncfile.findVariable("CMI_C02").findAttributeDouble("_FillValue", -1024)));
		image.permaFields.get("band_2").bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable("band_wavelength_C02")));
		image.permaFields.get("band_2").processOffsets();
		
		image.permaFields.put("band_3", DataField.fromCdmVar(ncfile.findVariable("CMI_C03")));
		image.permaFields.get("band_3").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("CMI_C03").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("band_3").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("CMI_C03").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("band_3").bundleField("fill_value", DataField.fromNumber(ncfile.findVariable("CMI_C03").findAttributeDouble("_FillValue", -1024)));
		image.permaFields.get("band_3").bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable("band_wavelength_C03")));
		image.permaFields.get("band_3").processOffsets();
		
		image.permaFields.put("band_7", DataField.fromCdmVar(ncfile.findVariable("CMI_C07")));
		image.permaFields.get("band_7").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("CMI_C07").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("band_7").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("CMI_C07").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("band_7").bundleField("fill_value", DataField.fromNumber(ncfile.findVariable("CMI_C07").findAttributeDouble("_FillValue", -1024)));
		image.permaFields.get("band_7").bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable("band_wavelength_C07")));
		image.permaFields.get("band_7").processOffsets();
		
		image.permaFields.put("band_13", DataField.fromCdmVar(ncfile.findVariable("CMI_C13")));
		image.permaFields.get("band_13").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("CMI_C13").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("band_13").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("CMI_C13").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("band_13").bundleField("fill_value", DataField.fromNumber(ncfile.findVariable("CMI_C13").findAttributeDouble("_FillValue", -1024)));
		image.permaFields.get("band_13").bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable("band_wavelength_C13")));
		image.permaFields.get("band_13").processOffsets();

		image.permaFields.put("x", DataField.fromCdmVar(ncfile.findVariable("x")));
		image.permaFields.get("x").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("x").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("x").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("x").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("x").processOffsets();

		image.permaFields.put("y", DataField.fromCdmVar(ncfile.findVariable("y")));
		image.permaFields.get("y").bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable("y").findAttributeDouble("scale_factor", -1024)));
		image.permaFields.get("y").bundleField("add_offset", DataField.fromNumber(ncfile.findVariable("y").findAttributeDouble("add_offset", -1024)));
		image.permaFields.get("y").processOffsets();

		image.permaFields.put("dx", DataField.fromNumber(image.dataFromField("x", 1) - image.dataFromField("x", 0)));
		image.permaFields.put("dy", DataField.fromNumber(image.dataFromField("y", 1) - image.dataFromField("y", 0)));
		
		image.permaFields.put("time_start", DataField.fromNexradAttrToStr(ncfile.findGlobalAttribute("time_coverage_start")));
		image.permaFields.put("time_end", DataField.fromNexradAttrToStr(ncfile.findGlobalAttribute("time_coverage_end")));
		image.permaFields.put("orbital_slot", DataField.fromNexradAttrToStr(ncfile.findGlobalAttribute("orbital_slot")));
		
		image.permaFields.put("goes_imager_projection", DataField.fromCdmVar(ncfile.findVariable("goes_imager_projection")));
		image.permaFields.get("goes_imager_projection").bundleField("perspective_point_height", DataField.fromNumber(ncfile.findVariable("goes_imager_projection").findAttributeDouble("perspective_point_height", -1024)));
		image.permaFields.get("goes_imager_projection").bundleField("semi_major_axis", DataField.fromNumber(ncfile.findVariable("goes_imager_projection").findAttributeDouble("semi_major_axis", -1024)));
		image.permaFields.get("goes_imager_projection").bundleField("semi_minor_axis", DataField.fromNumber(ncfile.findVariable("goes_imager_projection").findAttributeDouble("semi_minor_axis", -1024)));
		image.permaFields.get("goes_imager_projection").bundleField("latitude_origin", DataField.fromNumber(ncfile.findVariable("goes_imager_projection").findAttributeDouble("latitude_of_projection_origin", -1024)));
		image.permaFields.get("goes_imager_projection").bundleField("longitude_origin", DataField.fromNumber(ncfile.findVariable("goes_imager_projection").findAttributeDouble("longitude_of_projection_origin", -1024)));
		
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
	
	@SuppressWarnings("deprecation")
	public void loadIntoSwap(String... keys) throws IOException {
		NetcdfFile ncfile = NetcdfFile.open(locationOnDisk);
		
		// need to add ability to swap in bands 4-6, 8-12, and 14-16
		final String band4 = "band_4";
		final String band5 = "band_5";
		final String band6 = "band_6";
		final String band8 = "band_8";
		final String band9 = "band_9";
		final String band10 = "band_10";
		final String band11 = "band_11";
		final String band12 = "band_12";
		final String band14 = "band_14";
		final String band15 = "band_15";
		final String band16 = "band_16";
		
		for(String key : keys) {
			if(band4.equals(key)) {
				String bandVar = "CMI_C04";
				String waveVar = "band_wavelength_C04";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band5.equals(key)) {
				String bandVar = "CMI_C05";
				String waveVar = "band_wavelength_C05";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band6.equals(key)) {
				String bandVar = "CMI_C06";
				String waveVar = "band_wavelength_C06";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band8.equals(key)) {
				String bandVar = "CMI_C08";
				String waveVar = "band_wavelength_C08";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band9.equals(key)) {
				String bandVar = "CMI_C09";
				String waveVar = "band_wavelength_C09";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band10.equals(key)) {
				String bandVar = "CMI_C10";
				String waveVar = "band_wavelength_C10";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band11.equals(key)) {
				String bandVar = "CMI_C11";
				String waveVar = "band_wavelength_C11";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band12.equals(key)) {
				String bandVar = "CMI_C12";
				String waveVar = "band_wavelength_C12";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band14.equals(key)) {
				String bandVar = "CMI_C14";
				String waveVar = "band_wavelength_C14";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band15.equals(key)) {
				String bandVar = "CMI_C15";
				String waveVar = "band_wavelength_C15";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			} else if(band16.equals(key)) {
				String bandVar = "CMI_C16";
				String waveVar = "band_wavelength_C16";
				
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable(bandVar)));
				swapFields.get(key).bundleField("scale_factor", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("scale_factor", -1024)));
				swapFields.get(key).bundleField("add_offset", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("add_offset", -1024)));
				swapFields.get(key).bundleField("fill_value", DataField.fromNumber(ncfile.findVariable(bandVar).findAttributeDouble("_FillValue", -1024)));
				swapFields.get(key).bundleField("wavelength", DataField.fromCdmVar(ncfile.findVariable(waveVar)));
				swapFields.get(key).processOffsets();
			}
		}
		
		ncfile.close();
	}
}
