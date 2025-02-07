package com.chasearchive.radarImageCli.test;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class GoesTest {
	public static void main(String[] args) {
		String[] args1 = {"-dt", "20240430_2330", "-lat", "34.79", "-lon", "-98.85", 
				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "goes/satgen-case-coopertonOk-VIS.png"};
		
		SatelliteImageCli.main(args1);
	}
}
