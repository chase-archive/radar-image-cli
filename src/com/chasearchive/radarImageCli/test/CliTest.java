package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.RadarImageCli;

public class CliTest {
	public static void main(String[] args) {
		String[] args1 = {"-dt", "20240430_2330", "-lat", "34.86", "-lon", "-98.96", "-a", "4:3", "-s", "2.0", "-debug", "VERBOSE", "-o", "radargen-case-rooseveltOk.png"};
		
		RadarImageCli.main(args1);
	}
}
