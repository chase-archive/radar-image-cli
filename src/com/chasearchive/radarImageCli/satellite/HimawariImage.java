package com.chasearchive.radarImageCli.satellite;

import java.io.File;
import java.io.IOException;

import ucar.nc2.NetcdfFile;

public class HimawariImage {
	// make implementation of SatelliteImage lol
	// i'm gonna have to write my own decoder for this
	// ouch 
	public static void main(String[] args) throws IOException {
		File f = new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/himawari/HS_H08_20150707_0200_B01_FLDK_R10_S0101.DAT");
		NetcdfFile ncfile = NetcdfFile.open(f.getAbsolutePath());
		
		System.out.println(ncfile);
	
	}
}
