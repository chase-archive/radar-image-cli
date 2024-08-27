package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.RadarImageCli;

public class CliTest {
	public static void main(String[] args) {
//		String[] args1 = {"-dt", "20240430_2353", "-lat", "34.86", "-lon", "-98.96", "-a", "4:3", "-s", "0.5", "-debug", "SILENT", "-o", "radargen-case-rooseveltOk.png"};
//		String[] args1 = {"-dt", "20240811_1100", "-lat", "35.18", "-lon", "-97.44", "-a", "4:3", "-s", "2.0", "-debug", "VERBOSE", "-o", "radargen-case-flood-zoomOut.png"};
//		String[] args1 = {"-dt", "20240503_0015", "-lat", "32.606786", "-lon", "-99.855", "-a", "4:3", "-m", "BV", "-s", "0.5", "-o", "radargen-case-hawleyTx-bv.png"};
		String[] args1 = {"-dt", "20240805_2230", "-lat", "44.06", "-lon", "-94.74", "-a", "4:3", "-s", "2.0", "-o", "radargen-case-darthurMn-zoomOut.png"};
		
		long startTime = System.currentTimeMillis();
		RadarImageCli.main(args1);
		long endTime = System.currentTimeMillis();
		
		System.out.println("Exectime: " + (endTime - startTime) + " ms");
	}
}
