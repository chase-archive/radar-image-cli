package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.RadarImageCli;

public class CliTest {
	public static void main(String[] args) {
		String[] args1 = {"-dt", "20240420_2330", "-lat", "34.86", "-lon", "-98.96"};
		
		RadarImageCli.main(args1);
	}
}
