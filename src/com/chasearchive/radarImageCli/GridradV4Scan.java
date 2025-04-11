package com.chasearchive.radarImageCli;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class GridradV4Scan {
	float[] lat;
	float[] lon;
	float[][][] refl;
	float[][] refl1km;

	float dLat;
	float dLon;

	public GridradV4Scan(File file) throws IOException {
		NetcdfFile ncfile = NetcdfFile.open(file.getAbsolutePath());
//		System.out.println(ncfile);
//		System.exit(44);

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
		float[] reflUnpacked = new float[28 * lat.length * lon.length];
		refl = new float[28][lat.length][lon.length];
		refl1km = new float[lat.length][lon.length];

		dLat = lat[1] - lat[0];
		dLon = lon[1] - lon[0];
		
		for (int i = 0; i < reflUnpacked.length; i++) {
			reflUnpacked[i] = -1023;
		}

		for (int i = 0; i < reflRaw.length; i++) {
			reflUnpacked[(int) index[i]] = reflRaw[i];
		}
		
		for (int i = 0; i < reflUnpacked.length; i++) {
			int i2 = i / lat.length / lon.length;
			int j = (i / lon.length) % lat.length;
			int k = i % lon.length;

			refl[i2][lat.length - 1 - j][k] = reflUnpacked[i];
		}

		for (int i = 0; i < 28; i++) {
//			System.out.println(i + ":\t" + max(refl[i]));
		}
		
		for(int j = 0; j < refl[0].length; j++) {
			for(int k = 0; k < refl[0][j].length; k++) {
				if(refl[0][j][k] != -1023) {
					refl1km[j][k] = refl[0][j][k];
				}
			}
		}
		
		refl1km = refl[0];
		
		// reflectivity at lowest altitude construction 
		for(int i = 0; i < refl.length; i++) {
			for(int j = 0; j < refl[i].length; j++) {
				for(int k = 0; k < refl[i][j].length; k++) {
					if(refl1km[j][k] == -1023) {
						refl1km[j][k] = refl[i][j][k];
					}
				}
			}
		}

		for(int j = 0; j < refl[0].length; j++) {
			for(int k = 1; k < refl[0][j].length - 1; k++) {
				if(refl[0][j][k] == -1023
						&& refl[0][j][k - 1] != -1023
						&& refl[0][j][k + 1] != -1023) {
					refl1km[j][k] = refl1km[j][k - 1];
				}
			}
		}
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
