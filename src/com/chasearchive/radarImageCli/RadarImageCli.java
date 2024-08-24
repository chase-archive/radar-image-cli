package com.chasearchive.radarImageCli;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class RadarImageCli {

	public static void main(String[] args) {
		System.out.println("input args: " + Arrays.toString(args));
		
		DateTime dt = null;
		double lat = -1024;
		double lon = -1024;
		
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
				lat = Double.valueOf(lat);
			} else if("-dt".equals(flag)) {
				lon = Double.valueOf(lon);
			} else {
				
			}
		}
		
		System.out.println(dt);
		System.out.println(lat);
		System.out.println(lon);
	}
}
