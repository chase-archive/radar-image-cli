package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class GoesTest {
	public static void main(String[] args) {
		String[] args1 = {"-dt", "20110814_0100", "-lat", "53.08", "-lon", "-114.97", 
				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "gridsat/satgen-case-draytonValleyAb-VISalt.png"};
//		String[] args1 = {"-dt", "20250212_1600", "-lat", "35.18", "-lon", "-97.44", 
//				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "goes/satgen-case-normanOkPostThundersleet-VIS.png"};
		
		SatelliteImageCli.main(args1);
	}
}
