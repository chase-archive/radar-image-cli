package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class GoesTest {
	public static void main(String[] args) {
		String[] args1 = {"-dt", "20150604_2345", "-lat", "39.14", "-lon", "-104.07", 
				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "gridsat/satgen-case-simlaCo-VISalt.png"};
//		String[] args1 = {"-dt", "20240715_2300", "-lat", "62.33", "-lon", "-150.11", 
//				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "gridsat/satgen-case-talkeetnaAk-VIS.png"};
		
		SatelliteImageCli.main(args1);
	}
}
