package com.chasearchive.radarImageCli;

import java.io.File;
import java.io.IOException;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class MrmsComposite {
	float[] lat;
	float[] lon;
	float[][][][] reflAtLowestAltitude;
	
	float dLat;
	float dLon;
	
	public static void main(String[] args) throws IOException {
		MrmsComposite mrms = new MrmsComposite(new File("/home/a-urq/Downloads/2024010100/CONUS/SeamlessHSR/MRMS_SeamlessHSR_00.00_20240101-005800.grib2.gz"));
	}
	
	public MrmsComposite(File file) throws IOException {
		NetcdfFile ncfile = NetcdfFile.open(file.getAbsolutePath());
		System.out.println(ncfile);
		
		Variable latVar = ncfile.findVariable("lat");
		Variable lonVar = ncfile.findVariable("lon");
		Variable ralaVar = ncfile.findVariable("SeamlessHSR_altitude_above_msl");
		
		lat = readVariable1Dim(latVar);
		lon = readVariable1Dim(lonVar);
		reflAtLowestAltitude = readVariable4Dim(ralaVar);
		
		dLat = lat[1] - lat[0];
		dLon = lon[1] - lon[0];
		
		System.out.println(dLat);
		System.out.println(dLon);
	}

	private static float[] readVariable1Dim(Variable rawData) {
		int[] shape = rawData.getShape();
		Array _data = null;

		try {
			_data = rawData.read();
		} catch (IOException e) {
			e.printStackTrace();
			return new float[shape[0]];
		}

		float[] data = new float[shape[0]];
		// see if an alternate data-reading algorithm that avoids division and modulos
		// could be faster
		for (int i = 0; i < _data.getSize(); i++) {
			int t = i;

			float record = _data.getFloat(i);

			data[t] = record;
		}

		return data;
	}

	private static float[][][][] readVariable4Dim(Variable rawData) {
		int[] shape = rawData.getShape();
		Array _data = null;

		try {
			_data = rawData.read();
		} catch (IOException e) {
			e.printStackTrace();
			return new float[shape[0]][shape[1]][shape[2]][shape[3]];
		}

		float[][][][] data = new float[shape[0]][shape[1]][shape[2]][shape[3]];
		// see if an alternate data-reading algorithm that avoids division and modulos
		// could be faster
		for (int i = 0; i < _data.getSize(); i++) {
			int x = i % shape[3];
			int y = (i / shape[3]) % shape[2];
			int z = (i / (shape[3] * shape[2])) % shape[1];
			int t = (i / (shape[3] * shape[2] * shape[1])) % shape[0];

			float record = _data.getFloat(i);

			data[t][z][shape[2] - 1 - y][x] = record;
		}

		return data;
	}
}
