package com.chasearchive.radarImageCli.satellite;

import java.awt.Color;

public class GeocolorProcessing {
	// from GOES true color recipe
	// https://unidata.github.io/python-gallery/examples/mapping_GOES16_TrueColor.html#sphx-glr-download-examples-mapping-goes16-truecolor-py

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

//	private static float[][] normalize(float[][] arr, float newMin, float newMax) {
//		float[][] normArr = new float[arr.length][arr[0].length];
//
//		float max = max2(arr);
//		float min = min(arr);
//
//		for (int i = 0; i < arr.length; i++) {
//			for (int j = 0; j < arr[i].length; j++) {
//				normArr[i][j] = linScale(min, max, newMin, newMax, arr[i][j]);
//
//				// overshoot correction
//				if (normArr[i][j] > newMax) {
//					normArr[i][j] = newMax;
//				}
//			}
//		}
//
//		return normArr;
//	}

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
