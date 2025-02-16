package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class GoesTest {
	public static void main(String[] args) {
//		String[] args1 = {"-dt", "20110814_0100", "-lat", "53.08", "-lon", "-114.97", 
//				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "gridsat/satgen-case-draytonValleyAb-VISalt.png"};
//		String[] args1 = {"-dt", "20240503_0015", "-lat", "32.61", "-lon", "-99.86", 
//				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "goes/satgen-case-hawleyTx-VIS.png"};
//		String[] args1 = {"-dt", "20200814_1830", "-lat", "-27.10", "-lon", "-51.24", 
//				"-a", "4:3", "-t", "LIR", "-s", "2.0", "-debug", "SILENT", "-o", "goes/satgen-test-aguaDoceSc-LIR.png"};
		String[] args1 = {"-dt", "20200814_1830", "-lat", "25", "-lon", "-78", 
				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "goes/satgen-test-bahamas-VIS.png"};
		
		SatelliteImageCli.main(args1);
	}
}
