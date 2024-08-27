package com.chasearchive.radarImageCli;

import static com.chasearchive.radarImageCli.RadarImageCli.logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class RadarScan {	
	private static PostProc reflPostProc = new PostProc() {
		@Override
		public float process(float data) {
			return (0.5f * (data) - 33);
		}
	};
	
	// velocity
	private static PostProc vlcyPostProc = new PostProc() {
		@Override
		public float process(float data) {
			return 0.5f * data - 64.5f;
		}
	};
	
//	// spectrum width
//	private static PostProc spwdPostProc = new PostProc() {
//		@Override
//		public float process(float data) {
//			return 0.5f * data - 64.5f;
//		}
//	};
//	
//	// differential reflectivity
//	private static PostProc drflPostProc = new PostProc() {
//		@Override
//		public float process(float data) {
//	//		return 0.05 * data - 8.0;
//			return 0.04714f * data - 8.0f;
//		}
//	};
//	
//	// correlation coefficient
//	private static PostProc crcfPostProc = new PostProc() {
//		@Override
//		public float process(float data) {
//			return (0.0033333334f * data + 0.20166667f);
//		}
//	};
//	
//	// differential phase
//	private static PostProc dphsPostProc = new PostProc() {
//		@Override
//		public float process(float data) {
//			return 0.35259685f * data - 0.7051937f;
//		}
//	};
	
	public double radarLat;
	public double radarLon;
	
	public double dr = 0.25; // km, make more adaptive later
	public double da = 0.5; // deg, same
	public double coneOfSilence = 1.875;
	
	private float[][][] reflectivity;
	private float[][][] radialVelocity;

	private float[] reflectivityTime; // millis after 00z UTC
	private float[] reflectivityElev; // degrees

	private float[] velocityTime; // millis after 00z UTC
	private float[] velocityElev; // degrees
	
	DateTime scanTime = null;

	@SuppressWarnings("deprecation")
	public RadarScan(File file) throws IOException {
		NetcdfFile ncfile = NetcdfFile.open(file.getAbsolutePath());
		
		logger.println(ncfile, DebugLoggerLevel.BRIEF);
		logger.println(Arrays.toString(ncfile.getGlobalAttributes().toArray()), DebugLoggerLevel.VERBOSE);
		
		String timestampStart = ncfile.findGlobalAttribute("time_coverage_start").getStringValue();
		logger.println(timestampStart, DebugLoggerLevel.BRIEF);

		radarLat = ncfile.findGlobalAttribute("StationLatitude").getNumericValue().doubleValue();
		radarLon = ncfile.findGlobalAttribute("StationLongitude").getNumericValue().doubleValue();

		logger.println(radarLat, DebugLoggerLevel.BRIEF);
		logger.println(radarLon, DebugLoggerLevel.BRIEF);
		
		scanTime = new DateTime(
				Integer.valueOf(timestampStart.substring(0, 4)),
				Integer.valueOf(timestampStart.substring(5, 7)),
				Integer.valueOf(timestampStart.substring(8, 10)),
				Integer.valueOf(timestampStart.substring(11, 13)),
				Integer.valueOf(timestampStart.substring(14, 16)),
				Integer.valueOf(timestampStart.substring(17, 19)),
				DateTimeZone.UTC
			);
		logger.println(scanTime, DebugLoggerLevel.BRIEF);
		
		Variable baseRefl = ncfile.findVariable("Reflectivity_HI");
		Variable baseReflAzi = ncfile.findVariable("azimuthR_HI");
		Variable baseReflTime = ncfile.findVariable("timeR_HI");
		Variable baseReflElev = ncfile.findVariable("elevationR_HI");

		Variable baseVlcy = ncfile.findVariable("RadialVelocity_HI");
		Variable baseVlcyAzi = ncfile.findVariable("azimuthV_HI");
		Variable baseVlcyTime = ncfile.findVariable("timeV_HI");
		Variable baseVlcyElev = ncfile.findVariable("elevationV_HI");

		reflectivity = readNexradData(baseRefl, baseReflAzi, reflPostProc, -1024, -32.5, -1);
		radialVelocity = readNexradData(baseVlcy, baseVlcyAzi, vlcyPostProc, -64.5, -64.0, -1);
		
		float[][] reflTimeRaw = varToArray2D(baseReflTime);
		reflectivityTime = new float[reflTimeRaw.length];
		for(int i = 0; i < reflectivityTime.length; i++) {
			reflectivityTime[i] = reflTimeRaw[i][0] / 1000.0f;
			
			reflectivityTime[i] -= reflTimeRaw[0][0] / 1000.0f;
		}
		
		float[][] reflElevRaw = varToArray2D(baseReflElev);
		reflectivityElev = new float[reflElevRaw.length];
		for(int i = 0; i < reflectivityElev.length; i++) {
			float avgElev = 0.0f;
			
			for(int j = 0; j < reflElevRaw[i].length; j++) {
				avgElev += reflElevRaw[i][j];
			}
			
			reflectivityElev[i] = avgElev / 720.0f;
		}
		
		float[][] vlcyTimeRaw = varToArray2D(baseVlcyTime);
		velocityTime = new float[vlcyTimeRaw.length];
		for(int i = 0; i < velocityTime.length; i++) {
			velocityTime[i] = vlcyTimeRaw[i][0] / 1000.0f;
			
			velocityTime[i] -= vlcyTimeRaw[0][0] / 1000.0f;
		}
		
		float[][] vlcyElevRaw = varToArray2D(baseVlcyElev);
		velocityElev = new float[vlcyElevRaw.length];
		for(int i = 0; i < velocityElev.length; i++) {
			float avgElev = 0.0f;
			
			for(int j = 0; j < vlcyElevRaw[i].length; j++) {
				avgElev += vlcyElevRaw[i][j];
			}
			
			velocityElev[i] = avgElev / 720.0f;
		}
	}

	private static float[][][] readNexradData(Variable rawData, Variable azimuths, PostProc proc, double ndValue,
			double rfValue, int maxAmtTilts) throws IOException {
		if(rawData == null) return new float[1][720][1832];
		
		int[] shape = rawData.getShape();
		Array _data = null;
		Array _azi = null;

//		System.out.println("maxamttilt check: " + shape[0] + "\t" + maxAmtTilts);

		if (maxAmtTilts != -1)
			shape[0] = Integer.min(shape[0], maxAmtTilts);
		
		if(azimuths == null) {
			System.err.println("readNexradData() - azimuths is null!");
			return new float[1][720][1832];
		}

		_data = rawData.read();
		_azi = azimuths.read();

		@SuppressWarnings("deprecation")
		boolean isDiffRefl = ("DifferentialReflectivity_HI".equals(rawData.getName()));

		if (isDiffRefl) {
//			System.out.println(Arrays.toString(shape));
		}

		double[][] azi = new double[shape[0]][shape[1]];
		for (int h = 0; h < _azi.getSize(); h++) {
			int i = h / (shape[1]);
			int j = h % shape[1];

			if (i >= shape[0])
				break;

			azi[i][j] = _azi.getDouble(h);
		}

		float[][][] data = new float[shape[0]][shape[1]][shape[2]];
//		System.out.print(data.length + "\t");
//		System.out.print(data[0].length + "\t");
//		System.out.println(data[0][0].length);
		for (int h = 0; h < _data.getSize(); h++) {
			int i = h / (shape[1] * shape[2]);
			int j = h / (shape[2]) % shape[1];
			int k = h % shape[2];

			if (i >= shape[0])
				break;

			if (isDiffRefl) {
				if (k % 2 == 1) {
					k /= 2;
				} else {
					k /= 2;
					k += shape[2] / 2;
				}
			}

			float record = proc.process(_data.getFloat(h));

//			int[] coords = { h, i, j, k };

			if (isDiffRefl) {
//				if(i == 0 && h % shape[2] < 6) {
//					System.out.printf("%25s", Arrays.toString(coords) + " " + record);
//				}
//				
//				if(i == 0 && k == 6) {
//					System.out.println();
//				}
			}

			if (record == ndValue) {
				data[i]
						[(int) Math.floor(shape[1]/360.0 * azi[i][j])]
								[k] = -1024;
			} else if (record == rfValue) {
				data[i][(int) Math.floor(shape[1]/360.0 * azi[i][j])][k] = -2048;
			} else {
				data[i][(int) Math.floor(shape[1]/360.0 * azi[i][j])][k] = record;
			}
		}

//		System.out.println(rawData);
//		System.out.println(rawData.getName());
//		System.out.println(Arrays.toString(shape));
//		System.out.println();

		return data;
	}

	private float[][] varToArray2D(Variable v) {
		if(v == null) return new float[0][0];
		
		try {
			Array arr = v.read();
			
			int[] shape = arr.getShape();
			
			float[][] ret = new float[shape[0]][shape[1]];
			
			for(int h = 0; h < arr.getSize(); h++) {
				ret[h / shape[1]][h % shape[1]] = arr.getFloat(h);
			}
			
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new float[0][0];
	}


	public float[][][] getReflectivity() {
		if (reflectivity == null)
			return new float[4][720][1];

		return reflectivity;
	}

	public float[][] getReflectivity(DateTime time, boolean allScans) {
		float secondTimeDifference = (time.getMillis() - scanTime.getMillis()) / 1000.0f;
		
		if(allScans) {
//			System.out.println("all-tilts");
			for(int i = 1; i < reflectivityTime.length; i++) {
				float reflTime = reflectivityTime[i];
				
				if(secondTimeDifference < reflTime) {
					return reflectivity[i - 1];
				}
			}
			
			return reflectivity[reflectivity.length - 1];
		} else {
			ArrayList<Integer> validScanIds = new ArrayList<>();

			validScanIds.add(0);
			for(int i = 1; i < reflectivityTime.length; i++) {
//				System.out.println(i + "\t" + (reflectivityElev[i] - reflectivityElev[i - 1]));
//				System.out.println(i + "\t" + (reflectivityElev[i]));
				if(Math.abs(reflectivityElev[i] - reflectivityElev[i - 1]) < 0.1) {
//					System.out.println(i + "\tnot added");
					continue;
				}
				
				if(reflectivityElev[i] > 0.6) {
//					System.out.println(i + "\tnot added");
					continue;
				}

//				System.out.println(i + "\t added");
				validScanIds.add(i);
			}
			
			for(int i = 0; i < validScanIds.size(); i++) {
//				System.out.println("valid scan id - " + validScanIds.get(i));
			}
			
			for(int i = 1; i < validScanIds.size(); i++) {
				float reflTime = reflectivityTime[validScanIds.get(i)];
				
				if(secondTimeDifference < reflTime) {
					return reflectivity[validScanIds.get(i - 1)];
				}
			}
			
			return reflectivity[validScanIds.get(validScanIds.size() - 1)];
		}
	}

	public float[][][] getRadialVelocity() {
		if (radialVelocity == null)
			return new float[4][720][1];

		return radialVelocity;
	}
	
	public float[][] getRadialVelocity(DateTime time, boolean allScans) {
		float secondTimeDifference = (time.getMillis() - scanTime.getMillis()) / 1000.0f;
		
		if(allScans) {
//			System.out.println("all-tilts");
			for(int i = 1; i < velocityTime.length; i++) {
				float vlcyTime = velocityTime[i];
				
				if(secondTimeDifference < vlcyTime) {
					return radialVelocity[i - 1];
				}
			}
			
			return radialVelocity[radialVelocity.length - 1];
		} else {
			ArrayList<Integer> validScanIds = new ArrayList<>();

			validScanIds.add(0);
			for(int i = 1; i < velocityTime.length; i++) {
//				System.out.println(i + "\t" + (velocityElev[i] - velocityElev[i - 1]));
//				System.out.println(i + "\t" + (velocityElev[i]));
				if(Math.abs(velocityElev[i] - velocityElev[i - 1]) < 0.1) {
//					System.out.println(i + "\tnot added");
					continue;
				}
				
				if(velocityElev[i] > 0.6) {
//					System.out.println(i + "\tnot added");
					continue;
				}

//				System.out.println(i + "\t added");
				validScanIds.add(i);
			}
			
			for(int i = 0; i < validScanIds.size(); i++) {
//				System.out.println("valid scan id - " + validScanIds.get(i));
			}
			
			for(int i = 1; i < validScanIds.size(); i++) {
				float vlcyTime = velocityTime[validScanIds.get(i)];
				
				if(secondTimeDifference < vlcyTime) {
					return radialVelocity[validScanIds.get(i - 1)];
				}
			}
			
			return radialVelocity[validScanIds.get(validScanIds.size() - 1)];
		}
	}
}

interface PostProc {
	float process(float data);
}
