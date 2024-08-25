package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.RadarImageCli;

public class CliTest {
	public static void main(String[] args) {
		String[] args1 = {"-dt", "20240420_2330", "-lat", "64.79", "-lon", "-151.131"};
		
		RadarImageCli.main(args1);
	}
}
