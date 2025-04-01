package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.RadarImageCli;
import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class GoesTest {
	public static void main(String[] args) {
//		String[] args1 = {"-dt", "20110814_0100", "-lat", "53.08", "-lon", "-114.97", 
//				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "gridsat/satgen-case-draytonValleyAb-VISalt.png"};
		String[] args1 = {"-dt", "20230525_0100", "-lat", "34.83", "-lon", "-103.27", 
				"-a", "4:3", "-t", "VIS", "-s", "2.0", "-debug", "SILENT", "-o", "goes/satgen-case-gradyNm-VIS.png"};
//		String[] args1 = {"-dt", "20200814_1830", "-lat", "-27.10", "-lon", "-51.24", 
//				"-a", "4:3", "-t", "LIR", "-s", "2.0", "-debug", "SILENT", "-o", "goes/satgen-test-aguaDoceSc-LIR.png"};
		
		String[] args2 = {"-dt", "20230525_0100", "-lat", "34.83", "-lon", "-103.27", 
				"-a", "4:3", "-s", "0.5", "-debug", "SILENT", "-o", "goes/radargen-case-gradyNm-BR.png"};
		
		SatelliteImageCli.main(args1);
	}
}
