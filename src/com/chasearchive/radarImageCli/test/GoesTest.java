package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class GoesTest {
	public static void main(String[] args) {
		String[] args1 = {"-dt", "20230322_1814", "-lat", "33.99", "-lon", "-118.13", 
				"-a", "4:3", "-t", "LIR", "-s", "2.0", "-debug", "SILENT", "-o", "goes/satgen-case-losAngelesCa-LIR.png"};
		
		SatelliteImageCli.main(args1);
	}
}
