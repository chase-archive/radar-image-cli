package com.chasearchive.radarImageCli.satellite;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.chasearchive.radarImageCli.AspectRatio;
import com.chasearchive.radarImageCli.DebugLogger;
import com.chasearchive.radarImageCli.DebugLoggerLevel;

public class SatelliteImageCli {
	public static final DebugLogger logger = new DebugLogger(DebugLoggerLevel.SILENT);
	
	public static void main(String[] args) {
		System.out.println("input args: " + Arrays.toString(args));
		
		DateTime dt = null;
		double lat = -1024;
		double lon = -1024;
		SatelliteGeneratorSettings settings = new SatelliteGeneratorSettings();
		String outputFileString = null;
		
		// flag/argument parsing logic
		for(int i = 0; i < args.length; i+=2) {
			String flag = args[i];
			String arg = args[i + 1];
			
			if("-dt".equals(flag)) {
				String year_ = arg.substring(0, 4);
				String month_ = arg.substring(4, 6);
				String day_ = arg.substring(6, 8);
				String hour_ = arg.substring(9, 11);
				String minute_ = arg.substring(11, 13);
				
				int year = Integer.valueOf(year_);
				int month = Integer.valueOf(month_);
				int day = Integer.valueOf(day_);
				int hour = Integer.valueOf(hour_);
				int minute = Integer.valueOf(minute_);
				
				dt = new DateTime(year, month, day, hour, minute, DateTimeZone.UTC);
			} else if("-lat".equals(flag)) {
				lat = Double.valueOf(arg);
			} else if("-lon".equals(flag)) {
				lon = Double.valueOf(arg);
			} else if("-a".equals(flag)) {
				if("1:1".equals(arg)) {
					settings.setAspectRatio(AspectRatio.SQUARE);
				} else if("4:3".equals(arg)) {
					settings.setAspectRatio(AspectRatio.FOUR_THREE);
				} else if("16:9".equals(arg)) {
					settings.setAspectRatio(AspectRatio.SIXTEEN_NINE);
				} else {
					continue;
				}
			} else if("-t".equals(flag)) {
				if("VIS".equals(arg)) {
					settings.setImageType(SatelliteImageType.GEOCOLOR);
				} else if("LIR".equals(arg)) {
					settings.setImageType(SatelliteImageType.LONGWAVE_IR);
				} else {
					continue;
				}
			} else if("-s".equals(flag)) {
				settings.setSize(Double.valueOf(arg));
			} else if("-r".equals(flag)) {
				settings.setResolution(Double.valueOf(arg));
			} else if("-debug".equals(flag)) {
				if("SILENT".equals(arg)) {
					logger.programLevel = DebugLoggerLevel.SILENT;
				} else if("BRIEF".equals(arg)) {
					logger.programLevel = DebugLoggerLevel.BRIEF;
				} else if("VERBOSE".equals(arg)) {
					logger.programLevel = DebugLoggerLevel.VERBOSE;
				} else {
					continue;
				}
			} else if("-o".equals(flag)) {
				outputFileString = arg;
			} else {
				continue; // no flags recognized, carry on to next one
			}
		}
		
		logger.println(dt, DebugLoggerLevel.BRIEF);
		logger.println(lat, DebugLoggerLevel.BRIEF);
		logger.println(lon, DebugLoggerLevel.BRIEF);
		
		try {
			BufferedImage satellite = SatelliteImageGenerator.generateSatellite(dt, lat, lon, settings);
			
			File outputFile = new File(outputFileString);
			ImageIO.write(satellite, "PNG", outputFile);
			logger.println("Output file to: " + outputFile.getAbsolutePath(), DebugLoggerLevel.BRIEF);
			
			try {
				FileUtils.deleteDirectory(new File("satellite-image-generator-temp"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			System.err.println("Could not generate satellite! Send following error message to Amelia:");
			System.err.println("Case info: time: " + dt + " lat: " + lat + " lon: " + lon);
			e.printStackTrace();
			
			try {
				FileUtils.deleteDirectory(new File("satellite-image-generator-temp"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			System.exit(1);
		}
	}
}
