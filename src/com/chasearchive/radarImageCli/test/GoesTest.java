package com.chasearchive.radarImageCli.test;

import java.io.IOException;

import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

import ucar.nc2.NetcdfFile;

public class GoesTest {
	public static void main(String[] args) {
		NetcdfFile ncfile;
		try {
			ncfile = NetcdfFile.open("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/gridsat/GridSat-CONUS.goes13.2016.04.11.2330.v01.nc");
			
			System.out.println(ncfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
		
		String[] args1 = {"-dt", "20230322_1814", "-lat", "33.99", "-lon", "-118.13", 
				"-a", "4:3", "-t", "LIR", "-s", "2.0", "-debug", "SILENT", "-o", "goes/satgen-case-losAngelesCa-LIR.png"};
		
		SatelliteImageCli.main(args1);
	}
}
