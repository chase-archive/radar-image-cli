package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class GoesTest {
	public static void main(String[] args) {
		String[] args1 = {"-dt", "20160411_2300", "-lat", "33.50", "-lon", "-96.50", 
				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "gridsat/satgen-case-wylieTx-VIS.png"};
		
		SatelliteImageCli.main(args1);
	}
}
