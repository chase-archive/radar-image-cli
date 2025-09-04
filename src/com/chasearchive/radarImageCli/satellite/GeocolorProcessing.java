package com.chasearchive.radarImageCli.satellite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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

		GoesImage band1 = GoesImage.loadFromFile(new File(
				"/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C01_G16_s20250372301171_e20250372303544_c20250372303599.nc"));
		GoesImage band2 = GoesImage.loadFromFile(new File(
				"/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C02_G16_s20250372301171_e20250372303544_c20250372303571.nc"));
		GoesImage band3 = GoesImage.loadFromFile(new File(
				"/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C03_G16_s20250372301171_e20250372303544_c20250372303583.nc"));
		GoesImage band7 = GoesImage.loadFromFile(new File(
				"/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C07_G16_s20250372301171_e20250372303556_c20250372304028.nc"));
		GoesImage band13 = GoesImage.loadFromFile(new File(
				"/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C13_G16_s20250372301171_e20250372303556_c20250372304042.nc"));

//		NetcdfFile band1File = NetcdfFile.open("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C01_G16_s20250372301171_e20250372303544_c20250372303599.nc");
//		NetcdfFile band2File = NetcdfFile.open("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C02_G16_s20250372301171_e20250372303544_c20250372303571.nc");
//		NetcdfFile band3File = NetcdfFile.open("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C03_G16_s20250372301171_e20250372303544_c20250372303583.nc");
//		
//		System.out.println(band3File);

		Color[][] comp = createComposite(band1, band2, band3, band7, band13, GeostationaryProjection.GOES_EAST,
				new DateTime(2025, 2, 6, 23, 01, 17, DateTimeZone.UTC));
//		createIRGoes(band7, band13);
//		Color[][] comp = createTrueColorGoes(band1, band2, band3);

		BufferedImage geocolorImg = new BufferedImage(comp.length, comp[0].length, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = geocolorImg.createGraphics();

		for (int i = 0; i < geocolorImg.getWidth(); i++) {
			for (int j = 0; j < geocolorImg.getHeight(); j++) {
				g.setColor(comp[i][j]);
				g.fillRect(i, j, 1, 1);
			}
		}

		ImageIO.write(geocolorImg, "PNG",
				new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/goes.png"));
	}

	// temu geocolor
	public static Color[][] createComposite(GoesMultibandImage goes, GeostationaryProjection satProj) {
		int[] band2Shape = goes.field("band_2").getBundledField("rad").getShape();

		final int CHUNK_SIZE = 100;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) band2Shape[1] / CHUNK_SIZE)][(int) Math
				.ceil((double) band2Shape[0] / CHUNK_SIZE)];

		for (int i = 0; i < renderChunk.length; i++) {
			for (int j = 0; j < renderChunk[i].length; j++) {
				renderChunk[i][j] = true;
			}
		}

		return createComposite(goes, satProj, renderChunk, CHUNK_SIZE);
	}

	// temu geocolor
	public static Color[][] createComposite(GoesMultibandImage goes, GeostationaryProjection satProj,
			boolean[][] renderChunks, int chunkSize) {
		String timeStart = goes.field("time_start").getAnnotation();

		int timeStartYear = Integer.valueOf(timeStart.substring(0, 4));
		int timeStartMonth = Integer.valueOf(timeStart.substring(5, 7));
		int timeStartDay = Integer.valueOf(timeStart.substring(8, 10));
		int timeStartHour = Integer.valueOf(timeStart.substring(11, 13));
		int timeStartMinute = Integer.valueOf(timeStart.substring(14, 16));
		int timeStartSecond = Integer.valueOf(timeStart.substring(17, 19));

		DateTime dt = new DateTime(timeStartYear, timeStartMonth, timeStartDay, timeStartHour, timeStartMinute,
				timeStartSecond, DateTimeZone.UTC);
		GeoCoord[][] latLon = createLatLonMatrix(goes, satProj, renderChunks, chunkSize);
		float[][] solarAlt = createSolarAltitudeMatrix(latLon, dt, renderChunks, chunkSize);

//		float latMin = Float.MAX_VALUE;
//		float latMax = -Float.MAX_VALUE;
//		float lonMin = Float.MAX_VALUE;
//		float lonMax = -Float.MAX_VALUE;
//		
//		for(int i = 0; i < latLon.length; i++) {
//			for(int j = 0; j < latLon[i].length; j++) {
//				float lat = latLon[i][j].getLat();
//				float lon = latLon[i][j].getLon();
//				
//				if(lat < latMin) latMin = lat;
//				if(lat > latMax) latMax = lat;
//				if(lon < lonMin) lonMin = lon;
//				if(lon > lonMax) lonMax = lon;
//			}
//		}
//		
//		System.out.println(latMin + "\t" + latMax);
//		System.out.println(lonMin + "\t" + lonMax);
//		System.out.println(min(solarAlt) + "\t" + max(solarAlt));
//		System.exit(44);

		Color[][] trueColor = createTrueColorGoes(goes, latLon, dt, renderChunks, chunkSize);
		Color[][] irColor = createIRGoes(goes, renderChunks, chunkSize);

		Color[][] goesComposite = new Color[trueColor.length][trueColor[0].length];

		for (int i = 0; i < goesComposite.length; i++) {
			for (int j = 0; j < goesComposite[0].length; j++) {
				if (renderChunks[i / chunkSize][j / chunkSize]) {
					float blendFactor = (solarAlt[j][i]) / TERMINATOR_WIDTH;
					if (blendFactor < 0) {
						blendFactor = 0;
					} else if (blendFactor > 1) {
						blendFactor = 1;
					}

//					goesComposite[i][j] = trueColor[i][j];
//					goesComposite[j][i] = maxTristims(trueColor[i][j], irColor[i][j]);

					goesComposite[i][j] = blendTristims(trueColor[i][j], irColor[i][j], blendFactor);
				}
			}
		}

		return goesComposite;
	}

	public static Color[][] createTrueColorGoes(GoesMultibandImage goes, GeoCoord[][] latLon, DateTime dt,
			boolean[][] renderChunks, int chunkSize) {
		float[][] solarMult = createSolarMultiplierMatrix(latLon, dt, renderChunks, chunkSize);

		float[][] band1Rad = goes.field("band_1").array2D();
		float[][] band2Rad = goes.field("band_2").array2D();
		float[][] band3Rad = goes.field("band_3").array2D();

//		System.out.println(goes.field("band_1").getBundledField("wavelength"));

		for (int i = 0; i < band2Rad.length; i++) {
			for (int j = 0; j < band2Rad[i].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					if (renderChunks[j / chunkSize][i / chunkSize]) {
						float mult = 0.9f * (float) Math.sqrt(solarMult[i][j]);

						if (!Double.isNaN(mult)) {
							band1Rad[i][j] = band1Rad[i][j] * mult;
							band3Rad[i][j] = band3Rad[i][j] * mult;
							band2Rad[i][j] = band2Rad[i][j] * mult;
						}
					}
				}
			}
		}
//		System.exit(44);

		// VERY IMPORTANT!! normalize radiances to the correct specific ranges
		float[][] band1Clip = new float[band1Rad.length][band1Rad[0].length];
		float[][] band2Clip = new float[band2Rad.length][band2Rad[0].length];
		float[][] band3Clip = new float[band3Rad.length][band3Rad[0].length];
		for (int i = 0; i < band2Clip.length; i++) {
			for (int j = 0; j < band2Clip[i].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					band1Clip[i][j] = clip(band1Rad[i][j], 0, 1);
					band3Clip[i][j] = clip(band3Rad[i][j], 0, 1);
					band2Clip[i][j] = clip(band2Rad[i][j], 0, 1);
				}
			}
		}

//		band1Clip = normalize(band1Rad, 0, 1);
//		band2Clip = normalize(band2Rad, 0, 1);
//		band3Clip = normalize(band3Rad, 0, 1);

		final float GAMMA = 2.2f;

		float[][] band1NormG = new float[band1Clip.length][band1Clip[0].length];
		float[][] band2NormG = new float[band2Clip.length][band2Clip[0].length];
		float[][] band3NormG = new float[band3Clip.length][band3Clip[0].length];
		for (int i = 0; i < band2Clip.length; i++) {
			for (int j = 0; j < band2Clip[i].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					band1NormG[i][j] = gammaCorrect(band1Clip[i][j], GAMMA);
					band3NormG[i][j] = gammaCorrect(band3Clip[i][j], GAMMA);
					band2NormG[i][j] = gammaCorrect(band2Clip[i][j], GAMMA);
				}
			}
		}

		float[][] syntheticGreen = new float[band2Rad.length][band2Rad[0].length];

		for (int i = 0; i < syntheticGreen.length; i++) {
			for (int j = 0; j < syntheticGreen[i].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					// calculate the "true" green
					syntheticGreen[i][j] = clip(
							0.375f * band2NormG[i][j] + 0.25f * band3NormG[i][j] + 0.375f * band1NormG[i][j], 0, 1);
				}
			}
		}

		float[][] red = band2NormG;
		float[][] green = syntheticGreen;
		float[][] blue = band1NormG;

		Color[][] goesComposite = new Color[green[0].length][green.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
			for (int j = 0; j < goesComposite.length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					int r = (int) (255 * red[i][j]);
					int gr = (int) (255 * green[i][j]);
					int b = (int) (255 * blue[i][j]);

					Color c = new Color(r, gr, b);

					goesComposite[j][i] = contrast(c, 48);
				}
			}
		}

		return goesComposite;
	}

	public static Color[][] createIRGoes(GoesMultibandImage goes, boolean[][] renderChunks, int chunkSize) {
		float[][] band7Temp = goes.field("band_7").array2D();
		float[][] band13Temp = goes.field("band_13").array2D();

		float[][] band13Clip = clip(band13Temp, 90, 273);
		float[][] band13Norm = clip(invNormalize(band13Clip, 0, 500), 0, 255);

		Color[][] goesComposite = new Color[band13Temp[0].length][band13Temp.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
			for (int j = 0; j < goesComposite.length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
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
		}

		return goesComposite;
	}

	// temu geocolor (in retrospect i actually did a pretty good job with this one)
	public static Color[][] createComposite(GoesImage band1, GoesImage band2, GoesImage band3, GoesImage band7,
			GoesImage band13, GeostationaryProjection satProj, DateTime dt) {
		int[] band2Shape = band2.field("rad").getShape();

		final int CHUNK_SIZE = 100;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) band2Shape[1] / CHUNK_SIZE)][(int) Math
				.ceil((double) band2Shape[0] / CHUNK_SIZE)];

		for (int i = 0; i < renderChunk.length; i++) {
			for (int j = 0; j < renderChunk[i].length; j++) {
				renderChunk[i][j] = true;
			}
		}

		return createComposite(band1, band2, band3, band7, band13, satProj, dt, renderChunk, CHUNK_SIZE);
	}

	// temu geocolor
	private static final float TERMINATOR_WIDTH = 2.0f; // degrees of arc

	public static Color[][] createComposite(GoesImage band1, GoesImage band2, GoesImage band3, GoesImage band7,
			GoesImage band13, GeostationaryProjection satProj, DateTime dt, boolean[][] renderChunks, int chunkSize) {
		GeoCoord[][] latLon = createLatLonMatrix(band2, satProj, renderChunks, chunkSize);
		float[][] solarAlt = createSolarAltitudeMatrix(latLon, dt, renderChunks, chunkSize);

		Color[][] trueColor = createTrueColorGoes(band1, band2, band3, latLon, dt, renderChunks, chunkSize);
		Color[][] irColor = createIRGoes(band7, band13, renderChunks, chunkSize);

		Color[][] goesComposite = new Color[trueColor.length][trueColor[0].length];

		for (int i = 0; i < goesComposite.length; i++) {
			for (int j = 0; j < goesComposite[0].length; j++) {
				if (renderChunks[i / chunkSize][j / chunkSize]) {
					float blendFactor = (solarAlt[j][i]) / TERMINATOR_WIDTH;
					if (blendFactor < 0) {
						blendFactor = 0;
					} else if (blendFactor > 1) {
						blendFactor = 1;
					}

					goesComposite[i][j] = blendTristims(trueColor[i][j], irColor[i / 4][j / 4], blendFactor);
				}
			}
		}

		return goesComposite;
	}

	public static Color[][] createTrueColorGoes(GoesImage band1, GoesImage band2, GoesImage band3, GeoCoord[][] latLon,
			DateTime dt) {
		int[] band2Shape = band2.field("rad").getShape();

		final int CHUNK_SIZE = 100;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) band2Shape[0] / CHUNK_SIZE)][(int) Math
				.ceil((double) band2Shape[1] / CHUNK_SIZE)];

		for (int i = 0; i < renderChunk.length; i++) {
			for (int j = 0; j < renderChunk[i].length; j++) {
				renderChunk[i][j] = true;
			}
		}

		return createTrueColorGoes(band1, band2, band3, latLon, dt, renderChunk, CHUNK_SIZE);
	}

	// found by trial and error
	private static final float WHITE_POINT = 1000.0f;
	private static final float WHITE_BALANCE_RED = 0.932f;
	private static final float WHITE_BALANCE_GREEN = 1.383f;
	private static final float WHITE_BALANCE_BLUE = 0.785f;

	public static Color[][] createTrueColorGoes(GoesImage band1, GoesImage band2, GoesImage band3, GeoCoord[][] latLon,
			DateTime dt, boolean[][] renderChunks, int chunkSize) {
		float[][] solarMult = createSolarMultiplierMatrix(latLon, dt, renderChunks, chunkSize);

		float[][] band1Rad = band1.field("rad").array2D();
		float[][] band2Rad = band2.field("rad").array2D();
		float[][] band3Rad = band3.field("rad").array2D();

		for (int i = 0; i < band2Rad.length; i++) {
			for (int j = 0; j < band2Rad[i].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					if (renderChunks[j / chunkSize][i / chunkSize]) {
						float mult = solarMult[i][j];

						if (!Double.isNaN(mult)) {
							if (i % 2 == 0 && j % 2 == 0) {
								band1Rad[i / 2][j / 2] = band1Rad[i / 2][j / 2] * mult;
								band3Rad[i / 2][j / 2] = band3Rad[i / 2][j / 2] * mult;
							}
							band2Rad[i][j] = band2Rad[i][j] * mult;
						}
					}
				}
			}
		}

		// VERY IMPORTANT!! normalize and color-balance radiances to the correct
		// specific ranges
		float[][] band1Clip = new float[band1Rad.length][band1Rad[0].length];
		float[][] band2Clip = new float[band2Rad.length][band2Rad[0].length];
		float[][] band3Clip = new float[band3Rad.length][band3Rad[0].length];
		for (int i = 0; i < band2Clip.length; i++) {
			for (int j = 0; j < band2Clip[i].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					if (i % 2 == 0 && j % 2 == 0) {
						band1Clip[i / 2][j / 2] = clip(band1Rad[i / 2][j / 2] / (WHITE_POINT / WHITE_BALANCE_BLUE), 0,
								1);
						band3Clip[i / 2][j / 2] = clip(band3Rad[i / 2][j / 2] / (WHITE_POINT / WHITE_BALANCE_GREEN), 0,
								1);
					}
					band2Clip[i][j] = clip(band2Rad[i][j] / (WHITE_POINT / WHITE_BALANCE_RED), 0, 1);
				}
			}
		}

//		System.out.println(max2(band1Rad));
//		System.out.println(max2(band2Rad));
//		System.out.println(max2(band3Rad));

//		band1Clip = normalize(band1Rad, 0, 1);
//		band2Clip = normalize(band2Rad, 0, 1);
//		band3Clip = normalize(band3Rad, 0, 1);

		final float GAMMA = 2.2f;

		float[][] band1NormG = new float[band1Clip.length][band1Clip[0].length];
		float[][] band2NormG = new float[band2Clip.length][band2Clip[0].length];
		float[][] band3NormG = new float[band3Clip.length][band3Clip[0].length];
		for (int i = 0; i < band2Clip.length; i++) {
			for (int j = 0; j < band2Clip[i].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					if (i % 2 == 0 && j % 2 == 0) {
						band1NormG[i / 2][j / 2] = gammaCorrect(band1Clip[i / 2][j / 2], GAMMA);
						band3NormG[i / 2][j / 2] = gammaCorrect(band3Clip[i / 2][j / 2], GAMMA);
					}
					band2NormG[i][j] = gammaCorrect(band2Clip[i][j], GAMMA);
				}
			}
		}

		float[][] syntheticGreen = new float[band2Rad.length][band2Rad[0].length];

		for (int i = 0; i < syntheticGreen.length; i++) {
			for (int j = 0; j < syntheticGreen[i].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					// calculate the "true" green
					syntheticGreen[i][j] = clip(0.375f * band2NormG[i][j] + 0.25f * band3NormG[i / 2][j / 2]
							+ 0.375f * band1NormG[i / 2][j / 2], 0, 1);
				}
			}
		}

		float[][] red = band2NormG;
		float[][] green = syntheticGreen;
		float[][] blue = band1NormG;

		Color[][] goesComposite = new Color[red[0].length][red.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
//			if(i % 500 == 0) System.out.println("Goes True-Color Composite " + (100 * (float) i/goesComposite[0].length) + "% complete");

			for (int j = 0; j < goesComposite.length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					int r = (int) (255 * red[i][j]);

					int gr = (int) (255 * green[i][j]);
					int b = (int) (255 * blue[i / 2][j / 2]);

					Color c = new Color(r, gr, b);

					goesComposite[j][i] = contrast(c, 48);
				}
			}
		}
		
		goesComposite = correctOrangeBlueSpeckle(goesComposite);

		return goesComposite;
	}

	public static Color[][] createIRGoes(GoesImage band7, GoesImage band13) {
		int[] band2Shape = band7.field("rad").getShape();

		final int CHUNK_SIZE = 100;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) band2Shape[0] / CHUNK_SIZE)][(int) Math
				.ceil((double) band2Shape[1] / CHUNK_SIZE)];

		for (int i = 0; i < renderChunk.length; i++) {
			for (int j = 0; j < renderChunk[i].length; j++) {
				renderChunk[i][j] = true;
			}
		}

		return createIRGoes(band7, band13, renderChunk, CHUNK_SIZE);
	}

	public static Color[][] createIRGoes(GoesImage band7, GoesImage band13, boolean[][] renderChunks, int chunkSize) {
		float[][] band7Rad = band7.field("rad").array2D();
		float[][] band13Rad = band13.field("rad").array2D();

		float[][] band7Temp = new float[band7Rad.length][band7Rad[0].length];
		float[][] band13Temp = new float[band13Rad.length][band13Rad[0].length];
		float band7wavelength = band7.dataFromField("wavelength");
		float band13wavelength = band13.dataFromField("wavelength");

//		System.out.println("band 7: " + band7wavelength + " um");
//		System.out.println("band 13: " + band13wavelength + " um");

		// VERY IMPORTANT!! figure out radiance -> brightness temperature conversion
		for (int i = 0; i < band13Rad.length; i++) {
			for (int j = 0; j < band13Rad[i].length; j++) {
				if (band13Rad[i][j] == -1024) {
					band7Temp[i][j] = -1024;
					band13Temp[i][j] = -1024;
				} else {
//					band7Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavelength(band7Rad[i][j] * 100000, band7wavelength / 1000000);
//					band13Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavelength(band13Rad[i][j] * 100000, band13wavelength / 1000000);
					band7Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavenumber(
							band7Rad[i][j] / 100000.0,
							WeatherUtils.wavelengthToWavenumber(band7wavelength / 1000000.0));
					band13Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavenumber(
							band13Rad[i][j] / 100000.0,
							WeatherUtils.wavelengthToWavenumber(band13wavelength / 1000000.0));
				}
			}
		}
//		System.out.println("band 13: " + band13Rad[400][1500] + " mW m^-2 sr^-1 (cm^-1)^-1");
//		System.out.println("band 13: " + band13Temp[400][1500] + " K");
//		System.out.println("band 7: " + band7Rad[400][1500] + " mW m^-2 sr^-1 (cm^-1)^-1");
//		System.out.println("band 7: " + band7Temp[400][1500] + " K");

		float[][] band13Clip = clip(band13Temp, 90, 273);
		float[][] band13Norm = clip(invNormalize(band13Clip, 0, 500), 0, 255);

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

	public static Color[][] createComposite(GoesImageMcfetch band1, GoesImageMcfetch band2, GoesImageMcfetch band4,
											GeostationaryProjection satProj, DateTime dt) {
		int[] band1Shape = band1.field("rad").getShape();

		final int CHUNK_SIZE = 100;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) band1Shape[1] / CHUNK_SIZE)][(int) Math
				.ceil((double) band1Shape[0] / CHUNK_SIZE)];

		for (int i = 0; i < renderChunk.length; i++) {
			for (int j = 0; j < renderChunk[i].length; j++) {
				renderChunk[i][j] = true;
			}
		}

		return createComposite(band1, band2, band4, satProj, dt, renderChunk, CHUNK_SIZE);
	}

	public static Color[][] createComposite(GoesImageMcfetch band1, GoesImageMcfetch band2, GoesImageMcfetch band4,
											GeostationaryProjection satProj, DateTime dt, boolean[][] renderChunks, int chunkSize) {
		GeoCoord[][] latLon = createLatLonMatrix(band1);
		float[][] solarAlt = createSolarAltitudeMatrix(latLon, dt, renderChunks, chunkSize);

		Color[][] trueColor = createTrueColorGoes(band1, latLon, dt, renderChunks, chunkSize);
		Color[][] irColor = createIRGoes(band2, band4, renderChunks, chunkSize);

		Color[][] goesComposite = new Color[trueColor.length][trueColor[0].length];


//		System.out.println("chunkSize: " + chunkSize);
//		System.out.println("renderChunks.shape: " + renderChunks.length + ", " +  + renderChunks[0].length);
//		System.out.println("trueColor.shape (expected): " + renderChunks.length * chunkSize + ", " +  + renderChunks[0].length * chunkSize);
//		System.out.println("trueColor.shape (actual)  : " + trueColor.length + ", " +  + trueColor[0].length);
//		System.out.println("goesComposite.shape (expected): " + renderChunks.length * chunkSize + ", " +  + renderChunks[0].length * chunkSize);
//		System.out.println("goesComposite.shape (actual)  : " + goesComposite.length + ", " +  + goesComposite[0].length);

		for (int i = 0; i < goesComposite.length; i++) {
			for (int j = 0; j < goesComposite[0].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					float blendFactor = (solarAlt[i][j]) / TERMINATOR_WIDTH_MCFETCH;
					if (blendFactor < 0) {
						blendFactor = 0;
					} else if (blendFactor > 1) {
						blendFactor = 1;
					}

//					goesComposite[i][j] = blendTristims(trueColor[i][j], irColor[i / 4][j / 4], blendFactor);
					goesComposite[i][j] = trueColor[i][j];
				}
			}
		}

		return goesComposite;
	}

	public static Color[][] createTrueColorGoes(GoesImageMcfetch band1, GeoCoord[][] latLon,
												DateTime dt) {
		int[] band2Shape = band1.field("lon").getShape();

		final int CHUNK_SIZE = 100;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) band2Shape[0] / CHUNK_SIZE)][(int) Math
				.ceil((double) band2Shape[1] / CHUNK_SIZE)];

		for (int i = 0; i < renderChunk.length; i++) {
			for (int j = 0; j < renderChunk[i].length; j++) {
				renderChunk[i][j] = true;
			}
		}

		return createTrueColorGoes(band1, latLon, dt, renderChunk, CHUNK_SIZE);
	}

	private static final DateTime WHITE_POINT_CHANGE = new DateTime(1997, 1, 1, 0, 0, DateTimeZone.UTC);
	public static Color[][] createTrueColorGoes(GoesImageMcfetch band1, GeoCoord[][] latLon,
												DateTime dt, boolean[][] renderChunks, int chunkSize) {
		float[][] solarMult = createSolarMultiplierMatrixMcfetch(latLon, dt, renderChunks, chunkSize);

		Color[][] surfaceColor = new Color[latLon.length][latLon[0].length];

		for (int i = 0; i < surfaceColor.length; i++) {
			for (int j = 0; j < surfaceColor[i].length; j++) {
				if (renderChunks[i / chunkSize][j / chunkSize]) {
					surfaceColor[i][j] = ModisBlueMarble.getColor(latLon[i][j]);
				}
			}
		}

        String satellite = band1.field("satellite").getAnnotation();
		float[][] band1Gvar = band1.field("data").array3D()[0];
//		System.out.println("band1Gvar.shape (actual)  : " + band1Gvar.length + ", " +  + band1Gvar[0].length);

		float[][] band1Rad = new float[band1Gvar.length][band1Gvar[0].length];
		for (int i = 0; i < band1Gvar.length; i++) {
			for (int j = 0; j < band1Gvar[i].length; j++) {
				if (renderChunks[i / chunkSize][j / chunkSize]) {
					band1Rad[i][j] = GvarProcessing.spectralRadiance(band1Gvar[i][j], 1, satellite);
				}
			}
		}

//		System.out.println("solarMult.shape: " + solarMult.length + ", " +  + solarMult[0].length);
		for (int i = 0; i < band1Gvar.length; i++) {
			for (int j = 0; j < band1Gvar[i].length; j++) {
				if (renderChunks[i / chunkSize][j / chunkSize]) {
					float mult = solarMult[j][i];

					if (!Double.isNaN(mult)) {
						band1Rad[i][j] = band1Rad[i][j] * mult;
					}
				}
			}
		}

		// VERY IMPORTANT!! normalize and color-balance radiances to the correct
		// specific ranges
		float[][] band1Clip = new float[band1Rad.length][band1Rad[0].length];
		for (int i = 0; i < band1Clip.length; i++) {
			for (int j = 0; j < band1Clip[i].length; j++) {
				if (renderChunks[i / chunkSize][j / chunkSize]) {
					float whitePointMult = dt.isBefore(WHITE_POINT_CHANGE) ? 2.5f : 1.75f;
					band1Clip[i][j] = clip(band1Rad[i][j] / (WHITE_POINT * whitePointMult), 0, 1);
				}
			}
		}

//		band1Clip = normalize(band1Rad, 0, 1);

		final float GAMMA = 2.2f;

		float[][] band1NormG = new float[band1Clip.length][band1Clip[0].length];
		for (int i = 0; i < band1NormG.length; i++) {
			for (int j = 0; j < band1NormG[i].length; j++) {
				if (renderChunks[i / chunkSize][j / chunkSize]) {
					band1NormG[i][j] = gammaCorrect(band1Clip[i][j], GAMMA);
				}
			}
		}

		final Color cloudColor = new Color(255, 255, 255);

		Color[][] goesComposite = new Color[band1NormG[0].length][band1NormG.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
//			if(i % 500 == 0) System.out.println("Goes True-Color Composite " + (100 * (float) i/goesComposite[0].length) + "% complete");

			for (int j = 0; j < goesComposite.length; j++) {
				if (renderChunks[i / chunkSize][j / chunkSize]) {
					float cloudColorAlpha = band1NormG[i][j];

//					System.out.println("band1Gvar: " + band1Gvar[i][j]);
//					System.out.println("band1Rad-deriv: " + GvarProcessing.spectralRadiance(band1Gvar[i][j], 1, satellite));
//					System.out.println("band1Rad: " + band1Rad[i][j]);
//					System.out.println("band1Clip: " + band1Clip[i][j]);
//					System.out.println("band1NormG: " + band1NormG[i][j]);
//					System.out.println("cloudColorAlpha: " + cloudColorAlpha);

					Color sfcClr = surfaceColor[i][j];

					int r = (int) (Math.pow(1 - cloudColorAlpha, 2) * sfcClr.getRed()
									+ (cloudColorAlpha) * cloudColor.getRed());

					int gr = (int) (Math.pow(1 - cloudColorAlpha, 2) * sfcClr.getGreen()
							+ (cloudColorAlpha) * cloudColor.getGreen());
					int b = (int) (Math.pow(1 - cloudColorAlpha, 2) * sfcClr.getBlue()
							+ (cloudColorAlpha) * cloudColor.getBlue());

					Color c = new Color(r, gr, b);

					goesComposite[j][i] = c;
//					goesComposite[j][i] = contrast(c, 48);
				}
			}
		}

//		BufferedImage testImg = new BufferedImage(goesComposite.length, goesComposite[0].length, BufferedImage.TYPE_4BYTE_ABGR);
//		Graphics2D g = testImg.createGraphics();
//
//		for (int i = 0; i < goesComposite.length; i++) {
//			for (int j = 0; j < goesComposite[i].length; j++) {
//				g.setColor(goesComposite[i][j]);
//				g.fillRect(i, j, 1, 1);
//			}
//		}
//
//		try {
//			ImageIO.write(testImg, "PNG", new File("mcfetch-trueColor-test.png"));
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
////		System.exit(44);

		return goesComposite;
	}

	public static Color[][] createIRGoes(GoesImageMcfetch band2, GoesImageMcfetch band4) {
		int[] band2Shape = band2.field("data").getShape();

		final int CHUNK_SIZE = 100;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) band2Shape[1] / CHUNK_SIZE)][(int) Math
				.ceil((double) band2Shape[2] / CHUNK_SIZE)];

		for (int i = 0; i < renderChunk.length; i++) {
			for (int j = 0; j < renderChunk[i].length; j++) {
				renderChunk[i][j] = true;
			}
		}

		return createIRGoes(band2, band4, renderChunk, CHUNK_SIZE);
	}

	public static Color[][] createIRGoes(GoesImageMcfetch band2, GoesImageMcfetch band4, boolean[][] renderChunks, int chunkSize) {
		float[][] band2Gvar = band2.field("data").array3D()[0];
		float[][] band4Gvar = band4.field("data").array3D()[0];

		float[][] band2Temp = new float[band2Gvar.length][band2Gvar[0].length];
		float[][] band4Temp = new float[band4Gvar.length][band4Gvar[0].length];
		float band2wavelength = 3.90f; // micrometers
		float band4wavelength = 10.70f; // micrometers

//		System.out.println("band 7: " + band7wavelength + " um");
//		System.out.println("band 13: " + band13wavelength + " um");

		// VERY IMPORTANT!! figure out radiance -> brightness temperature conversion
		for (int i = 0; i < band2Gvar.length; i++) {
			for (int j = 0; j < band2Gvar[i].length; j++) {
				if (band4Gvar[i][j] == -1024) {
					band2Temp[i][j] = -1024;
					band4Temp[i][j] = -1024;
				} else {
//					band7Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavelength(band7Rad[i][j] * 100000, band7wavelength / 1000000);
//					band13Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavelength(band13Rad[i][j] * 100000, band13wavelength / 1000000);

					String satellite = band2.field("satellite").getAnnotation();
					float band2Radiance = GvarProcessing.spectralRadiance(band2Gvar[i][j], 2, satellite);
					float band4Radiance = GvarProcessing.spectralRadiance(band4Gvar[i][j], 4, satellite);

					band2Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavenumber(
							band2Radiance / 100000.0,
							WeatherUtils.wavelengthToWavenumber(band2wavelength / 1000000.0));
					band4Temp[i][j] = (float) WeatherUtils.brightnessTemperatureFromWavenumber(
							band4Radiance / 100000.0,
							WeatherUtils.wavelengthToWavenumber(band4wavelength / 1000000.0));
				}
			}
		}
//		System.out.println("band 4: " + GvarProcessing.spectralRadiance(band4Gvar[150][300], 4, band2.field("satellite").getAnnotation()) + " mW m^-2 sr^-1 (cm^-1)^-1");
//		System.out.println("band 4: " + band4Temp[150][300] + " K");
//		System.out.println("band 2: " + GvarProcessing.spectralRadiance(band2Gvar[150][300], 2, band2.field("satellite").getAnnotation()) + " mW m^-2 sr^-1 (cm^-1)^-1");
//		System.out.println("band 2: " + band2Temp[150][300] + " K");
//		System.out.println("min(band4Temp):" + min(band4Temp));
//		System.out.println("max(band4Temp):" + max(band4Temp));

		float[][] band4Clip = clip(band4Temp, 90, 273);
		float[][] band4Norm = clip(invNormalizePrecise(band4Clip, 90, 273, 0, 500), 0, 255);

		Color[][] goesComposite = new Color[band4Temp[0].length][band4Temp.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
			for (int j = 0; j < goesComposite.length; j++) {
				float fog = band4Temp[i][j] - band2Temp[i][j];

				float fogBlue = clip(linScale(0, 5, 0, 150, fog), 0, 150);

				Color fogColor = new Color((int) (0.5 * fogBlue), (int) (0.75 * fogBlue), (int) (1.0 * fogBlue));
//				Color band13Color = new Color((int) band13Norm[i][j], (int) band13Norm[i][j],
//						(int) Double.max(band13Norm[i][j], fogBlue));
//
//				goesComposite[j][i] = maxTristims(fogColor, band13Color);

				Color band4Color = new Color((int) band4Norm[i][j], (int) band4Norm[i][j],
						(int) Double.max(band4Norm[i][j], fogBlue));

				goesComposite[j][i] = maxTristims(band4Color, fogColor);

				if (band4Temp[i][j] == -1024) {
					goesComposite[j][i] = Color.BLACK;
				}
			}
		}

		return goesComposite;
	}
	
	private static void fillGaps(Color[][] img) {
		for(int i = 1; i < img.length - 1; i++) {
			for(int j = 1; j < img[i].length - 1; j++) {
				Color cL = img[i - 1][j];
				Color cR = img[i + 1][j];
				Color cU = img[i][j - 1];
				Color cD = img[i][j + 1];
				Color cC = img[i][j];
				
				if(cL != null && cR != null && cU != null && cD != null && cC == null) {
					Color avgColor = rgbAvg(cL, cR, cU, cD);
					
					img[i][j] = avgColor;
				}
			}
		}
	}
	
	private static Color[][] correctOrangeBlueSpeckle(Color[][] img) {
		Color[][] ret = img.clone();
		
		assert img.length % 2 == 0;
		assert img[0].length % 2 == 0;
		
		for(int i = 0; i < img.length; i+=2) {
			for(int j = 0; j < img[i].length ; j+=2) {
				Color c00 = img[i][j];
				Color c10 = img[i + 1][j];
				Color c01 = img[i][j + 1];
				Color c11 = img[i + 1][j + 1];
				
				if(c00 == null && c10 == null && c01 == null && c11 == null) {
					continue;
				}
				
				Color avg = rgbAvg(c00, c10, c01, c11);
				
				if(c00 == null) c00 = avg;
				if(c10 == null) c10 = avg;
				if(c01 == null) c01 = avg;
				if(c11 == null) c11 = avg;

				double r00 = c00.getRed();
				double r10 = c10.getRed();
				double r01 = c01.getRed();
				double r11 = c11.getRed();
				
				double averageRed = avg.getRed();
				
				double mult00 = r00/averageRed;
				double mult10 = r10/averageRed;
				double mult01 = r01/averageRed;
				double mult11 = r11/averageRed;
				
				double b00 = Double.min(mult00 * c00.getBlue(), 255);
				double b10 = Double.min(mult10 * c10.getBlue(), 255);
				double b01 = Double.min(mult01 * c01.getBlue(), 255);
				double b11 = Double.min(mult11 * c11.getBlue(), 255);
				
				double g00 = Double.min(mult00 * c00.getGreen(), 255);
				double g10 = Double.min(mult10 * c10.getGreen(), 255);
				double g01 = Double.min(mult01 * c01.getGreen(), 255);
				double g11 = Double.min(mult11 * c11.getGreen(), 255);
				
				Color new_c00 = new Color((int) r00, (int) g00, (int) b00);
				Color new_c10 = new Color((int) r10, (int) g10, (int) b10);
				Color new_c01 = new Color((int) r01, (int) g01, (int) b01);
				Color new_c11 = new Color((int) r11, (int) g11, (int) b11);
				
				final double BLEND_FACTOR = 0.5;
				if(img[i][j] != null && r00 >= 32) ret[i][j] = rgbBlend(c00, new_c00, BLEND_FACTOR);
				if(img[i + 1][j] != null && r10 >= 32) ret[i + 1][j] = rgbBlend(c10, new_c10, BLEND_FACTOR);
				if(img[i][j + 1] != null && r01 >= 32) ret[i][j + 1] = rgbBlend(c01, new_c01, BLEND_FACTOR);
				if(img[i + 1][j + 1] != null && r11 >= 32) ret[i + 1][j + 1] = rgbBlend(c11, new_c11, BLEND_FACTOR);
			}
		}
		
		return ret;
	}
	
	private static Color rgbAvg(Color... colors) {
		int rSum = 0;
		int gSum = 0;
		int bSum = 0;
		int count = 0;
		
		for(int i = 0; i < colors.length; i++) {
			Color c = colors[i];
			
			if(c != null) {
				rSum += c.getRed();
				gSum += c.getGreen();
				bSum += c.getBlue();
				count++;
			}
		}
		
		Color avg = new Color(rSum/count, gSum/count, bSum/count);
		
		return avg;
	}
	
	private static Color rgbBlend(Color c1, Color c2, double blendFactor) {
		int r1 = c1.getRed();
		int g1 = c1.getGreen();
		int b1 = c1.getBlue();
		
		int r2 = c2.getRed();
		int g2 = c2.getGreen();
		int b2 = c2.getBlue();
		
		int r3 = (int) ((1 - blendFactor) * r1 + blendFactor * r2);
		int g3 = (int) ((1 - blendFactor) * g1 + blendFactor * g2);
		int b3 = (int) ((1 - blendFactor) * b1 + blendFactor * b2);
		
		Color c3 = new Color(r3, g3, b3);
		return c3;
	}

	private static float[][] createSolarAltitudeMatrix(GeoCoord[][] latLonMatrix, DateTime dt, boolean[][] renderChunks,
			int chunkSize) {
		float[][] matrix = new float[latLonMatrix[0].length][latLonMatrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					GeoCoord coord = latLonMatrix[j][i];
					matrix[i][j] = (float) Math
							.toDegrees(SolarPosition.solarAltitude(dt, coord.getLat(), coord.getLon()));
				}
			}
		}

		return matrix;
	}

	private static final float MAX_MULT = (float) (1.0f / Math.sin(Math.toRadians(TERMINATOR_WIDTH * 3)));

	private static float[][] createSolarMultiplierMatrix(GeoCoord[][] latLonMatrix, DateTime dt,
			boolean[][] renderChunks, int chunkSize) {
		float[][] matrix = new float[latLonMatrix[0].length][latLonMatrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					GeoCoord coord = latLonMatrix[j][i];
					float secantSolarZenith = (float) (1.0f
							/ SolarPosition.cosSolarZenithAngle(dt, coord.getLat(), coord.getLon()));

					if (secantSolarZenith > MAX_MULT) {
						secantSolarZenith = MAX_MULT;
					}

					matrix[i][j] = secantSolarZenith;
				}
			}
		}

		return matrix;
	}

	// temu geocolor
	private static final float TERMINATOR_WIDTH_MCFETCH = 15.0f; // degrees of arc
	private static final float MAX_MULT_MCFETCH = (float) (1.0f / Math.sin(Math.toRadians(TERMINATOR_WIDTH_MCFETCH/3)));

	private static float[][] createSolarMultiplierMatrixMcfetch(GeoCoord[][] latLonMatrix, DateTime dt,
														 boolean[][] renderChunks, int chunkSize) {
		float[][] matrix = new float[latLonMatrix[0].length][latLonMatrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (renderChunks[j / chunkSize][i / chunkSize]) {
					GeoCoord coord = latLonMatrix[j][i];
					float secantSolarZenith = (float) (1.0f
							/ SolarPosition.cosSolarZenithAngle(dt, coord.getLat(), coord.getLon()));

					if (secantSolarZenith > MAX_MULT_MCFETCH) {
						secantSolarZenith = MAX_MULT_MCFETCH;
					}

					matrix[i][j] = secantSolarZenith;
				}
			}
		}

		return matrix;
	}

	private static GeoCoord[][] createLatLonMatrix(GoesImageMcfetch goes) {
		float[][] lat = goes.field("lat").array2D();
		float[][] lon = goes.field("lon").array2D();

		GeoCoord[][] matrix = new GeoCoord[lat.length][lat[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if(lat[i][j] > 1024000 && lon[i][j] > 1024000) {
					matrix[i][j] = new GeoCoord(Float.NaN, Float.NaN);
				} else {
					matrix[i][j] = new GeoCoord(lat[i][j], lon[i][j]);
				}
			}
		}

		return matrix;
	}

	private static GeoCoord[][] createLatLonMatrix(GoesImage goes, GeostationaryProjection satProj,
			boolean[][] renderChunks, int chunkSize) {
		float[] x = goes.field("x").array1D();
		float[] y = goes.field("y").array1D();

		GeoCoord[][] matrix = new GeoCoord[x.length][y.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (renderChunks[i / chunkSize][j / chunkSize]) {
					matrix[i][j] = satProj.projectXYToLatLon(-x[i], y[j]);
				}
			}
		}

		return matrix;
	}

	private static GeoCoord[][] createLatLonMatrix(GoesMultibandImage goes, GeostationaryProjection satProj,
			boolean[][] renderChunks, int chunkSize) {
		float[] x = goes.field("x").array1D();
		float[] y = goes.field("y").array1D();

		GeoCoord[][] matrix = new GeoCoord[x.length][y.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = satProj.projectXYToLatLon(-x[i], y[j]);
			}
		}

		return matrix;
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

	private static Color blendTristims(Color a, Color b, float aWeight) {
		int rA = a.getRed();
		int gA = a.getGreen();
		int bA = a.getBlue();

		int rB = b.getRed();
		int gB = b.getGreen();
		int bB = b.getBlue();

		assert aWeight >= 0;
		assert aWeight <= 1;

		float bWeight = 1 - aWeight;

		int rC = (int) (aWeight * rA + bWeight * rB);
		int gC = (int) (aWeight * gA + bWeight * gB);
		int bC = (int) (aWeight * bA + bWeight * bB);

		Color comp = new Color(rC, gC, bC);

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

	private static float[][] invNormalizePrecise(float[][] arr, float oldMin, float oldMax, float newMin, float newMax) {
		float[][] normArr = new float[arr.length][arr[0].length];

		float max = oldMax;
		float min = oldMin;

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

	private static float gammaCorrect(float val, float gamma) {
		float gammaCorr = (float) Math.pow(val, 1 / gamma);

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
				if (arr[i][j] != maxO && arr[i][j] != -1024.0) {
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

	/**
	 * 
	 * @param arr
	 * @param startI (inclusive)
	 * @param endI   (inclusive)
	 * @return
	 */
	private static float[] subsetArray1D(float[] arr, int startI, int endI) {
		float[] subset = new float[endI + 1 - startI];

		for (int i = 0; i < subset.length; i++) {
			subset[i] = arr[startI + i];
		}

		return subset;
	}

	/**
	 * 
	 * @param arr
	 * @param startI (inclusive)
	 * @param endI   (inclusive)
	 * @param startJ (inclusive)
	 * @param endJ   (inclusive)
	 * @return
	 */
	private static float[][] subsetArray2D(float[][] arr, int startI, int endI, int startJ, int endJ) {
		float[][] subset = new float[endI + 1 - startI][endJ + 1 - startJ];

		for (int i = 0; i < subset.length; i++) {
			for (int j = 0; j < subset[i].length; j++) {
				subset[i][j] = arr[startI + i][startJ + j];
			}
		}

		return subset;
	}

	private static float linScale(float preMin, float preMax, float postMin, float postMax, float value) {
		float slope = (postMax - postMin) / (preMax - preMin);

		return slope * (value - preMin) + postMin;
	}
}
