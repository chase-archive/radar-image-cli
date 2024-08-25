package com.chasearchive.radarImageCli;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class RadarImageCli {
	// TODO:
	// Radar data download/decoder/storage
	// Radar data plotter (not obscenely slow)
	// Maybe city markers?
	
	static final DebugLogger logger = new DebugLogger(DebugLoggerLevel.VERBOSE);
	
	public static void main(String[] args) {
		System.out.println("input args: " + Arrays.toString(args));
		
		DateTime dt = null;
		double lat = -1024;
		double lon = -1024;
		GeneratorSettings settings = new GeneratorSettings();
		
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
			} else {
				
			}
		}
		
		logger.println(dt, DebugLoggerLevel.BRIEF);
		logger.println(lat, DebugLoggerLevel.BRIEF);
		logger.println(lon, DebugLoggerLevel.BRIEF);
		
		try {
			BufferedImage radar = RadarImageGenerator.generateRadar(dt, lat, lon, settings);
			
			ImageIO.write(radar, "PNG", new File("basemap-test5.png"));
		} catch (IOException e) {
			System.err.println("Could not generate radar! Send following error message to Amelia:");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
