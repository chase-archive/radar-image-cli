package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.RadarImageCli;

public class CliTest {
	public static void main(String[] args) {
		String[] args1 = {"-dt", "20240430_2353", "-lat", "34.86", "-lon", "-98.96", "-a", "4:3", "-s", "0.5", "-debug", "SILENT", "-o", "radargen-case-rooseveltOk.png"};
//		String[] args1 = {"-dt", "20240811_1100", "-lat", "35.18", "-lon", "-97.44", "-a", "4:3", "-s", "2.0", "-debug", "VERBOSE", "-o", "radargen-case-flood-zoomOut.png"};
//		String[] args1 = {"-dt", "20240503_0015", "-lat", "32.606786", "-lon", "-99.855", "-a", "4:3", "-s", "0.5", "-debug", "BRIEF", "-o", "radargen-case-hawleyTx.png"};
//		String[] args1 = {"-dt", "20240520_0045", "-lat", "35.65", "-lon", "-98.96", "-a", "4:3", "-s", "0.5", "-o", "radargen-case-custerCityOk.png"};
//		String[] args1 = {"-dt", "20240129_1953", "-lat", "61.22", "-lon", "-149.90", "-a", "4:3", "-m", "BR", "-s", "2.0", "-o", "radargen-case-anchorageAk.png"};
//		String[] args1 = {"-dt", "20240823_0045", "-lat", "31.76", "-lon", "-106.44", "-a", "4:3", "-s", "0.5", "-o", "radargen-case-elPasoDustStorm.png"};
//		String[] args1 = {"-dt", "20231030_2324", "-lat", "45.34", "-lon", "-97.30", "-a", "4:3", "-s", "0.5", "-o", "radargen-case-waubaySdSnowSquall.png"};
		
		long startTime = System.currentTimeMillis();
		RadarImageCli.main(args1);
		long endTime = System.currentTimeMillis();
		
		System.out.println("Exectime: " + (endTime - startTime) + " ms");
	}
}
