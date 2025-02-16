package com.chasearchive.radarImageCli.satellite.utilities;

import java.io.IOException;

import ucar.nc2.NetcdfFile;

public class ReadNcFile {
	public static void main(String[] args) throws IOException {
		NetcdfFile ncfile = NetcdfFile.open(
				"/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/satellite-image-generator-temp/sat_multiband.nc");

		System.out.println(ncfile);
	}
}
