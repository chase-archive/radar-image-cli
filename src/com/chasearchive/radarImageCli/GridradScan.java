package com.chasearchive.radarImageCli;

import java.io.File;
import java.io.IOException;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class GridradScan {
	float[] lat;
	float[] lon;
	float[][][] refl;
	float[][] refl1km;

	float dLat;
	float dLon;

	public static void main(String[] args) throws IOException {
		GridradScan gridrad = new GridradScan(new File(
				"/home/a-urq/Downloads/2024010100/CONUS/SeamlessHSR/MRMS_SeamlessHSR_00.00_20240101-005800.grib2.gz"));
	}

	public GridradScan(File file) throws IOException {
		NetcdfFile ncfile = NetcdfFile.open(file.getAbsolutePath());
		System.out.println(ncfile);

		Variable latVar = ncfile.findVariable("Latitude");
		Variable lonVar = ncfile.findVariable("Longitude");
		Variable ralaVar = ncfile.findVariable("Reflectivity");
		Variable indexVar = ncfile.findVariable("index");

		lat = readVariable1Dim(latVar);
		lon = readVariable1Dim(lonVar);

		for (int i = 0; i < lon.length; i++) {
			lon[i] -= 360;
		}

		float[] reflRaw = readVariable1Dim(ralaVar);
		float[] index = readVariable1Dim(indexVar);
		float[] reflUnpacked = new float[24 * 1201 * 2301];
		refl = new float[24][1201][2301];
		refl1km = new float[1201][2301];

		dLat = lat[1] - lat[0];
		dLon = lon[1] - lon[0];

		for (int i = 0; i < reflRaw.length; i++) {
			reflUnpacked[(int) index[i]] = reflRaw[i];
		}
		
		for (int i = 0; i < reflUnpacked.length; i++) {
			int i2 = i / 1201 / 2301;
			int j = (i / 2301) % 1201;
			int k = i % 2301;

			refl[i2][lat.length - 1 - j][k] = reflUnpacked[i];
		}

		for (int i = 0; i < 24; i++) {
			System.out.println(i + ":\t" + max(refl[i]));
		}
		refl1km = refl[0];
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

	private static float max(float[] arr) {
		float max = arr[0];

		for (int i = 1; i < arr.length; i++) {
			max = Float.max(arr[i], max);
		}

		return max;
	}

	private static float max(float[][] arr) {
		float max = arr[0][0];

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
					max = Float.max(arr[i][j], max);
			}
		}

		return max;
	}

	private static float max(float[][][] arr) {
		float max = arr[0][0][0];

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				for (int k = 0; k < arr[i][j].length; k++) {
					max = Float.max(arr[i][j][k], max);
				}
			}
		}

		return max;
	}
}
