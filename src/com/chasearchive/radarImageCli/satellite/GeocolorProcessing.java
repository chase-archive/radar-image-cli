package com.chasearchive.radarImageCli.satellite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.ameliaWx.weatherUtils.WeatherUtils;

public class GeocolorProcessing {
	// from GOES true color recipe
	// https://unidata.github.io/python-gallery/examples/mapping_GOES16_TrueColor.html#sphx-glr-download-examples-mapping-goes16-truecolor-py

	public static void main(String[] args) throws IOException {
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 / 1000000.0, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 / 100000.0, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 / 10000.0, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 / 1000.0, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 / 100.0, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 / 10.0, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 / 1, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 * 10, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 * 100, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 * 1000, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 * 10000, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 * 100000, 10.33 / 1000000));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavelength(77 * 1000000, 10.33 / 1000000));
//		System.out.println(10);
//
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 / 1000000.0, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 / 100000.0, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 / 10000.0, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 / 1000.0, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 / 100.0, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 / 10.0, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 / 1, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 * 10, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 * 100, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 * 1000, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 * 10000, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 * 100000, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		System.out.println((float) WeatherUtils.brightnessTemperatureFromWavenumber(77 * 1000000, WeatherUtils.wavelengthToWavenumber(0.0000103)));
//		
//		System.exit(0);
		
		GoesImage band1 = GoesImage.loadFromFile(new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C01_G16_s20250372301171_e20250372303544_c20250372303599.nc"));
		GoesImage band2 = GoesImage.loadFromFile(new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C02_G16_s20250372301171_e20250372303544_c20250372303571.nc"));
		GoesImage band3 = GoesImage.loadFromFile(new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C03_G16_s20250372301171_e20250372303544_c20250372303583.nc"));
		GoesImage band7 = GoesImage.loadFromFile(new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C07_G16_s20250372301171_e20250372303556_c20250372304028.nc"));
		GoesImage band13 = GoesImage.loadFromFile(new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C13_G16_s20250372301171_e20250372303556_c20250372304042.nc"));
		
//		NetcdfFile band1File = NetcdfFile.open("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C01_G16_s20250372301171_e20250372303544_c20250372303599.nc");
//		NetcdfFile band2File = NetcdfFile.open("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C02_G16_s20250372301171_e20250372303544_c20250372303571.nc");
//		NetcdfFile band3File = NetcdfFile.open("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C03_G16_s20250372301171_e20250372303544_c20250372303583.nc");
//		
//		System.out.println(band3File);

		Color[][] comp = createComposite(band1, band2, band3, band7, band13);
//		createIRGoes(band7, band13);
//		Color[][] comp = createTrueColorGoes(band1, band2, band3);

		BufferedImage geocolorImg = new BufferedImage(comp.length, comp[0].length,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = geocolorImg.createGraphics();

		for (int i = 0; i < geocolorImg.getWidth(); i++) {
			for (int j = 0; j < geocolorImg.getHeight(); j++) {
				g.setColor(comp[i][j]);
				g.fillRect(i, j, 1, 1);
			}
		}

		ImageIO.write(geocolorImg, "PNG", new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/goes.png"));
	}
	
	// temu geocolor
	public static Color[][] createComposite(GoesMultibandImage goes) {
		Color[][] trueColor = createTrueColorGoes(goes);
		Color[][] irColor = createIRGoes(goes);

		Color[][] goesComposite = new Color[trueColor.length][trueColor[0].length];

		for (int i = 0; i < goesComposite.length; i++) {
			for (int j = 0; j < goesComposite[0].length; j++) {
				goesComposite[i][j] = maxTristims(trueColor[i][j], irColor[i][j]);
			}
		}

		return goesComposite;
	}
	
	public static Color[][] createTrueColorGoes(GoesMultibandImage goes) {
		float[][] band1Rad = goes.field("band_1").array2D();
		float[][] band2Rad = goes.field("band_2").array2D();
		float[][] band3Rad = goes.field("band_3").array2D();

		float[][] band1Clip = clip(band1Rad, 0, 1);
		float[][] band2Clip = clip(band2Rad, 0, 1);
		float[][] band3Clip = clip(band3Rad, 0, 1);

		final float GAMMA = 2.2f;

		float[][] band1NormG = gammaCorrect(band1Clip, GAMMA);
		float[][] band2NormG = gammaCorrect(band2Clip, GAMMA);
		float[][] band3NormG = gammaCorrect(band3Clip, GAMMA);

		float[][] syntheticGreen = new float[band3Rad.length][band3Rad[0].length];

		for (int i = 0; i < syntheticGreen.length; i++) {
			for (int j = 0; j < syntheticGreen[i].length; j++) {
				// calculate the "true" green
				syntheticGreen[i][j] = 0.325f * band2NormG[i][j] + 0.35f * band3NormG[i][j] + 0.325f * band1NormG[i][j];
			}
		}

		syntheticGreen = clip(syntheticGreen, 0, 1);

		float[][] red = band2NormG;
		float[][] green = syntheticGreen;
		float[][] blue = band1NormG;

		boolean hiResRed = false;
		if (red.length >= 2 * blue.length) {
			hiResRed = true;
		}

		Color[][] goesComposite = new Color[green[0].length][green.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
			for (int j = 0; j < goesComposite.length; j++) {
				int r;
				if (hiResRed) {
					r = (int) (255 * red[2 * i][2 * j]);
				} else {
					r = (int) (255 * red[i][j]);
				}

				int gr = (int) (255 * green[i][j]);
				int b = (int) (255 * blue[i][j]);

				Color c = new Color(r, gr, b);

				goesComposite[j][i] = contrast(c, 48);
			}
		}

		return goesComposite;
	}

	public static Color[][] createIRGoes(GoesMultibandImage goes) {
		float[][] band7Temp = goes.field("band_7").array2D();
		float[][] band13Temp = goes.field("band_13").array2D();

		float[][] band13Clip = clip(band13Temp, 90, 273);
		float[][] band13Norm = clip(invNormalize(band13Clip, 0, 350), 0, 255);

		Color[][] goesComposite = new Color[band13Temp[0].length][band13Temp.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
			for (int j = 0; j < goesComposite.length; j++) {
				float fog = band13Temp[i][j] - band7Temp[i][j];

				float fogBlue = clip(linScale(0, 5, 0, 200, fog), 0, 200);

				Color fogColor = new Color((int) (0.5 * fogBlue), (int) (0.75 * fogBlue), (int) (1.0 * fogBlue));
				Color band13Color = new Color((int) band13Norm[i][j], (int) band13Norm[i][j],
						(int) Double.max(band13Norm[i][j], fogBlue));

				goesComposite[j][i] = maxTristims(fogColor, band13Color);

				if (band13Temp[i][j] == -1024) {
					goesComposite[j][i] = Color.BLACK;
				}
			}
		}

		return goesComposite;
	}

	// temu geocolor
	public static Color[][] createComposite(GoesImage band1, GoesImage band2, GoesImage band3, GoesImage band7, GoesImage band13) {
		Color[][] trueColor = createTrueColorGoes(band1, band2, band3);
		Color[][] irColor = createIRGoes(band7, band13);

		Color[][] goesComposite = new Color[trueColor.length][trueColor[0].length];

		for (int i = 0; i < goesComposite.length; i++) {
			for (int j = 0; j < goesComposite[0].length; j++) {
				goesComposite[i][j] = maxTristims(trueColor[i][j], irColor[i / 4][j / 4]);
			}
		}

		return goesComposite;
	}
	
	public static Color[][] createTrueColorGoes(GoesImage band1, GoesImage band2, GoesImage band3) {
		float[][] band1Rad = band1.field("rad").array2D();
		float[][] band2Rad = band2.field("rad").array2D();
		float[][] band3Rad = band3.field("rad").array2D();
		
		// VERY IMPORTANT!! normalize radiances to the correct specific ranges

		float[][] band1Clip = clip(band1Rad, 0, 1000);
		float[][] band2Clip = clip(band2Rad, 0, 1000);
		float[][] band3Clip = clip(band3Rad, 0, 1000);
		
		band1Clip = normalize(band1Rad, 0, 1);
		band2Clip = normalize(band2Rad, 0, 1);
		band3Clip = normalize(band3Rad, 0, 1);

		final float GAMMA = 2.2f;

		float[][] band1NormG = gammaCorrect(band1Clip, GAMMA);
		float[][] band2NormG = gammaCorrect(band2Clip, GAMMA);
		float[][] band3NormG = gammaCorrect(band3Clip, GAMMA);

		float[][] syntheticGreen = new float[band2Rad.length][band2Rad[0].length];

		for (int i = 0; i < syntheticGreen.length; i++) {
			for (int j = 0; j < syntheticGreen[i].length; j++) {
				// calculate the "true" green
				syntheticGreen[i][j] = 0.325f * band2NormG[i][j] + 0.35f * band3NormG[i / 2][j / 2] + 0.325f * band1NormG[i / 2][j / 2];
			}
		}

		syntheticGreen = clip(syntheticGreen, 0, 1);

		float[][] red = band2NormG;
		float[][] green = syntheticGreen;
		float[][] blue = band1NormG;

		Color[][] goesComposite = new Color[red[0].length][red.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
			if(i % 500 == 0) System.out.println("Goes True-Color Composite " + (100 * (float) i/goesComposite[0].length) + "% complete");
			
			for (int j = 0; j < goesComposite.length; j++) {
				int r = (int) (255 * red[i][j]);

				int gr = (int) (255 * green[i][j]);
				int b = (int) (255 * blue[i / 2][j / 2]);

				Color c = new Color(r, gr, b);

				goesComposite[j][i] = contrast(c, 48);
			}
		}

		return goesComposite;
	}

	public static Color[][] createIRGoes(GoesImage band7, GoesImage band13) {
		float[][] band7Rad = band7.field("rad").array2D();
		float[][] band13Rad = band13.field("rad").array2D();
		
		float[][] band7Temp = new float[band7Rad.length][band7Rad[0].length];
		float[][] band13Temp = new float[band13Rad.length][band13Rad[0].length];
		float band7wavelength = band7.dataFromField("wavelength");
		float band13wavelength = band13.dataFromField("wavelength");

		System.out.println("band 7: " + band7wavelength + " um");
		System.out.println("band 13: " + band13wavelength + " um");
		
		// VERY IMPORTANT!! figure out radiance -> brightness temperature conversion 
		for(int i = 0; i < band13Rad.length; i++) {
			for(int j = 0; j < band13Rad[i].length; j++) {
				if(band13Rad[i][j] == -1024) {
					band7Temp[i][j] = -1024;
					band13Temp[i][j] = -1024;
				} else {
//					band7Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavelength(band7Rad[i][j] * 100000, band7wavelength / 1000000);
//					band13Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavelength(band13Rad[i][j] * 100000, band13wavelength / 1000000);
					band7Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavenumber(band7Rad[i][j] / 100000.0, WeatherUtils.wavelengthToWavenumber(band7wavelength / 1000000.0));
					band13Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavenumber(band13Rad[i][j] / 100000.0, WeatherUtils.wavelengthToWavenumber(band13wavelength / 1000000.0));
				}
			}
		}
//		System.out.println("band 13: " + band13Rad[400][1500] + " mW m^-2 sr^-1 (cm^-1)^-1");
//		System.out.println("band 13: " + band13Temp[400][1500] + " K");
//		System.out.println("band 7: " + band7Rad[400][1500] + " mW m^-2 sr^-1 (cm^-1)^-1");
//		System.out.println("band 7: " + band7Temp[400][1500] + " K");

		float[][] band13Clip = clip(band13Temp, 90, 273);
		float[][] band13Norm = clip(invNormalize(band13Clip, 0, 350), 0, 255);

		Color[][] goesComposite = new Color[band13Temp[0].length][band13Temp.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
			for (int j = 0; j < goesComposite.length; j++) {
				float fog = band13Temp[i][j] - band7Temp[i][j];

				float fogBlue = clip(linScale(0, 5, 0, 150, fog), 0, 150);

				Color fogColor = new Color((int) (0.5 * fogBlue), (int) (0.75 * fogBlue), (int) (1.0 * fogBlue));
//				Color band13Color = new Color((int) band13Norm[i][j], (int) band13Norm[i][j],
//						(int) Double.max(band13Norm[i][j], fogBlue));
//
//				goesComposite[j][i] = maxTristims(fogColor, band13Color);

				Color band13Color = new Color((int) band13Norm[i][j], (int) band13Norm[i][j],
						(int) Double.max(band13Norm[i][j], fogBlue));

				goesComposite[j][i] = maxTristims(band13Color, fogColor);

				if (band13Temp[i][j] == -1024) {
					goesComposite[j][i] = Color.BLACK;
				}
			}
		}

		return goesComposite;
	}

	private static Color maxTristims(Color a, Color b) {
		int rA = a.getRed();
		int gA = a.getGreen();
		int bA = a.getBlue();

		int rB = b.getRed();
		int gB = b.getGreen();
		int bB = b.getBlue();

		Color comp = new Color(Integer.max(rA, rB), Integer.max(gA, gB), Integer.max(bA, bB));

		return comp;
	}

	private static Color contrast(Color c, float contrast) {
		float factor = 259 * (contrast + 255) / (255 * (259 - contrast));

		int r = (int) (factor * (c.getRed() - 128) + 128);
		int g = (int) (factor * (c.getGreen() - 128) + 128);
		int b = (int) (factor * (c.getBlue() - 128) + 128);

		r = clip(r, 0, 255);
		g = clip(g, 0, 255);
		b = clip(b, 0, 255);

		return new Color(r, g, b);
	}

	private static int clip(int val, int min, int max) {
		if (val < min) {
			return min;
		} else if (val > max) {
			return max;
		} else if (val == -1024) {
			return -1024;
		} else {
			return val;
		}
	}

	private static float clip(float val, float min, float max) {
		if (val < min) {
			return min;
		} else if (val > max) {
			return max;
		} else if (val == -1024) {
			return -1024;
		} else {
			return val;
		}
	}

	private static float[][] normalize(float[][] arr, float newMin, float newMax) {
		float[][] normArr = new float[arr.length][arr[0].length];

		float max = max2(arr);
		float min = min(arr);

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				normArr[i][j] = linScale(min, max, newMin, newMax, arr[i][j]);

				// overshoot correction
				if (normArr[i][j] > newMax) {
					normArr[i][j] = newMax;
				}
			}
		}

		return normArr;
	}

	private static float[][] invNormalize(float[][] arr, float newMin, float newMax) {
		float[][] normArr = new float[arr.length][arr[0].length];

		float max = max2(arr);
		float min = min(arr);

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				normArr[i][j] = linScale(min, max, newMax, newMin, arr[i][j]);

				// overshoot correction
				if (normArr[i][j] > newMax) {
					normArr[i][j] = newMax;
				}
			}
		}

		return normArr;
	}

	private static float[][] gammaCorrect(float[][] arr, float gamma) {
		float[][] gammaCorr = new float[arr.length][arr[0].length];

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				gammaCorr[i][j] = (float) Math.pow(arr[i][j], 1 / gamma);
			}
		}

		return gammaCorr;
	}

	private static float[][] clip(float[][] arr, float min, float max) {
		float[][] clipped = new float[arr.length][arr[0].length];

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				clipped[i][j] = clip(arr[i][j], min, max);
			}
		}

		return clipped;
	}

	private static float min(float[][] arr) {
		float min = Float.MAX_VALUE;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				if (arr[i][j] != -1024) {
					min = Float.min(arr[i][j], min);
				}
			}
		}

		return min;
	}

	// gets the second highest value
	private static float max2(float[][] arr) {
		float maxO = max(arr);

		float max = -Float.MAX_VALUE;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				if (arr[i][j] != maxO) {
					max = Float.max(arr[i][j], max);
				}
			}
		}

		return max;
	}

	private static float max(float[][] arr) {
		float max = -Float.MAX_VALUE;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				max = Float.max(arr[i][j], max);
			}
		}

		return max;
	}

	private static float linScale(float preMin, float preMax, float postMin, float postMax, float value) {
		float slope = (postMax - postMin) / (preMax - preMin);

		return slope * (value - preMin) + postMin;
	}
}
