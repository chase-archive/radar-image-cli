package com.chasearchive.radarImageCli.satellite;

import static com.chasearchive.radarImageCli.RadarImageCli.logger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.weatherUtils.WeatherUtils;
import com.ameliaWx.wxArchives.PointF;
import com.ameliaWx.wxArchives.earthWeather.goes.GoesAws;
import com.ameliaWx.wxArchives.earthWeather.goes.SatelliteSector;
import com.ameliaWx.wxArchives.earthWeather.iemWarnings.DamageTag;
import com.ameliaWx.wxArchives.earthWeather.iemWarnings.TornadoTag;
import com.ameliaWx.wxArchives.earthWeather.iemWarnings.WarningArchive;
import com.ameliaWx.wxArchives.earthWeather.iemWarnings.WarningPolygon;
import com.chasearchive.radarImageCli.City;
import com.chasearchive.radarImageCli.ColorTable;
import com.chasearchive.radarImageCli.DebugLoggerLevel;
import com.chasearchive.radarImageCli.Layering;
import com.chasearchive.radarImageCli.PointD;
import com.chasearchive.radarImageCli.RadarImageGenerator;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class SatelliteImageGenerator {
	// TODO:
	// Make full-disk subsets to make the memory management possible to handle
	// Switch to multiband if absolutely necessary, which it might be
	// Data for Himawari 8 and Himawari 9
	// Possibly METEOSAT data for Europe and Africa, depends on availability

	public static ArrayList<City> cities;
	static {
		cities = new ArrayList<>();
		loadCities();
	}
	
	public static void main(String[] args) {
		try {
			getGridsatData(new DateTime(2016, 04, 11, 23, 00, DateTimeZone.UTC), 33.0f, -96.5f);
		} catch (NoValidSatelliteScansFoundException e) {
			e.printStackTrace();
		}
	}

	public static HashMap<String, BufferedImage> generateSatellite(DateTime time, double lat, double lon,
			SatelliteGeneratorSettings settings) throws IOException {
		SatelliteSource source = SatelliteSource.GOES_EAST;
		SatelliteSector sector = SatelliteSector.GOES_CONUS;

		// this decision tree will likely evolve as new data sources are added
		if (lon > -106 && lon <= 74) {
			source = SatelliteSource.GOES_EAST;

			if(time.isBefore(GRIDSAT_GOES_16_OPERATIONAL_CUTOFF)) {
				source = SatelliteSource.GRIDSAT;
			}
			
			if (lat > 22 && lat <= 51 && lon > -106 && lon <= -59) {
				sector = SatelliteSector.GOES_CONUS;
			} else {
				sector = SatelliteSector.GOES_FULL_DISK;
			}
		} else if ((lon > -180 && lon <= -106) || (lon > 74 && lon <= 180)) {
			source = SatelliteSource.GOES_WEST;

			if(time.isBefore(GRIDSAT_END)) {
				source = SatelliteSource.GRIDSAT;
			}

			if (lat > 22 && lat <= 51 && lon > -162 && lon <= -106) {
				sector = SatelliteSector.GOES_PACUS;
			} else {
				sector = SatelliteSector.GOES_FULL_DISK;
			}
		}

		System.out.println("source: " + source);
		System.out.println("sector: " + sector);

		settings.setSource(source);
		settings.setSector(sector);

		RotateLatLonProjection plotProj = new RotateLatLonProjection(lat, lon, 111.32, 111.32, 1000, 1000);
		GeostationaryProjection satProj = null;

		if (settings.getSource() == SatelliteSource.GOES_EAST) {
			satProj = GeostationaryProjection.GOES_EAST;
		} else if (settings.getSource() == SatelliteSource.GOES_WEST) {
			satProj = GeostationaryProjection.GOES_WEST;
		} else if (settings.getSource() == SatelliteSource.GRIDSAT) {

		}

		BufferedImage basemap = generateBasemap(lat, lon, settings, plotProj);

		BufferedImage satPlot = null;

		if (settings.getSource() == SatelliteSource.GOES_EAST || settings.getSource() == SatelliteSource.GOES_WEST) {
			if (settings.getSector() == SatelliteSector.GOES_CONUS || settings.getSector() == SatelliteSector.GOES_PACUS) {
				long downloadStartTime = System.currentTimeMillis();
				File[] satFiles = null;
				try {
					satFiles = getGoesData(time, settings.getImageType(), settings.getSource(), settings.getSector());
				} catch (NoValidSatelliteScansFoundException e) {
					e.printStackTrace();
				}
				long downloadEndTime = System.currentTimeMillis();
				System.out.println("download time: " + (downloadEndTime - downloadStartTime)/1000.0 + " s");
	
				long fileLoadStartTime = System.currentTimeMillis();
				GoesImage band1 = GoesImage.loadFromFile(satFiles[0]);
				GoesImage band2 = GoesImage.loadFromFile(satFiles[1]);
				GoesImage band3 = GoesImage.loadFromFile(satFiles[2]);
				GoesImage band7 = GoesImage.loadFromFile(satFiles[3]);
				GoesImage band13 = GoesImage.loadFromFile(satFiles[4]);
	
				GoesImage[] goesImages = { band1, band2, band3, band7, band13 };
				long fileLoadEndTime = System.currentTimeMillis();
				System.out.println("file load time: " + (fileLoadEndTime - fileLoadStartTime)/1000.0 + " s");
	
				long plotStartTime = System.currentTimeMillis();
				satPlot = generateSatellitePlot(goesImages, time, lat, lon, settings, satProj, plotProj);
				long plotEndTime = System.currentTimeMillis();
				System.out.println("overall plotting time: " + (plotEndTime - plotStartTime)/1000.0 + " s");
			} else if (settings.getSector() == SatelliteSector.GOES_FULL_DISK) {
				long downloadStartTime = System.currentTimeMillis();
				File satMultibandFile = null;
				try {
					satMultibandFile = getGoesMultibandData(time, settings.getSource(), settings.getSector());
				} catch (NoValidSatelliteScansFoundException e) {
					e.printStackTrace();
				}
				long downloadEndTime = System.currentTimeMillis();
				System.out.println("download time: " + (downloadEndTime - downloadStartTime)/1000.0 + " s");
	
				long fileLoadStartTime = System.currentTimeMillis();
				GoesMultibandImage goesImage = GoesMultibandImage.loadFromFile(satMultibandFile);
	
				long fileLoadEndTime = System.currentTimeMillis();
				System.out.println("file load time: " + (fileLoadEndTime - fileLoadStartTime)/1000.0 + " s");
	
				long plotStartTime = System.currentTimeMillis();
				satPlot = generateSatellitePlot(goesImage, time, lat, lon, settings, satProj, plotProj);
				long plotEndTime = System.currentTimeMillis();
				System.out.println("overall plotting time: " + (plotEndTime - plotStartTime)/1000.0 + " s");
			}
		} else if (settings.getSource() == SatelliteSource.GRIDSAT) {
			long downloadStartTime = System.currentTimeMillis();
			File gridsatFile = null;
			try {
				 gridsatFile = getGridsatData(time, (float) lat, (float) lon);
			} catch (NoValidSatelliteScansFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long downloadEndTime = System.currentTimeMillis();
			System.out.println("download time: " + (downloadEndTime - downloadStartTime)/1000.0 + " s");
			
			System.out.println(gridsatFile);
			
			if(gridsatFile != null) {
				long fileLoadStartTime = System.currentTimeMillis();
				GridsatImage gridsat = GridsatImage.loadFromFile(gridsatFile);
				long fileLoadEndTime = System.currentTimeMillis();
				System.out.println("file load time: " + (fileLoadEndTime - fileLoadStartTime)/1000.0 + " s");
	
				long plotStartTime = System.currentTimeMillis();
				satPlot = generateSatellitePlot(gridsat, time, lat, lon, settings, plotProj);
				long plotEndTime = System.currentTimeMillis();
				System.out.println("overall plotting time: " + (plotEndTime - plotStartTime)/1000.0 + " s");
			} else {
				satPlot = null;
			}
		}

//		BufferedImage warningPlot = generateWarningPlot(time, lat, lon, settings, plotProj);
		BufferedImage citiesPlot = generateCityPlot(lat, lon, settings, plotProj);
		BufferedImage availabilityNoticeLayer = new BufferedImage(basemap.getWidth(), basemap.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);;

		BufferedImage logo = ImageIO.read(RadarImageGenerator.loadResourceAsFile("res/chase-archive-logo-256pix.png"));

		BufferedImage timestampLayer = new BufferedImage(basemap.getWidth(), basemap.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = timestampLayer.createGraphics();

		g.setColor(new Color(0, 0, 0, 96));
		g.fillRect(0, timestampLayer.getHeight() - 56, 530, 56);
		
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
		g.setColor(Color.BLACK);
		g.drawString(dateStringAlt(time), 9, basemap.getHeight() - 14);
		g.drawString(dateStringAlt(time), 11, basemap.getHeight() - 16);
		g.drawString(dateStringAlt(time), 9, basemap.getHeight() - 16);
		g.drawString(dateStringAlt(time), 11, basemap.getHeight() - 14);
		g.setColor(Color.WHITE);
		g.drawString(dateStringAlt(time), 10, basemap.getHeight() - 15);
		
		BufferedImage compositePlot = new BufferedImage(basemap.getWidth(), basemap.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		g = compositePlot.createGraphics();

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, basemap.getWidth(), basemap.getHeight());

		if(satPlot != null) {
			g.drawImage(satPlot, 0, 0, null);
		}
		g.drawImage(basemap, 0, 0, null);
		if(satPlot != null) {
			g.drawImage(citiesPlot, 0, 0, null);
//			g.drawImage(warningPlot, 0, 0, null);
		}
		g.drawImage(logo, 0, 0, null);
		g.drawImage(timestampLayer, 0, 0, null);
		if(satPlot == null) {
			availabilityNoticeLayer = noDataAvailableNotice(settings);
			g.drawImage(availabilityNoticeLayer, 0, 0, null);
		}

		g.dispose();

		HashMap<String, BufferedImage> imagesToExport = new HashMap<>();
		
		if (settings.getLayering() != Layering.COMPOSITE_ONLY) {
			imagesToExport.put("basemap.png", basemap);
			imagesToExport.put("cities.png", citiesPlot);
			imagesToExport.put("availability.png", availabilityNoticeLayer);
			imagesToExport.put("satellite.png", satPlot);
			imagesToExport.put("timestamp.png", timestampLayer);
		}
		if (settings.getLayering() != Layering.SEPARATE_ONLY) {
			imagesToExport.put("composite.png", compositePlot);
		}

		return imagesToExport;
	}

	public static DateTimeZone timeZone = DateTimeZone.forID("America/Chicago");
	public static String timeZoneCode = "CST";

	private static String dateStringAlt(DateTime d) {
		DateTime c = d.toDateTime(timeZone);

		String daylightCode = timeZoneCode.substring(0, timeZoneCode.length() - 2) + "DT";

		boolean isPm = c.getHourOfDay() >= 12;
		boolean is12 = c.getHourOfDay() == 0 || c.getHourOfDay() == 12;
		return String.format("%04d", c.getYear()) + "-" + String.format("%02d", c.getMonthOfYear()) + "-"
				+ String.format("%02d", c.getDayOfMonth()) + " "
				+ String.format("%02d", c.getHourOfDay() % 12 + (is12 ? 12 : 0)) + ":"
				+ String.format("%02d", c.getMinuteOfHour()) + " " + (isPm ? "PM" : "AM") + " "
				+ (TimeZone.getTimeZone(timeZone.getID()).inDaylightTime(d.toDate()) ? daylightCode : timeZoneCode);
	}

	private static final ColorTable brightnessTemperatureColorTable = new ColorTable(
			RadarImageGenerator.loadResourceAsFile("res/aru-br-temp.pal"), 0.1f, 10, "dBZ");

	private static BufferedImage generateBasemap(double lat, double lon, SatelliteGeneratorSettings settings,
			RotateLatLonProjection plotProj) throws IOException {
		ArrayList<ArrayList<PointD>> countyBorders;
		ArrayList<ArrayList<PointD>> stateBorders;
		ArrayList<ArrayList<PointD>> interstates;
		ArrayList<ArrayList<PointD>> majorRoads;
		ArrayList<ArrayList<PointD>> estadoBorders;
		ArrayList<ArrayList<PointD>> provinceBorders;
		ArrayList<ArrayList<PointD>> caSubdBorders;
		ArrayList<ArrayList<PointD>> geoboundariesADM0;
		ArrayList<ArrayList<PointD>> geoboundariesADM1;

		File countyBordersKML = RadarImageGenerator.loadResourceAsFile("res/usCounties.kml");
		File stateBordersKML = RadarImageGenerator.loadResourceAsFile("res/usStates.kml");
		File interstatesPoly = RadarImageGenerator.loadResourceAsFile("res/primary-roads.poly");
		File interstatesPolyMeta = RadarImageGenerator.loadResourceAsFile("res/primary-roads.poly.meta");
		File majorRoadsPoly = RadarImageGenerator.loadResourceAsFile("res/prisec-roads.poly");
		File majorRoadsPolyMeta = RadarImageGenerator.loadResourceAsFile("res/prisec-roads.poly.meta");

		File mxEstadosPoly = RadarImageGenerator.loadResourceAsFile("res/mxEstados.poly");
		File mxEstadosPolyMeta = RadarImageGenerator.loadResourceAsFile("res/mxEstados.poly.meta");
		File caProvincesPoly = RadarImageGenerator.loadResourceAsFile("res/caProvinces.poly");
		File caProvincesPolyMeta = RadarImageGenerator.loadResourceAsFile("res/caProvinces.poly.meta");
		File caAdminSubdPoly = RadarImageGenerator.loadResourceAsFile("res/caAdminSubd.poly");
		File caAdminSubdPolyMeta = RadarImageGenerator.loadResourceAsFile("res/caAdminSubd.poly.meta");

		File geoboundariesADM0Poly = RadarImageGenerator.loadResourceAsFile("res/geoboundaries-ADM0.poly");
		File geoboundariesADM0PolyMeta = RadarImageGenerator.loadResourceAsFile("res/geoboundaries-ADM0.poly.meta");
		File geoboundariesADM1Poly = RadarImageGenerator.loadResourceAsFile("res/geoboundaries-ADM1.poly");
		File geoboundariesADM1PolyMeta = RadarImageGenerator.loadResourceAsFile("res/geoboundaries-ADM1.poly.meta");

		countyBorders = getPolygons(countyBordersKML);
		stateBorders = getPolygons(stateBordersKML);
		interstates = getPolygons(interstatesPoly, interstatesPolyMeta);
		majorRoads = getPolygons(majorRoadsPoly, majorRoadsPolyMeta);
		estadoBorders = getPolygons(mxEstadosPoly, mxEstadosPolyMeta);
		provinceBorders = getPolygons(caProvincesPoly, caProvincesPolyMeta);
		caSubdBorders = getPolygons(caAdminSubdPoly, caAdminSubdPolyMeta);
		geoboundariesADM0 = getPolygons(geoboundariesADM0Poly, geoboundariesADM0PolyMeta);
		geoboundariesADM1 = getPolygons(geoboundariesADM1Poly, geoboundariesADM1PolyMeta);

		GeoCoord latLonProjected = plotProj.rotateLatLon(lon, lat);
//		System.out.println("latLonProjected (need to zero this out): " + latLonProjected);
//		GeoCoord latLonProjected1 = plotProj.rotateLatLon(lon, lat + 10);
//		System.out.println("latLonProjected (need to 10 this out): " + latLonProjected1);
//		GeoCoord latLonProjected2 = plotProj.rotateLatLon(lon + 10, lat);
//		System.out.println("latLonProjected (need to ??? this out): " + latLonProjected2);
		GeoCoord trueNorthPointer = plotProj.rotateLatLon(lon, lat + 0.01); // i love finite differencing

		double trueNorth_dx = trueNorthPointer.getLat() - latLonProjected.getLat();
		double trueNorth_dy = trueNorthPointer.getLon() - latLonProjected.getLon();

		logger.println("trueNorth_dx: " + trueNorth_dx, DebugLoggerLevel.VERBOSE);
		logger.println("trueNorth_dy: " + trueNorth_dy, DebugLoggerLevel.VERBOSE);

		double rotationAngle = Math.atan2(-trueNorth_dx, -trueNorth_dy);

		logger.println("rotationAngle: " + Math.toDegrees(rotationAngle) + " deg", DebugLoggerLevel.BRIEF);

		BufferedImage basemap = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = basemap.createGraphics();

		BasicStroke bs = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke ts = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		BufferedImage states = new BufferedImage(basemap.getWidth(), basemap.getHeight(),
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g1 = states.createGraphics();

		BufferedImage counties = new BufferedImage(basemap.getWidth(), basemap.getHeight(),
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = counties.createGraphics();

		BufferedImage highways = new BufferedImage(basemap.getWidth(), basemap.getHeight(),
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g3 = highways.createGraphics();

		BufferedImage roads = new BufferedImage(basemap.getWidth(), basemap.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g4 = roads.createGraphics();

		// lambert stuff
//		PointD latLonProjectedUL = new PointD(
//				latLonProjected.getX() - (111.32 / plotProj.dx * settings.getSize() * settings.getAspectRatioFloat()),
//				latLonProjected.getY() - (111.32 / plotProj.dy * settings.getSize()));
//		PointD latLonProjectedDR = new PointD(
//				latLonProjected.getX() + (111.32 / plotProj.dx * settings.getSize() * settings.getAspectRatioFloat()),
//				latLonProjected.getY() + (111.32 / plotProj.dy * settings.getSize()));

		PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
				(settings.getSize()));
		PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
				-(settings.getSize()));

		logger.println("latLonProjectedUL: " + latLonProjectedUL, DebugLoggerLevel.VERBOSE);
		logger.println("latLonProjectedDR: " + latLonProjectedDR, DebugLoggerLevel.VERBOSE);

		g1.setColor(new Color(255, 255, 255));
		g1.setStroke(bs);
		for (ArrayList<PointD> polygon : stateBorders) {
			for (int i = 0; i < polygon.size(); i++) {
				int j = i + 1;
				if (j == polygon.size())
					j = 0;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					g1.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}
		for (ArrayList<PointD> polygon : estadoBorders) {
			for (int i = 0; i < polygon.size(); i++) {
				int j = i + 1;
				if (j == polygon.size())
					j = 0;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					g1.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}
		for (ArrayList<PointD> polygon : provinceBorders) {
			for (int i = 0; i < polygon.size(); i++) {
				int j = i + 1;
				if (j == polygon.size())
					j = 0;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					g1.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}
		for (ArrayList<PointD> polygon : geoboundariesADM0) {
			for (int i = 0; i < polygon.size(); i++) {
				int j = i + 1;
				if (j == polygon.size())
					j = 0;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
//					if (!(((polygon.get(i).getY() > 24 && polygon.get(i).getY() <= 51 && polygon.get(i).getX() > -162 && polygon.get(i).getX() <= -59) &&
//							(polygon.get(j).getY() > 24 && polygon.get(j).getY() <= 51 && polygon.get(j).getX() > -162 && polygon.get(j).getX() <= -59)) &&
//							!((polygon.get(i).getY() <= 28 && polygon.get(i).getX() > -79.5 && polygon.get(i).getX() <= -70) &&
//							!(polygon.get(j).getY() <= 28 && polygon.get(j).getX() > -79.5 && polygon.get(j).getX() <= -70)) &&
//							!((polygon.get(i).getY() <= 33 && polygon.get(i).getX() > -67) &&
//							!(polygon.get(j).getY() <= 33 && polygon.get(j).getX() > -67)))) {
					if (!(((polygon.get(i).getY() > 24 && polygon.get(i).getY() <= 51 && polygon.get(i).getX() > -162 && polygon.get(i).getX() <= -59) &&
							(polygon.get(j).getY() > 24 && polygon.get(j).getY() <= 51 && polygon.get(j).getX() > -162 && polygon.get(j).getX() <= -59)))) {
						g1.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
					} else if((polygon.get(i).getY() <= 28 && polygon.get(i).getX() > -79.5 && polygon.get(i).getX() <= -70 &&
							polygon.get(j).getY() <= 28 && polygon.get(j).getX() > -79.5 && polygon.get(j).getX() <= -70) ||
							(polygon.get(i).getY() <= 33 && polygon.get(i).getX() > -67 &&
							polygon.get(j).getY() <= 33 && polygon.get(j).getX() > -67)) {
						g1.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
					}
				}
			}
		}

		g2.setColor(new Color(255, 255, 255));
		g2.setStroke(bs);
		for (ArrayList<PointD> polygon : countyBorders) {
			for (int i = 0; i < polygon.size(); i++) {
				int j = i + 1;
				if (j == polygon.size())
					j = 0;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}
		for (ArrayList<PointD> polygon : caSubdBorders) {
			for (int i = 0; i < polygon.size(); i++) {
				int j = i + 1;
				if (j == polygon.size())
					j = 0;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}
		for (ArrayList<PointD> polygon : geoboundariesADM1) {
			for (int i = 0; i < polygon.size(); i++) {
				int j = i + 1;
				if (j == polygon.size())
					j = 0;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					if (!(((polygon.get(i).getY() > 24 && polygon.get(i).getY() <= 51 && polygon.get(i).getX() > -162 && polygon.get(i).getX() <= -59) &&
							(polygon.get(j).getY() > 24 && polygon.get(j).getY() <= 51 && polygon.get(j).getX() > -162 && polygon.get(j).getX() <= -59)))) {
						g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
					} else if((polygon.get(i).getY() <= 28 && polygon.get(i).getX() > -79.5 && polygon.get(i).getX() <= -70 &&
							polygon.get(j).getY() <= 28 && polygon.get(j).getX() > -79.5 && polygon.get(j).getX() <= -70) ||
							(polygon.get(i).getY() <= 33 && polygon.get(i).getX() > -67 &&
							polygon.get(j).getY() <= 33 && polygon.get(j).getX() > -67)) {
						g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
					}
				}
			}
		}

		g3.setColor(new Color(0, 0, 0));
		g3.setStroke(ts);
		for (ArrayList<PointD> polygon : interstates) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					g3.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		g3.setColor(new Color(128, 0, 0));
		g3.setStroke(bs);
		for (ArrayList<PointD> polygon : interstates) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					g3.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		g4.setColor(new Color(0, 0, 0));
		g4.setStroke(ts);
		for (ArrayList<PointD> polygon : majorRoads) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					g4.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		g4.setColor(new Color(128, 128, 255));
		g4.setStroke(bs);
		for (ArrayList<PointD> polygon : majorRoads) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getLon());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getLat());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getLon());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getLat());

				if (Math.abs(p1.getLon() - p2.getLon()) < 100) {
					g4.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		float[] scales = { 1f, 1f, 1f, 0.4f };
		float[] offsets = new float[4];
		RescaleOp rop = new RescaleOp(scales, offsets, null);
		float[] scales2 = { 1f, 1f, 1f, 0.3f };
		float[] offsets2 = new float[4];
		RescaleOp rop2 = new RescaleOp(scales2, offsets2, null);

		g.drawImage(roads, rop2, 0, 0);
		g.drawImage(highways, 0, 0, null);
		g.drawImage(counties, rop, 0, 0);
		g.drawImage(states, 0, 0, null);

		return basemap;
	}

	public static final Font CITY_FONT = new Font(Font.MONOSPACED, Font.BOLD, 18);
	public static final Font TOWN_FONT = new Font(Font.MONOSPACED, Font.BOLD, 12);

	private static BufferedImage generateCityPlot(double lat, double lon, SatelliteGeneratorSettings settings,
			RotateLatLonProjection plotProj) {
		BufferedImage citiesImg = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = citiesImg.createGraphics();

		double pixelsPerDegree = settings.getResolution() / settings.getSize();
		System.out.println("pixelsPerDegree: " + pixelsPerDegree);

		for (City c : cities) {
			String name = c.getName();
			double cLat = c.getLatitude();
			double cLon = c.getLongitude();
			int pop = c.getPopulation();
			double prm = c.getProminence();

			if (pixelsPerDegree < 25) {
				continue;
			}
			if (pixelsPerDegree < 100 && prm < 10) {
				continue;
			}
			if (pixelsPerDegree < 200 && prm < 5) {
				continue;
			}
			if (pixelsPerDegree < 300 && prm < 1) {
				continue;
			}
			if (pixelsPerDegree < 400 && prm < 0.75) {
				continue;
			}
			if (pixelsPerDegree < 600 && prm < 0.50) {
				continue;
			}
			if (pixelsPerDegree < 800 && prm < 0.25) {
				continue;
			}
			if (pixelsPerDegree < 1000 && prm < 0.1) {
				continue;
			}
			if (pixelsPerDegree < 1200 && prm < 0.05) {
				continue;
			}
			if (pixelsPerDegree < 1400 && prm < 0.025) {
				continue;
			}
			if (pixelsPerDegree < 2000 && prm < 0.001) {
				continue;
			}

			g2d.setFont((pop > 100000) ? CITY_FONT : TOWN_FONT);

			GeoCoord cityP = plotProj.rotateLatLon(cLon, cLat);

			PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
					(settings.getSize()));
			PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
					-(settings.getSize()));

			int cityX = (int) linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, citiesImg.getWidth(),
					cityP.getLon());
			int cityY = (int) linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, citiesImg.getHeight(),
					cityP.getLat());

//			System.out.println(name + "\t" + cLon + "\t" + cLat + "\t" + cityP + "\t" + cityX + "\t" + cityY);

			g2d.setColor(Color.BLACK);
			drawCenteredString(name, g2d, cityX + 0, cityY - 1);
			drawCenteredString(name, g2d, cityX - 1, cityY + 0);
			drawCenteredString(name, g2d, cityX + 1, cityY + 0);
			drawCenteredString(name, g2d, cityX + 0, cityY + 1);
			g2d.setColor(Color.WHITE);
			drawCenteredString(name, g2d, cityX, cityY);
		}

		return citiesImg;
	}

	private static BufferedImage generateSatellitePlot(GoesImage[] goes, DateTime time, double lat, double lon,
			SatelliteGeneratorSettings settings, GeostationaryProjection satProj, RotateLatLonProjection plotProj) {
		if(lat < -82 || lat > 82 || (lon > -1 && lon < 142)) {
			return null;
		}
		
		BufferedImage satPlot = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = satPlot.createGraphics();

		PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
				(settings.getSize()));
		PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
				-(settings.getSize()));
		
		DataField band13 = goes[4].field("rad");
		int[] band13Shape = band13.getShape();

		float[] lirX = goes[4].field("x").array1D();
		float[] lirY = goes[4].field("y").array1D();
		float lirDx = goes[4].dataFromField("dx");
		float lirDy = goes[4].dataFromField("dy");
		
		// chunk optimization
		long chunkStartTime = System.currentTimeMillis();
		final int CHUNK_SIZE = 25;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) lirX.length/CHUNK_SIZE)]
				[(int) Math.ceil((double) lirY.length/CHUNK_SIZE)];
		
		int plotWidth = satPlot.getWidth();
		int plotHeight = satPlot.getHeight();
		
		for(int i = 0; i < renderChunk.length; i++) {
			for(int j = 0; j < renderChunk[i].length; j++) {
				int i1Low = i * CHUNK_SIZE;
				int j1Low = j * CHUNK_SIZE;
				int i1High = (i + 1) * CHUNK_SIZE;
				int j1High = (j + 1) * CHUNK_SIZE;
				
				if(i1High >= lirX.length) {
					i1High = lirX.length - 1;
				}
				
				if(j1High >= lirY.length) {
					j1High = lirY.length - 1;
				}
				
				int i1Mid = (i1Low + i1High)/2;
				int j1Mid = (j1Low + j1High)/2;
				
				float x1 = -lirX[i1Low] - lirDx / 2.0f;
				float y1 = lirY[j1Low] - lirDy / 2.0f;
				float x2 = -lirX[i1High] + lirDx / 2.0f;
				float y2 = lirY[j1Low] - lirDy / 2.0f;
				float x3 = -lirX[i1High] + lirDx / 2.0f;
				float y3 = lirY[j1High] + lirDy / 2.0f;
				float x4 = -lirX[i1Low] - lirDx / 2.0f;
				float y4 = lirY[j1High] + lirDy / 2.0f;
				float x5 = -lirX[i1Mid];
				float y5 = lirY[j1Mid];

				GeoCoord latLon1 = satProj.projectXYToLatLon(x1, y1);
				GeoCoord latLon2 = satProj.projectXYToLatLon(x2, y2);
				GeoCoord latLon3 = satProj.projectXYToLatLon(x3, y3);
				GeoCoord latLon4 = satProj.projectXYToLatLon(x4, y4);
				GeoCoord latLon5 = satProj.projectXYToLatLon(x5, y5);
				
				boolean anyValid = !Float.isNaN(latLon1.getLat()) || !Float.isNaN(latLon2.getLat())
						|| !Float.isNaN(latLon3.getLat()) || !Float.isNaN(latLon4.getLat()) || !Float.isNaN(latLon5.getLat())
						|| !Float.isNaN(latLon1.getLon()) || !Float.isNaN(latLon2.getLon())
						|| !Float.isNaN(latLon3.getLon()) || !Float.isNaN(latLon4.getLon()) || !Float.isNaN(latLon5.getLon());
				
				if(anyValid) {
					GeoCoord p1 = plotProj.rotateLatLon(latLon1);
					GeoCoord p2 = plotProj.rotateLatLon(latLon2);
					GeoCoord p3 = plotProj.rotateLatLon(latLon3);
					GeoCoord p4 = plotProj.rotateLatLon(latLon4);
					GeoCoord p5 = plotProj.rotateLatLon(latLon5);

					double _x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p1.getLon());
					double _y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p1.getLat());
					double _x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p2.getLon());
					double _y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p2.getLat());
					double _x3 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p3.getLon());
					double _y3 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p3.getLat());
					double _x4 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p4.getLon());
					double _y4 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p4.getLat());
					double _x5 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p5.getLon());
					double _y5 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p5.getLat());
					
					if(_x1 > 0 && _x1 < plotWidth && _y1 > 0 && _y1 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x2 > 0 && _x2 < plotWidth && _y2 > 0 && _y2 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x3 > 0 && _x2 < plotWidth && _y3 > 0 && _y3 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x4 > 0 && _x4 < plotWidth && _y4 > 0 && _y4 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x5 > 0 && _x5 < plotWidth && _y5 > 0 && _y5 < plotHeight) {
						renderChunk[i][j] = true;
					} else {
						renderChunk[i][j] = false;
					}
				} else {
					renderChunk[i][j] = false;
				}
			}
		}
		long chunkEndTime = System.currentTimeMillis();
		System.out.println("chunk decision time: " + (chunkEndTime - chunkStartTime)/1000.0 + " s");

		Color[][] satColors = new Color[band13Shape[1]][band13Shape[0]];

		float[] x = new float[0];
		float[] y = new float[0];
		float dx = 0;
		float dy = 0;
		
//		int startI = 0;
//		int endI = band13Shape[1] - 1;
//		int startJ = 0;
//		int endJ = band13Shape[0] - 1;
//		
//		if(band13Shape[1] > 10000) {
//			endI = band13Shape[1]/2 - 1;
//		}

		System.out.println("image type: " + settings.getImageType());

		long colorProcessingStartTime = System.currentTimeMillis();
		int chunkSizeInBand = 0;
		switch (settings.getImageType()) {
		case GEOCOLOR:
			chunkSizeInBand = 100;
			
			satColors = GeocolorProcessing.createComposite(goes[0], goes[1], goes[2], goes[3], goes[4], satProj, time, renderChunk, chunkSizeInBand);

			x = goes[1].field("x").array1D();
			y = goes[1].field("y").array1D();
			dx = goes[1].dataFromField("dx");
			dy = goes[1].dataFromField("dy");

			break;
		case LONGWAVE_IR:
			ColorTable brTempColors = brightnessTemperatureColorTable;
			chunkSizeInBand = 25;

			for (int i = 0; i < satColors.length; i++) {
				for (int j = 0; j < satColors[i].length; j++) {
					if(renderChunk[i/chunkSizeInBand][j/chunkSizeInBand]) {
						double brTemp = WeatherUtils.brightnessTemperatureFromWavenumber(band13.getData(j, i) / 100000.0,
								WeatherUtils.wavelengthToWavenumber(goes[4].dataFromField("wavelength") / 1000000.0));
						satColors[i][j] = brTempColors.getColor(brTemp);
					}
				}
			}

			x = goes[4].field("x").array1D();
			y = goes[4].field("y").array1D();
			dx = goes[4].dataFromField("dx");
			dy = goes[4].dataFromField("dy");

			break;
		}
		long colorProcessingEndTime = System.currentTimeMillis();
		System.out.println("color processing time: " + (colorProcessingEndTime - colorProcessingStartTime)/1000.0 + " s");

//		System.out.println("x[0]: " + x[0]);
//		System.out.println("y[0]: " + y[0]);
//		System.out.println("x[1400]: " + x[1400]);
//		System.out.println("y[1400]: " + y[1400]);

//		GeoCoord testLL = satProj.projectXYToLatLon(-x[200] - dx / 2.0f, y[200] - dy / 2.0f);

//		System.out.println("testLL: " + testLL);
		
		long plottingStartTime = System.currentTimeMillis();
		for (int i = 0; i < satColors.length; i++) {
			for (int j = 0; j < satColors[0].length; j++) {
				if(renderChunk[i/chunkSizeInBand][j/chunkSizeInBand]) {
					float x0 = -x[i];
					float y0 = y[j];
	
					GeoCoord latLon1 = satProj.projectXYToLatLon(x0 - dx / 2.0f, y0 - dy / 2.0f);
					GeoCoord latLon2 = satProj.projectXYToLatLon(x0 + dx / 2.0f, y0 - dy / 2.0f);
					GeoCoord latLon3 = satProj.projectXYToLatLon(x0 + dx / 2.0f, y0 + dy / 2.0f);
					GeoCoord latLon4 = satProj.projectXYToLatLon(x0 - dx / 2.0f, y0 + dy / 2.0f);
	
					GeoCoord p1 = plotProj.rotateLatLon(latLon1);
					GeoCoord p2 = plotProj.rotateLatLon(latLon2);
					GeoCoord p3 = plotProj.rotateLatLon(latLon3);
					GeoCoord p4 = plotProj.rotateLatLon(latLon4);
	
					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p1.getLon());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p1.getLat());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p2.getLon());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p2.getLat());
					double x3 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p3.getLon());
					double y3 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p3.getLat());
					double x4 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p4.getLon());
					double y4 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p4.getLat());
	
	//				if()
	//				System.out.println("satellite polygon ll1: " + latLon1);
	//				System.out.println("satellite polygon p1: " + p1);
	
					int[] xPoints = new int[] { (int) x1, (int) x2, (int) x3, (int) x4 };
					int[] yPoints = new int[] { (int) y1, (int) y2, (int) y3, (int) y4 };
	
					boolean allValid = !Float.isNaN(latLon1.getLat()) && !Float.isNaN(latLon2.getLat())
							&& !Float.isNaN(latLon3.getLat()) && !Float.isNaN(latLon4.getLat())
							&& !Float.isNaN(latLon1.getLon()) && !Float.isNaN(latLon2.getLon())
							&& !Float.isNaN(latLon3.getLon()) && !Float.isNaN(latLon4.getLon()) && x1 != 0 && x2 != 0
							&& x3 != 0 && x4 != 0 && x1 != 0 && x2 != 0 && x3 != 0 && x4 != 0;
	
					if (allValid) {
						g.setColor(satColors[i][j]);
						g.fillPolygon(xPoints, yPoints, 4);
					}
				}
			}
		}
		long plottingEndTime = System.currentTimeMillis();
		System.out.println("plotting time: " + (plottingEndTime - plottingStartTime)/1000.0 + " s");

		return satPlot;
	}
	
	private static BufferedImage generateSatellitePlot(GoesMultibandImage goes, DateTime time, double lat, double lon,
			SatelliteGeneratorSettings settings, GeostationaryProjection satProj, RotateLatLonProjection plotProj) {System.out.println(lat < -82);
		if(lat < -82 || lat > 82 || (lon > -1 && lon < 142)) {
			return null;
		}
		
		BufferedImage satPlot = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = satPlot.createGraphics();

		PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
				(settings.getSize()));
		PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
				-(settings.getSize()));
		
		DataField band13 = goes.field("band_13");
		int[] band13Shape = band13.getShape();

		BufferedImage testPlot = new BufferedImage(band13Shape[1], band13Shape[0], BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = testPlot.createGraphics();
		
		float[] redX = goes.field("x").array1D();
		float[] redY = goes.field("y").array1D();
		float redDx = goes.dataFromField("dx");
		float redDy = goes.dataFromField("dy");
		
		// chunk optimization
//		long chunkStartTime = System.currentTimeMillis();
		final int CHUNK_SIZE = 25;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) redX.length/CHUNK_SIZE)]
				[(int) Math.ceil((double) redY.length/CHUNK_SIZE)];
		
		int plotWidth = satPlot.getWidth();
		int plotHeight = satPlot.getHeight();
		
		for(int i = 0; i < renderChunk.length; i++) {
			for(int j = 0; j < renderChunk[i].length; j++) {
				int i1Low = i * CHUNK_SIZE;
				int j1Low = j * CHUNK_SIZE;
				int i1High = (i + 1) * CHUNK_SIZE;
				int j1High = (j + 1) * CHUNK_SIZE;
				
				if(i1High >= redX.length) {
					i1High = redX.length - 1;
				}
				
				if(j1High >= redY.length) {
					j1High = redY.length - 1;
				}
				
				int i1Mid = (i1Low + i1High)/2;
				int j1Mid = (j1Low + j1High)/2;
				
				float x1 = -redX[i1Low] - redDx / 2.0f;
				float y1 = redY[j1Low] - redDy / 2.0f;
				float x2 = -redX[i1High] + redDx / 2.0f;
				float y2 = redY[j1Low] - redDy / 2.0f;
				float x3 = -redX[i1High] + redDx / 2.0f;
				float y3 = redY[j1High] + redDy / 2.0f;
				float x4 = -redX[i1Low] - redDx / 2.0f;
				float y4 = redY[j1High] + redDy / 2.0f;
				float x5 = -redX[i1Mid];
				float y5 = redY[j1Mid];

				GeoCoord latLon1 = satProj.projectXYToLatLon(x1, y1);
				GeoCoord latLon2 = satProj.projectXYToLatLon(x2, y2);
				GeoCoord latLon3 = satProj.projectXYToLatLon(x3, y3);
				GeoCoord latLon4 = satProj.projectXYToLatLon(x4, y4);
				GeoCoord latLon5 = satProj.projectXYToLatLon(x5, y5);
				
				boolean anyValid = !Float.isNaN(latLon1.getLat()) || !Float.isNaN(latLon2.getLat())
						|| !Float.isNaN(latLon3.getLat()) || !Float.isNaN(latLon4.getLat()) || !Float.isNaN(latLon5.getLat())
						|| !Float.isNaN(latLon1.getLon()) || !Float.isNaN(latLon2.getLon())
						|| !Float.isNaN(latLon3.getLon()) || !Float.isNaN(latLon4.getLon()) || !Float.isNaN(latLon5.getLon());
				
				if(anyValid) {
					GeoCoord p1 = plotProj.rotateLatLon(latLon1);
					GeoCoord p2 = plotProj.rotateLatLon(latLon2);
					GeoCoord p3 = plotProj.rotateLatLon(latLon3);
					GeoCoord p4 = plotProj.rotateLatLon(latLon4);
					GeoCoord p5 = plotProj.rotateLatLon(latLon5);

					double _x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p1.getLon());
					double _y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p1.getLat());
					double _x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p2.getLon());
					double _y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p2.getLat());
					double _x3 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p3.getLon());
					double _y3 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p3.getLat());
					double _x4 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p4.getLon());
					double _y4 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p4.getLat());
					double _x5 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p5.getLon());
					double _y5 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p5.getLat());
					
					if(_x1 > 0 && _x1 < plotWidth && _y1 > 0 && _y1 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x2 > 0 && _x2 < plotWidth && _y2 > 0 && _y2 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x3 > 0 && _x2 < plotWidth && _y3 > 0 && _y3 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x4 > 0 && _x4 < plotWidth && _y4 > 0 && _y4 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x5 > 0 && _x5 < plotWidth && _y5 > 0 && _y5 < plotHeight) {
						renderChunk[i][j] = true;
					} else {
						renderChunk[i][j] = false;
					}
				} else {
					renderChunk[i][j] = false;
				}
			}
		}
//		long chunkEndTime = System.currentTimeMillis();

		Color[][] satColors = new Color[band13Shape[1]][band13Shape[0]];

		float[] x = new float[0];
		float[] y = new float[0];
		float dx = 0;
		float dy = 0;
		
//		int startI = 0;
//		int endI = band13Shape[1] - 1;
//		int startJ = 0;
//		int endJ = band13Shape[0] - 1;
//		
//		if(band13Shape[1] > 10000) {
//			endI = band13Shape[1]/2 - 1;
//		}

		System.out.println("image type: " + settings.getImageType());

		long colorProcessingStartTime = System.currentTimeMillis();
		int chunkSizeInBand = 0;
		switch (settings.getImageType()) {
		case GEOCOLOR:
			chunkSizeInBand = 25;
			
			satColors = GeocolorProcessing.createComposite(goes, satProj, renderChunk, chunkSizeInBand);

			x = goes.field("x").array1D();
			y = goes.field("y").array1D();
			dx = goes.dataFromField("dx");
			dy = goes.dataFromField("dy");

			break;
		case LONGWAVE_IR:
			ColorTable brTempColors = brightnessTemperatureColorTable;
			chunkSizeInBand = 25;

			for (int i = 0; i < satColors.length; i++) {
				for (int j = 0; j < satColors[i].length; j++) {
					if(renderChunk[i/chunkSizeInBand][j/chunkSizeInBand]) {
						satColors[i][j] = brTempColors.getColor(band13.getData(j, i));
					}
				}
			}

			x = goes.field("x").array1D();
			y = goes.field("y").array1D();
			dx = goes.dataFromField("dx");
			dy = goes.dataFromField("dy");

			break;
		}
		long colorProcessingEndTime = System.currentTimeMillis();
		System.out.println("color processing time: " + (colorProcessingEndTime - colorProcessingStartTime)/1000.0 + " s");

		System.out.println("x[0]: " + x[0]);
		System.out.println("y[0]: " + y[0]);
		System.out.println("x[1400]: " + x[1400]);
		System.out.println("y[1400]: " + y[1400]);

		GeoCoord testLL = satProj.projectXYToLatLon(-x[200] - dx / 2.0f, y[200] - dy / 2.0f);

		System.out.println("testLL: " + testLL);
		
		long plottingStartTime = System.currentTimeMillis();
		for (int i = 0; i < satColors.length; i++) {
			for (int j = 0; j < satColors[0].length; j++) {
				if(renderChunk[i/chunkSizeInBand][j/chunkSizeInBand]) {
					float x0 = -x[i];
					float y0 = y[j];
	
					GeoCoord latLon1 = satProj.projectXYToLatLon(x0 - dx / 2.0f, y0 - dy / 2.0f);
					GeoCoord latLon2 = satProj.projectXYToLatLon(x0 + dx / 2.0f, y0 - dy / 2.0f);
					GeoCoord latLon3 = satProj.projectXYToLatLon(x0 + dx / 2.0f, y0 + dy / 2.0f);
					GeoCoord latLon4 = satProj.projectXYToLatLon(x0 - dx / 2.0f, y0 + dy / 2.0f);
	
					GeoCoord p1 = plotProj.rotateLatLon(latLon1);
					GeoCoord p2 = plotProj.rotateLatLon(latLon2);
					GeoCoord p3 = plotProj.rotateLatLon(latLon3);
					GeoCoord p4 = plotProj.rotateLatLon(latLon4);
	
					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p1.getLon());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p1.getLat());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p2.getLon());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p2.getLat());
					double x3 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p3.getLon());
					double y3 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p3.getLat());
					double x4 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p4.getLon());
					double y4 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p4.getLat());
	
	//				if()
	//				System.out.println("satellite polygon ll1: " + latLon1);
	//				System.out.println("satellite polygon p1: " + p1);
	
					int[] xPoints = new int[] { (int) x1, (int) x2, (int) x3, (int) x4 };
					int[] yPoints = new int[] { (int) y1, (int) y2, (int) y3, (int) y4 };
	
					boolean allValid = !Float.isNaN(latLon1.getLat()) && !Float.isNaN(latLon2.getLat())
							&& !Float.isNaN(latLon3.getLat()) && !Float.isNaN(latLon4.getLat())
							&& !Float.isNaN(latLon1.getLon()) && !Float.isNaN(latLon2.getLon())
							&& !Float.isNaN(latLon3.getLon()) && !Float.isNaN(latLon4.getLon()) && x1 != 0 && x2 != 0
							&& x3 != 0 && x4 != 0 && x1 != 0 && x2 != 0 && x3 != 0 && x4 != 0;
	
					if (allValid) {
						g.setColor(satColors[i][j]);
						g.fillPolygon(xPoints, yPoints, 4);
						g2.setColor(satColors[i][j]);
						g2.fillRect(i, j, 1, 1);
					}
				}
			}
		}
		long plottingEndTime = System.currentTimeMillis();
		System.out.println("plotting time: " + (plottingEndTime - plottingStartTime)/1000.0 + " s");
		
		try {
			ImageIO.write(testPlot, "PNG", new File("test-plot.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return satPlot;
	}

	private static BufferedImage generateSatellitePlot(GridsatImage gridsat, DateTime time, double lat, double lon,
			SatelliteGeneratorSettings settings, RotateLatLonProjection plotProj) {
		if(lat < -82 || lat > 82 || (lon > -1 && lon < 142)) {
			return null;
		}
		
		BufferedImage satPlot = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = satPlot.createGraphics();

		PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
				(settings.getSize()));
		PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
				-(settings.getSize()));

		DataField band13 = gridsat.field("vis");
		int[] band13Shape = band13.getShape();

		Color[][] satColors = new Color[band13Shape[2]][band13Shape[1]];

		float[][] data = null;
		float[] latArr = new float[0];
		float[] lonArr = new float[0];
		float dlat = 0;
		float dlon = 0;

		latArr = gridsat.field("lat").array1D();
		lonArr = gridsat.field("lon").array1D();
		dlat = gridsat.dataFromField("dlat");
		dlon = gridsat.dataFromField("dlon");
		
		final int CHUNK_SIZE = 25;
		boolean[][] renderChunk = new boolean[(int) Math.ceil((double) lonArr.length/CHUNK_SIZE)]
				[(int) Math.ceil((double) latArr.length/CHUNK_SIZE)];
		
		System.out.println("renderChunk shape: " + renderChunk.length + "\t" + renderChunk[0].length);
		
		int plotWidth = satPlot.getWidth();
		int plotHeight = satPlot.getHeight();
		
		for(int i = 0; i < renderChunk.length; i++) {
			for(int j = 0; j < renderChunk[i].length; j++) {
				int i1Low = i * CHUNK_SIZE;
				int j1Low = j * CHUNK_SIZE;
				int i1High = (i + 1) * CHUNK_SIZE;
				int j1High = (j + 1) * CHUNK_SIZE;
				
				if(i1High >= lonArr.length) {
					i1High = lonArr.length - 1;
				}
				
				if(j1High >= latArr.length) {
					j1High = latArr.length - 1;
				}
				
				int i1Mid = (i1Low + i1High)/2;
				int j1Mid = (j1Low + j1High)/2;

				float lat1 = latArr[j1Low];
				float lon1 = lonArr[i1Low];
				float lat2 = latArr[j1High];
				float lon2 = lonArr[i1Low];
				float lat3 = latArr[j1High];
				float lon3 = lonArr[i1High];
				float lat4 = latArr[j1Low];
				float lon4 = lonArr[i1High];
				float lat5 = latArr[j1Mid];
				float lon5 = lonArr[i1Mid];

				GeoCoord latLon1 = new GeoCoord(lat1 - dlat / 2.0f, lon1 - dlon / 2.0f);
				GeoCoord latLon2 = new GeoCoord(lat2 + dlat / 2.0f, lon2 - dlon / 2.0f);
				GeoCoord latLon3 = new GeoCoord(lat3 + dlat / 2.0f, lon3 + dlon / 2.0f);
				GeoCoord latLon4 = new GeoCoord(lat4 - dlat / 2.0f, lon4 + dlon / 2.0f);
				GeoCoord latLon5 = new GeoCoord(lat5, lon5);
				
				boolean anyValid = !Float.isNaN(latLon1.getLat()) || !Float.isNaN(latLon2.getLat())
						|| !Float.isNaN(latLon3.getLat()) || !Float.isNaN(latLon4.getLat()) || !Float.isNaN(latLon5.getLat())
						|| !Float.isNaN(latLon1.getLon()) || !Float.isNaN(latLon2.getLon())
						|| !Float.isNaN(latLon3.getLon()) || !Float.isNaN(latLon4.getLon()) || !Float.isNaN(latLon5.getLon());
				
				if(anyValid) {
					GeoCoord p1 = plotProj.rotateLatLon(latLon1);
					GeoCoord p2 = plotProj.rotateLatLon(latLon2);
					GeoCoord p3 = plotProj.rotateLatLon(latLon3);
					GeoCoord p4 = plotProj.rotateLatLon(latLon4);
					GeoCoord p5 = plotProj.rotateLatLon(latLon5);

					double _x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p1.getLon());
					double _y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p1.getLat());
					double _x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p2.getLon());
					double _y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p2.getLat());
					double _x3 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p3.getLon());
					double _y3 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p3.getLat());
					double _x4 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p4.getLon());
					double _y4 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p4.getLat());
					double _x5 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p5.getLon());
					double _y5 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p5.getLat());
					
					if(_x1 > 0 && _x1 < plotWidth && _y1 > 0 && _y1 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x2 > 0 && _x2 < plotWidth && _y2 > 0 && _y2 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x3 > 0 && _x2 < plotWidth && _y3 > 0 && _y3 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x4 > 0 && _x4 < plotWidth && _y4 > 0 && _y4 < plotHeight) {
						renderChunk[i][j] = true;
					} else if(_x5 > 0 && _x5 < plotWidth && _y5 > 0 && _y5 < plotHeight) {
						renderChunk[i][j] = true;
					} else {
						renderChunk[i][j] = false;
					}
				} else {
					renderChunk[i][j] = false;
				}
			}
		}

		System.out.println("image type: " + settings.getImageType());

		switch (settings.getImageType()) {
		case GEOCOLOR:
			data = gridsat.field("vis").array3D()[0];

			for (int i = 0; i < satColors.length; i++) {
				for (int j = 0; j < satColors[i].length; j++) {
					if(renderChunk[i/CHUNK_SIZE][j/CHUNK_SIZE]) {
						int gray = (int) (255 * data[j][i]);
	
						if(gray < 0) gray = 0;
						if(gray > 255) gray = 255;
						
	//					float alpha = 1.33f * data[j][i];
	//
	//					if(alpha < 0) alpha = 0;
	//					if(alpha > 1) alpha = 1;
	//					
	//					Color daytimeBackground = ModisBlueMarble.getColor(latArr[j], lonArr[i]);
	//					
	//					satColors[i][j] = new Color(
	//							(int) (alpha * 255 + (1 - alpha) * 0.5f * daytimeBackground.getRed()), 
	//							(int) (alpha * 255 + (1 - alpha) * 0.5f * daytimeBackground.getGreen()), 
	//							(int) (alpha * 255 + (1 - alpha) * 0.5f * daytimeBackground.getBlue()));
						
						satColors[i][j] = new Color(gray, gray, gray);
					}
				}
			}
			
			break;
		case LONGWAVE_IR:
			ColorTable brTempColors = brightnessTemperatureColorTable;
			data = gridsat.field("lir").array3D()[0];

			for (int i = 0; i < satColors.length; i++) {
				for (int j = 0; j < satColors[i].length; j++) {
					if(renderChunk[i/CHUNK_SIZE][j/CHUNK_SIZE]) {
						satColors[i][j] = brTempColors.getColor(data[j][i]);
					}
				}
			}

			break;
		}

		for (int i = 0; i < satColors.length; i++) {
			for (int j = 0; j < satColors[0].length; j++) {
				if(renderChunk[i/CHUNK_SIZE][j/CHUNK_SIZE]) {
					float lat0 = latArr[j];
					float lon0 = lonArr[i];
	
					GeoCoord latLon1 = new GeoCoord(lat0 + dlat / 2.0f, lon0 + dlon / 2.0f);
					GeoCoord latLon2 = new GeoCoord(lat0 - dlat / 2.0f, lon0 + dlon / 2.0f);
					GeoCoord latLon3 = new GeoCoord(lat0 - dlat / 2.0f, lon0 - dlon / 2.0f);
					GeoCoord latLon4 = new GeoCoord(lat0 + dlat / 2.0f, lon0 - dlon / 2.0f);
	
					GeoCoord p1 = plotProj.rotateLatLon(latLon1);
					GeoCoord p2 = plotProj.rotateLatLon(latLon2);
					GeoCoord p3 = plotProj.rotateLatLon(latLon3);
					GeoCoord p4 = plotProj.rotateLatLon(latLon4);
	
					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p1.getLon());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p1.getLat());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p2.getLon());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p2.getLat());
					double x3 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p3.getLon());
					double y3 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p3.getLat());
					double x4 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, satPlot.getWidth(),
							p4.getLon());
					double y4 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, satPlot.getHeight(),
							p4.getLat());
	
	//				if()
	//				System.out.println("satellite polygon ll1: " + latLon1);
	//				System.out.println("satellite polygon p1: " + p1);
	
					int[] xPoints = new int[] { (int) x1, (int) x2, (int) x3, (int) x4 };
					int[] yPoints = new int[] { (int) y1, (int) y2, (int) y3, (int) y4 };
	
					boolean allValid = !Float.isNaN(latLon1.getLat()) && !Float.isNaN(latLon2.getLat())
							&& !Float.isNaN(latLon3.getLat()) && !Float.isNaN(latLon4.getLat())
							&& !Float.isNaN(latLon1.getLon()) && !Float.isNaN(latLon2.getLon())
							&& !Float.isNaN(latLon3.getLon()) && !Float.isNaN(latLon4.getLon()) && x1 != 0 && x2 != 0
							&& x3 != 0 && x4 != 0 && x1 != 0 && x2 != 0 && x3 != 0 && x4 != 0;
					
					if (allValid) {
						g.setColor(satColors[i][j]);
						g.fillPolygon(xPoints, yPoints, 4);
					}
				}
			}
		}

		return satPlot;
	}

	@SuppressWarnings("unused")
	private static BufferedImage generateWarningPlot(DateTime time, double lat, double lon,
			SatelliteGeneratorSettings settings, RotateLatLonProjection plotProj) throws IOException {
		BufferedImage warningPlot = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = warningPlot.createGraphics();

//		BasicStroke bs = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
//		BasicStroke ts = new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
//		BasicStroke ets = new BasicStroke(16, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		BasicStroke bs = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke ts = new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke ets = new BasicStroke(11, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		ArrayList<WarningPolygon> warnings = null;
		try {
			WarningArchive wa = new WarningArchive("satellite-image-generator-temp/");
			warnings = wa.getWarnings(time.minusHours(2), time.plusHours(2));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("returning blank for warning plot!");
			return warningPlot;
		}

		PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
				(settings.getSize()));
		PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
				-(settings.getSize()));

		for (WarningPolygon warning : warnings) {
			if (warning.isActive(time)) {
				// extra thick outlines
				g.setStroke(ts);
				switch (warning.getWarningType()) {
				case DUST_STORM_WARNING:
					g.setColor(new Color(0, 0, 0, 0));
					break;
				case FLASH_FLOOD:
					if (warning.getDamageTag() == DamageTag.DESTRUCTIVE) {
						g.setColor(new Color(0, 0, 0));
					} else {
						g.setColor(new Color(0, 0, 0, 0));
					}
					break;
				case SEVERE_THUNDERSTORM:
					if (warning.getDamageTag() == DamageTag.DESTRUCTIVE) {
						g.setColor(new Color(0, 0, 0));
					} else if (warning.getTornadoTag() == TornadoTag.POSSIBLE) {
						g.setColor(new Color(0, 0, 0));
					} else {
						g.setColor(new Color(0, 0, 0, 0));
					}
					break;
				case SNOW_SQUALL_WARNING:
					g.setColor(new Color(0, 0, 0, 0));
					break;
				case SPECIAL_MARINE:
					g.setColor(new Color(0, 0, 0, 0));
					break;
				case TORNADO:
					if (warning.getDamageTag() == DamageTag.CONSIDERABLE) {
						g.setColor(new Color(0, 0, 0));
					} else {
						g.setColor(new Color(0, 0, 0, 0));
					}
					break;
				default:
					g.setColor(new Color(0, 0, 0, 0));
					break;
				}
				g.setStroke(ets);

				ArrayList<PointF> polygonF = warning.getPoints();
				ArrayList<PointD> polygon = new ArrayList<>();

				for (int i = 0; i < polygonF.size(); i++) {
					polygon.add(new PointD((double) polygonF.get(i).getX(), (double) polygonF.get(i).getY()));
//					logger.println("polygonF conversion: " + polygon.get(polygon.size() - 1), DebugLoggerLevel.BRIEF);
				}

				for (int i = 0; i < polygon.size(); i++) {
					int j = i + 1;
					if (j == polygon.size())
						j = 0;

					GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
					GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p1.getLon());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p1.getLat());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p2.getLon());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p2.getLat());

					g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
				// outlines
				g.setStroke(ts);
				switch (warning.getWarningType()) {
				case DUST_STORM_WARNING:
					g.setColor(new Color(0, 0, 0));
					break;
				case FLASH_FLOOD:
					if (warning.getDamageTag() == DamageTag.DESTRUCTIVE) {
						g.setColor(new Color(255, 0, 255));
					} else {
						g.setColor(new Color(0, 0, 0));
					}
					break;
				case SEVERE_THUNDERSTORM:
					if (warning.getDamageTag() == DamageTag.DESTRUCTIVE) {
						g.setColor(new Color(128, 0, 128));
					} else if (warning.getTornadoTag() == TornadoTag.POSSIBLE) {
						g.setColor(new Color(255, 0, 64));
					} else {
						g.setColor(new Color(0, 0, 0));
					}
					break;
				case SNOW_SQUALL_WARNING:
					g.setColor(new Color(0, 0, 0));
					break;
				case SPECIAL_MARINE:
					g.setColor(new Color(0, 0, 0));
					break;
				case TORNADO:
					if (warning.getDamageTag() == DamageTag.CONSIDERABLE) {
						g.setColor(new Color(128, 0, 0));
					} else {
						g.setColor(new Color(0, 0, 0));
					}
					break;
				default:
					g.setColor(new Color(0, 0, 0, 0));
					break;
				}
				g.setStroke(ts);

				for (int i = 0; i < polygon.size(); i++) {
					int j = i + 1;
					if (j == polygon.size())
						j = 0;

					GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
					GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p1.getLon());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p1.getLat());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p2.getLon());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p2.getLat());

					g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}

				switch (warning.getWarningType()) {
				case DUST_STORM_WARNING:
					g.setColor(new Color(239, 192, 144));
					break;
				case FLASH_FLOOD:
					g.setColor(new Color(0, 255, 0));
					break;
				case SEVERE_THUNDERSTORM:
					g.setColor(new Color(255, 255, 0));
					break;
				case SNOW_SQUALL_WARNING:
					g.setColor(new Color(0, 255, 255));
					break;
				case SPECIAL_MARINE:
					g.setColor(new Color(255, 128, 0));
					break;
				case TORNADO:
					if (warning.getDamageTag() == DamageTag.CATASTROPHIC) {
						g.setColor(new Color(255, 0, 255));
					} else {
						g.setColor(new Color(255, 0, 0));
					}
					break;
				default:
					g.setColor(new Color(0, 0, 0, 0));
					break;
				}
				g.setStroke(bs);

				for (int i = 0; i < polygon.size(); i++) {
					int j = i + 1;
					if (j == polygon.size())
						j = 0;

					GeoCoord p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
					GeoCoord p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p1.getLon());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p1.getLat());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p2.getLon());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p2.getLat());

					g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		return warningPlot;
	}

	private static final Font NOTICE_FONT = new Font(Font.MONOSPACED, Font.BOLD + Font.ITALIC, 36);
	private static BufferedImage noDataAvailableNotice(SatelliteGeneratorSettings settings) {
		BufferedImage noticePlot = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = noticePlot.createGraphics();
		g.setFont(NOTICE_FONT);
		
		int centerX = noticePlot.getWidth() / 2;
		int centerY = noticePlot.getHeight() / 2;
		
		g.setColor(new Color(220, 20, 60));
		drawCenteredOutlinedString("DATA IS NOT AVAILABLE.", g, centerX, centerY);
		
		g.dispose();
		
		return noticePlot;
	}

	private static final int TIME_TOLERANCE = 20; // minutes
	private static final DateTime GRIDSAT_GOES_16_OPERATIONAL_CUTOFF = new DateTime(2017, 3, 1, 0, 0, DateTimeZone.UTC);
	private static final DateTime GRIDSAT_END = new DateTime(2018, 1, 1, 0, 0, DateTimeZone.UTC);
	private static final DateTime GOES_17_OPERATIONAL_START = new DateTime(2018, 8, 28, 0, 0, DateTimeZone.UTC);
	private static final DateTime GOES_17_18_OPERATIONAL_CUTOFF = new DateTime(2023, 1, 3, 0, 0, DateTimeZone.UTC);
	private static final DateTime GOES_16_19_OPERATIONAL_CUTOFF = new DateTime(2025, 4, 7, 15, 0, DateTimeZone.UTC);

	private static File[] getGoesData(DateTime time, SatelliteImageType imageType, SatelliteSource source, SatelliteSector sector)
			throws NoValidSatelliteScansFoundException {
		// bypasses download, uses data on hard drive
//		if(1 > 0) {
//			return new File[] {
//					new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/satellite-image-generator-temp2/sat_band1.nc"),
//					new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/satellite-image-generator-temp2/sat_band2.nc"),
//					new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/satellite-image-generator-temp2/sat_band3.nc"),
//					new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/satellite-image-generator-temp2/sat_band7.nc"),
//					new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/satellite-image-generator-temp2/sat_band13.nc"),
//			};
//		}
		
		ArrayList<String> validBand1Files = new ArrayList<>();
		ArrayList<String> validBand2Files = new ArrayList<>();
		ArrayList<String> validBand3Files = new ArrayList<>();
		ArrayList<String> validBand7Files = new ArrayList<>();
		ArrayList<String> validBand13Files = new ArrayList<>();

		DateTime prevUtcHour = time.minusHours(1);

		List<String> satFilesCurr = new ArrayList<>();
		List<String> satFilesPrev = new ArrayList<>();

		if (source == SatelliteSource.GOES_EAST) {
			if (time.isBefore(GOES_16_19_OPERATIONAL_CUTOFF)) {
				System.out.println("Downloading GOES-16 data...");
				satFilesCurr = GoesAws.goes16Level1Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes16Level1Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			} else {
				System.out.println("Downloading GOES-19 data...");
				satFilesCurr = GoesAws.goes19Level1Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes19Level1Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			}
		} else if (source == SatelliteSource.GOES_WEST) {
			if (time.isBefore(GOES_17_OPERATIONAL_START)) {
				System.out.println("Downloading GOES-16 data...");
				satFilesCurr = GoesAws.goes16Level1Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes16Level1Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			} else if (time.isBefore(GOES_17_18_OPERATIONAL_CUTOFF)) {
				System.out.println("Downloading GOES-17 data...");
				satFilesCurr = GoesAws.goes17Level1Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes17Level1Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			} else {
				System.out.println("Downloading GOES-18 data...");
				satFilesCurr = GoesAws.goes18Level1Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes18Level1Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			}
		}

		List<String> satFiles = new ArrayList<>();
		satFiles.addAll(satFilesPrev);
		satFiles.addAll(satFilesCurr);

		System.out.println("satFiles.size(): " + satFiles.size());

		for (String str : satFiles) {
			System.out.println("satFile: " + str);
		}

		ArrayList<String> band1FilesWithinTolerance = new ArrayList<>();
		ArrayList<String> band2FilesWithinTolerance = new ArrayList<>();
		ArrayList<String> band3FilesWithinTolerance = new ArrayList<>();
		ArrayList<String> band7FilesWithinTolerance = new ArrayList<>();
		ArrayList<String> band13FilesWithinTolerance = new ArrayList<>();
		for (int j = 0; j < satFiles.size(); j++) {
			String[] awsPath = satFiles.get(j).split("/");
			String filename = awsPath[awsPath.length - 1];

			System.out.println("satFile name: " + filename);

			int dayOfYear = Integer.valueOf(filename.substring(31, 34));

			DateTime fileTimestamp = new DateTime(Integer.valueOf(filename.substring(27, 31)), 1, 1,
					Integer.valueOf(filename.substring(34, 36)), Integer.valueOf(filename.substring(36, 38)),
					Integer.valueOf(filename.substring(38, 40)), DateTimeZone.UTC);

			fileTimestamp = fileTimestamp.dayOfYear().setCopy(dayOfYear);

			System.out.println("fileTimestamp: " + fileTimestamp);

			fileTimestamp.dayOfYear().setCopy(dayOfYear);

			int band = Integer.valueOf(filename.substring(19, 21));

			if (time.minusMinutes(TIME_TOLERANCE).isBefore(fileTimestamp)
					&& time.plusMinutes(TIME_TOLERANCE).isAfter(fileTimestamp)) {
				logger.println("valid file added: " + filename, DebugLoggerLevel.VERBOSE);

				switch (band) {
				case 1:
					band1FilesWithinTolerance.add(satFiles.get(j));
					break;
				case 2:
					band2FilesWithinTolerance.add(satFiles.get(j));
					break;
				case 3:
					band3FilesWithinTolerance.add(satFiles.get(j));
					break;
				case 7:
					band7FilesWithinTolerance.add(satFiles.get(j));
					break;
				case 13:
					band13FilesWithinTolerance.add(satFiles.get(j));
					break;
				}
			}
		}

		if (band1FilesWithinTolerance.size() > 0) {
			validBand1Files = band1FilesWithinTolerance;
		}
		if (band2FilesWithinTolerance.size() > 0) {
			validBand2Files = band2FilesWithinTolerance;
		}
		if (band3FilesWithinTolerance.size() > 0) {
			validBand3Files = band3FilesWithinTolerance;
		}
		if (band7FilesWithinTolerance.size() > 0) {
			validBand7Files = band7FilesWithinTolerance;
		}
		if (band13FilesWithinTolerance.size() > 0) {
			validBand13Files = band13FilesWithinTolerance;
		}

		if (band1FilesWithinTolerance.size() == 0) {
			throw new NoValidSatelliteScansFoundException();
		}
		if (band2FilesWithinTolerance.size() == 0) {
			throw new NoValidSatelliteScansFoundException();
		}
		if (band3FilesWithinTolerance.size() == 0) {
			throw new NoValidSatelliteScansFoundException();
		}
		if (band7FilesWithinTolerance.size() == 0) {
			throw new NoValidSatelliteScansFoundException();
		}
		if (band13FilesWithinTolerance.size() == 0) {
			throw new NoValidSatelliteScansFoundException();
		}

		// file selections
		String mostRecentBand1File = validBand1Files.get(0);
		for (int i = 1; i < validBand1Files.size(); i++) {
			String[] awsPath = validBand1Files.get(i).split("/");
			String filename = awsPath[awsPath.length - 1];

//			System.out.println(filename);

			int year = Integer.valueOf(filename.substring(27, 31));
			int dayOfYear = Integer.valueOf(filename.substring(31, 34));
			int hour = Integer.valueOf(filename.substring(34, 36));
			int minute = Integer.valueOf(filename.substring(36, 38));
			int second = Integer.valueOf(filename.substring(38, 40));

//			System.out.println(year + "\t" + dayOfYear + "\t" + hour + "\t" + minute + "\t" + second);

			DateTime fileTimestamp = new DateTime(year, 1, 1, hour, minute, second, DateTimeZone.UTC);

			fileTimestamp = fileTimestamp.dayOfYear().setCopy(dayOfYear);

			if (fileTimestamp.isBefore(time)) {
				mostRecentBand1File = validBand1Files.get(i);
			} else {
				break;
			}
		}
		String mostRecentBand2File = validBand1Files.get(0);
		for (int i = 1; i < validBand2Files.size(); i++) {
			String[] awsPath = validBand2Files.get(i).split("/");
			String filename = awsPath[awsPath.length - 1];

			int year = Integer.valueOf(filename.substring(27, 31));
			int dayOfYear = Integer.valueOf(filename.substring(31, 34));
			int hour = Integer.valueOf(filename.substring(34, 36));
			int minute = Integer.valueOf(filename.substring(36, 38));
			int second = Integer.valueOf(filename.substring(38, 40));

//			System.out.println(year + "\t" + dayOfYear + "\t" + hour + "\t" + minute + "\t" + second);

			DateTime fileTimestamp = new DateTime(year, 1, 1, hour, minute, second, DateTimeZone.UTC);

			fileTimestamp = fileTimestamp.dayOfYear().setCopy(dayOfYear);

			if (fileTimestamp.isBefore(time)) {
				mostRecentBand2File = validBand2Files.get(i);
			} else {
				break;
			}
		}
		String mostRecentBand3File = validBand1Files.get(0);
		for (int i = 1; i < validBand3Files.size(); i++) {
			String[] awsPath = validBand3Files.get(i).split("/");
			String filename = awsPath[awsPath.length - 1];

			int year = Integer.valueOf(filename.substring(27, 31));
			int dayOfYear = Integer.valueOf(filename.substring(31, 34));
			int hour = Integer.valueOf(filename.substring(34, 36));
			int minute = Integer.valueOf(filename.substring(36, 38));
			int second = Integer.valueOf(filename.substring(38, 40));

//			System.out.println(year + "\t" + dayOfYear + "\t" + hour + "\t" + minute + "\t" + second);

			DateTime fileTimestamp = new DateTime(year, 1, 1, hour, minute, second, DateTimeZone.UTC);

			fileTimestamp = fileTimestamp.dayOfYear().setCopy(dayOfYear);

			if (fileTimestamp.isBefore(time)) {
				mostRecentBand3File = validBand3Files.get(i);
			} else {
				break;
			}
		}
		String mostRecentBand7File = validBand1Files.get(0);
		for (int i = 1; i < validBand7Files.size(); i++) {
			String[] awsPath = validBand7Files.get(i).split("/");
			String filename = awsPath[awsPath.length - 1];

			int year = Integer.valueOf(filename.substring(27, 31));
			int dayOfYear = Integer.valueOf(filename.substring(31, 34));
			int hour = Integer.valueOf(filename.substring(34, 36));
			int minute = Integer.valueOf(filename.substring(36, 38));
			int second = Integer.valueOf(filename.substring(38, 40));

//			System.out.println(year + "\t" + dayOfYear + "\t" + hour + "\t" + minute + "\t" + second);

			DateTime fileTimestamp = new DateTime(year, 1, 1, hour, minute, second, DateTimeZone.UTC);

			fileTimestamp = fileTimestamp.dayOfYear().setCopy(dayOfYear);

			if (fileTimestamp.isBefore(time)) {
				mostRecentBand7File = validBand7Files.get(i);
			} else {
				break;
			}
		}
		String mostRecentBand13File = validBand1Files.get(0);
		for (int i = 1; i < validBand13Files.size(); i++) {
			String[] awsPath = validBand13Files.get(i).split("/");
			String filename = awsPath[awsPath.length - 1];

			int year = Integer.valueOf(filename.substring(27, 31));
			int dayOfYear = Integer.valueOf(filename.substring(31, 34));
			int hour = Integer.valueOf(filename.substring(34, 36));
			int minute = Integer.valueOf(filename.substring(36, 38));
			int second = Integer.valueOf(filename.substring(38, 40));

//			System.out.println(year + "\t" + dayOfYear + "\t" + hour + "\t" + minute + "\t" + second);

			DateTime fileTimestamp = new DateTime(year, 1, 1, hour, minute, second, DateTimeZone.UTC);

			fileTimestamp = fileTimestamp.dayOfYear().setCopy(dayOfYear);

			if (fileTimestamp.isBefore(time)) {
				mostRecentBand13File = validBand13Files.get(i);
			} else {
				break;
			}
		}

		logger.println("chose file: " + mostRecentBand1File, DebugLoggerLevel.BRIEF);

		try {
			logger.println("try returning file: " + mostRecentBand1File, DebugLoggerLevel.BRIEF);

			if(imageType == SatelliteImageType.GEOCOLOR) {
				File band1File = downloadFile(mostRecentBand1File, "sat_band1.nc");
				File band2File = downloadFile(mostRecentBand2File, "sat_band2.nc");
				File band3File = downloadFile(mostRecentBand3File, "sat_band3.nc");
				File band7File = downloadFile(mostRecentBand7File, "sat_band7.nc");
				File band13File = downloadFile(mostRecentBand13File, "sat_band13.nc");
		
				logger.println("returning file: " + mostRecentBand1File, DebugLoggerLevel.BRIEF);
		
				return new File[] { band1File, band2File, band3File, band7File, band13File };
			} else {
//				File band1File = downloadFile(mostRecentBand1File, "sat_band1.nc");
//				File band2File = downloadFile(mostRecentBand2File, "sat_band2.nc");
//				File band3File = downloadFile(mostRecentBand3File, "sat_band3.nc");
//				File band7File = downloadFile(mostRecentBand7File, "sat_band7.nc");
				File band13File = downloadFile(mostRecentBand13File, "sat_band13.nc");
		
				logger.println("returning file: " + mostRecentBand1File, DebugLoggerLevel.BRIEF);
		
				return new File[] { null, null, null, null, band13File };
			}
		} catch (IOException e) {
			return null;
		}
	}

	private static File getGoesMultibandData(DateTime time, SatelliteSource source, SatelliteSector sector)
			throws NoValidSatelliteScansFoundException {
		// bypasses download, uses data on hard drive
		if(1 > 0) {
			return new File("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/sat_multiband.nc");
		}
		
		ArrayList<String> validFiles = new ArrayList<>();

		DateTime prevUtcHour = time.minusHours(1);

		List<String> satFilesCurr = new ArrayList<>();
		List<String> satFilesPrev = new ArrayList<>();

		if (source == SatelliteSource.GOES_EAST) {
			if (time.isBefore(GOES_16_19_OPERATIONAL_CUTOFF)) {
				System.out.println("Downloading GOES-16 data...");
				satFilesCurr = GoesAws.goes16Level2Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes16Level2Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			} else {
				System.out.println("Downloading GOES-19 data...");
				satFilesCurr = GoesAws.goes19Level2Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes19Level2Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			}
		} else if (source == SatelliteSource.GOES_WEST) {
			if (time.isBefore(GOES_17_OPERATIONAL_START)) {
				System.out.println("Downloading GOES-16 data...");
				satFilesCurr = GoesAws.goes16Level2Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes16Level2Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			} else if (time.isBefore(GOES_17_18_OPERATIONAL_CUTOFF)) {
				System.out.println("Downloading GOES-17 data...");
				satFilesCurr = GoesAws.goes17Level2Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes17Level2Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			} else {
				System.out.println("Downloading GOES-18 data...");
				satFilesCurr = GoesAws.goes18Level2Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay(), sector);
				satFilesPrev = GoesAws.goes18Level2Files(prevUtcHour.getYear(), prevUtcHour.getMonthOfYear(),
						prevUtcHour.getDayOfMonth(), prevUtcHour.getHourOfDay(), sector);
			}
		}

		List<String> satFiles = new ArrayList<>();
		satFiles.addAll(satFilesPrev);
		satFiles.addAll(satFilesCurr);

		System.out.println("satFiles.size(): " + satFiles.size());

		for (String str : satFiles) {
			System.out.println("satFile: " + str);
		}

		ArrayList<String> filesWithinTolerance = new ArrayList<>();
		for (int j = 0; j < satFiles.size(); j++) {
			String[] awsPath = satFiles.get(j).split("/");
			String filename = awsPath[awsPath.length - 1];

			System.out.println("satFile name: " + filename);

			int dayOfYear = Integer.valueOf(filename.substring(29, 32));

			DateTime fileTimestamp = new DateTime(Integer.valueOf(filename.substring(25, 29)), 1, 1,
					Integer.valueOf(filename.substring(32, 34)), Integer.valueOf(filename.substring(34, 36)),
					Integer.valueOf(filename.substring(36, 38)), DateTimeZone.UTC);
			fileTimestamp = fileTimestamp.dayOfYear().setCopy(dayOfYear);

			System.out.println("fileTimestamp: " + fileTimestamp);

			fileTimestamp.dayOfYear().setCopy(dayOfYear);

			if (time.minusMinutes(TIME_TOLERANCE).isBefore(fileTimestamp)
					&& time.plusMinutes(TIME_TOLERANCE).isAfter(fileTimestamp)) {
				logger.println("valid file added: " + filename, DebugLoggerLevel.VERBOSE);
				filesWithinTolerance.add(satFiles.get(j));
			}
		}

		if (filesWithinTolerance.size() > 0) {
			validFiles = filesWithinTolerance;
		}

		if (filesWithinTolerance.size() == 0) {
			throw new NoValidSatelliteScansFoundException();
		}

		// file selections
		String mostRecentFile = validFiles.get(0);
		for (int i = 1; i < validFiles.size(); i++) {
			String[] awsPath = validFiles.get(i).split("/");
			String filename = awsPath[awsPath.length - 1];

//			System.out.println(filename);

			int year = Integer.valueOf(filename.substring(25, 29));
			int dayOfYear = Integer.valueOf(filename.substring(29, 32));
			int hour = Integer.valueOf(filename.substring(32, 34));
			int minute = Integer.valueOf(filename.substring(34, 36));
			int second = Integer.valueOf(filename.substring(36, 38));

//			System.out.println(year + "\t" + dayOfYear + "\t" + hour + "\t" + minute + "\t" + second);

			DateTime fileTimestamp = new DateTime(year, 1, 1, hour, minute, second, DateTimeZone.UTC);

			fileTimestamp = fileTimestamp.dayOfYear().setCopy(dayOfYear);

			if (fileTimestamp.isBefore(time)) {
				mostRecentFile = validFiles.get(i);
			} else {
				break;
			}
		}

		logger.println("chose file: " + mostRecentFile, DebugLoggerLevel.BRIEF);

		try {
			logger.println("try returning file: " + mostRecentFile, DebugLoggerLevel.BRIEF);

			File l2File = downloadFile(mostRecentFile, "sat_multiband.nc");

			logger.println("returning file: " + mostRecentFile, DebugLoggerLevel.BRIEF);

			return l2File;
		} catch (IOException e) {
			return null;
		}
	}
	
	private static final int[] goesEastSats = new int[] {8, 12, 13};
	private static final int[] goesWestSats = new int[] {9, 10, 11, 15};
	private static File getGridsatData(DateTime time, float lat, float lon)
			throws NoValidSatelliteScansFoundException {
		try {
//			String gridsatUrl = "https://www.ncei.noaa.gov/data/gridsat-goes/access/conus/2016/04/GridSat-CONUS.goes13.2016.04.11.2300.v01.nc";
//			File gridsatFile = downloadFile(gridsatUrl, "gridsat.nc");
			
			String sector = "goes";
			
			if(lat > 24 && lat <= 50 && lon > -125 && lon <= -73) {
				sector = "conus";
			}
			
			String webFolderUrl = String.format("https://www.ncei.noaa.gov/data/gridsat-goes/access/%s/%04d/%02d/",
					sector, time.getYear(), time.getMonthOfYear());
			
			ArrayList<String> gridsatFiles = listWebDirectory(webFolderUrl);
			
			HashMap<Integer, ArrayList<String>> gridsatFilesBySatId = new HashMap<>();
			
			int noConusMod = ("conus".equals(sector) ? 0 : 1);
			
			for(String filename : gridsatFiles) {
				int satId = Integer.valueOf(filename.substring(18 - noConusMod, 20 - noConusMod));
				
				if(!gridsatFilesBySatId.containsKey(satId)) {
					gridsatFilesBySatId.put(satId, new ArrayList<>());
				}
				
				gridsatFilesBySatId.get(satId).add(filename);
			}
			
			System.out.println("Files per satellite:");
			for(Integer satId : gridsatFilesBySatId.keySet()) {
				System.out.println("\t" + satId + "\t" + gridsatFilesBySatId.get(satId).size());
			}
			
			System.out.println("gridsatFilesBySatId.keySet(): " + Arrays.toString(gridsatFilesBySatId.keySet().toArray()));

			HashMap<Integer, String> mostRecentFileBySatId = new HashMap<>();
			for(Integer satId : gridsatFilesBySatId.keySet()) {
				DateTime mostRecentTimestamp = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeZone.UTC);
				String mostRecentFilename = "";
				
				for(int i = 0; i < gridsatFilesBySatId.get(satId).size(); i++) {
					String filename = gridsatFilesBySatId.get(satId).get(i);
					
					int year = Integer.valueOf(filename.substring(21 - noConusMod, 25 - noConusMod));
					int month = Integer.valueOf(filename.substring(26 - noConusMod, 28 - noConusMod));
					int day = Integer.valueOf(filename.substring(29 - noConusMod, 31 - noConusMod));
					int hour = Integer.valueOf(filename.substring(32 - noConusMod, 34 - noConusMod));
					int minute = Integer.valueOf(filename.substring(34 - noConusMod, 36 - noConusMod));
					int second = 0;

//					System.out.println(year + "\t" + month + "\t" + hour + "\t" + minute + "\t" + second);

					DateTime fileTimestamp = new DateTime(year, month, day, hour, minute, second, DateTimeZone.UTC);
//					System.out.println(filename);
//					System.out.println(fileTimestamp);

					if (fileTimestamp.isBefore(time) && fileTimestamp.isAfter(mostRecentTimestamp)) {
						mostRecentFilename = filename;
						mostRecentTimestamp = fileTimestamp;
					} else {
//						break;
					}
				}

				mostRecentFileBySatId.put(satId, mostRecentFilename);
			}
			
			System.out.println("Most recent file per satellite:");
			for(Integer satId : mostRecentFileBySatId.keySet()) {
				System.out.println("\t" + satId + "\t" + mostRecentFileBySatId.get(satId));
			}
			
			// construct satellite selector
			// make list of historical goes-east/goes-west satellites and pick
			// the most recent goes satellite for its orbital slot with a valid
			// file in mostRecentFileBySatId
			
			String selectedFile = "";
			if(lon > -106) {
				for(int i = 0; i < goesEastSats.length; i++) {
					if(mostRecentFileBySatId.containsKey(goesEastSats[i])) {
						if(mostRecentFileBySatId.get(goesEastSats[i]).length() != 0) {
							selectedFile = mostRecentFileBySatId.get(goesEastSats[i]);
						} else {
							continue;
						}
					}
				}

				for(int i = 0; i < goesWestSats.length; i++) {
					if(mostRecentFileBySatId.containsKey(goesWestSats[i])) {
						if(mostRecentFileBySatId.get(goesWestSats[i]).length() != 0) {
							selectedFile = mostRecentFileBySatId.get(goesWestSats[i]);
						} else {
							continue;
						}
					}
				}
			} else {
				for(int i = 0; i < goesWestSats.length; i++) {
					if(mostRecentFileBySatId.containsKey(goesWestSats[i])) {
						if(mostRecentFileBySatId.get(goesWestSats[i]).length() != 0) {
							selectedFile = mostRecentFileBySatId.get(goesWestSats[i]);
						} else {
							continue;
						}
					}
				}

				for(int i = 0; i < goesEastSats.length; i++) {
					if(mostRecentFileBySatId.containsKey(goesEastSats[i])) {
						if(mostRecentFileBySatId.get(goesEastSats[i]).length() != 0) {
							selectedFile = mostRecentFileBySatId.get(goesEastSats[i]);
						} else {
							continue;
						}
					}
				}
			}
			
			if(selectedFile.length() != 0) {
				File gridsatFile = downloadFile(webFolderUrl + selectedFile, "gridsat.nc");
				
				return gridsatFile;
			} else {
				throw new NoValidSatelliteScansFoundException();
			}
		} catch (IOException e) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static PointD rotateAroundCenter(PointD p, double w, double h, double phi) {
		return rotateAroundCenter(p.getX(), p.getY(), w, h, phi);
	}

	private static PointD rotateAroundCenter(double x, double y, double w, double h, double phi) {
		double xPrime = x - w / 2;
		double yPrime = y - h / 2;

		double xPrimeRotated = Math.cos(phi) * xPrime - Math.sin(phi) * yPrime;
		double yPrimeRotated = Math.sin(phi) * xPrime + Math.cos(phi) * yPrime;

		double xRotate = xPrimeRotated + w / 2;
		double yRotate = yPrimeRotated + h / 2;

		return new PointD(xRotate, yRotate);
	}

	@SuppressWarnings("unused")
	private static PointD rotateAboutPoint(PointD p, double x0, double y0, double phi) {
		return rotateAboutPoint(p.getX(), p.getY(), x0, y0, phi);
	}

	private static PointD rotateAboutPoint(double x, double y, double x0, double y0, double phi) {
		double xPrime = x - x0;
		double yPrime = y - y0;

		double xPrimeRotated = Math.cos(phi) * xPrime - Math.sin(phi) * yPrime;
		double yPrimeRotated = Math.sin(phi) * xPrime + Math.cos(phi) * yPrime;

		double xRotate = xPrimeRotated + x0;
		double yRotate = yPrimeRotated + y0;

		return new PointD(xRotate, yRotate);
	}

	public static void drawCenteredString(String s, Graphics2D g, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		int ht = fm.getAscent() + fm.getDescent();
		int width = fm.stringWidth(s);
		g.drawString(s, x - width / 2, y + (fm.getAscent() - ht / 2));
	}

	public static void drawOutlinedString(String s, Graphics2D g, int x, int y, Color c) {
		g.setColor(Color.BLACK);
		g.drawString(s, x - 1, y - 1);
		g.drawString(s, x - 1, y);
		g.drawString(s, x - 1, y + 1);
		g.drawString(s, x, y - 1);
		g.drawString(s, x, y + 1);
		g.drawString(s, x + 1, y - 1);
		g.drawString(s, x + 1, y);
		g.drawString(s, x + 1, y + 1);

		g.setColor(c);
		g.drawString(s, x, y);
	}

	public static void drawCenteredOutlinedString(String s, Graphics2D g, int x, int y) {
		Color c = g.getColor();
		
		g.setColor(Color.BLACK);
		drawCenteredString(s, g, x - 1, y - 1);
		drawCenteredString(s, g, x - 1, y);
		drawCenteredString(s, g, x - 1, y + 1);
		drawCenteredString(s, g, x, y - 1);
		drawCenteredString(s, g, x, y + 1);
		drawCenteredString(s, g, x + 1, y - 1);
		drawCenteredString(s, g, x + 1, y);
		drawCenteredString(s, g, x + 1, y + 1);

		g.setColor(c);
		drawCenteredString(s, g, x, y);
	}

	private static ArrayList<ArrayList<PointD>> getPolygons(File kml) {

		Pattern p = Pattern.compile("<coordinates>.*?</coordinates>");

		Matcher m = p.matcher(usingBufferedReader(kml));

		ArrayList<String> coordList = new ArrayList<>();

		while (m.find()) {
			// System.out.println(m.start() + " " + m.end() + " " + m.group().substring(13,
			// m.group().length() - 14));
			coordList.add(m.group().substring(13, m.group().length() - 14));
		}

		ArrayList<ArrayList<PointD>> polygons = new ArrayList<>();

		for (String coords : coordList) {
			Scanner sc = new Scanner(coords);
			sc.useDelimiter(" ");

			ArrayList<PointD> polygon = new ArrayList<>();

			while (sc.hasNext()) {
				String s = sc.next();
				// System.out.println(s);

				String[] pp = s.split(",");

				if (pp.length >= 2 && pp[0].length() > 0 && pp[1].length() > 0) {
				} else
					continue;

				polygon.add(new PointD(Double.valueOf(pp[0]), Double.valueOf(pp[1])));
			}

			sc.close();
			polygons.add(polygon);
		}

		return polygons;
	}

	private static ArrayList<ArrayList<PointD>> getPolygons(File poly, File meta) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");

		// creates a CSV parser
		CsvParser parser = new CsvParser(settings);

		// parses all rows in one go.
		List<String[]> polyRows = parser.parseAll(poly);
		List<String[]> metaRows = parser.parseAll(meta);

		ArrayList<ArrayList<PointD>> polygons = new ArrayList<>();

		ArrayList<PointD> polygon = new ArrayList<>();
		int polygonId = 0;
		int polygonSize = Integer.valueOf(metaRows.get(polygonId)[0]);

		for (int i = 0; i < polyRows.size(); i++) {
			if (polygon.size() >= polygonSize) {
				polygonId++;
				polygonSize = Integer.valueOf(metaRows.get(polygonId)[0]);

				polygons.add(polygon);
				polygon = new ArrayList<>();
			}

			String[] row = polyRows.get(i);
			PointD point = new PointD(Float.valueOf(row[0]), Float.valueOf(row[1]));
			polygon.add(point);

//			System.out.printf("%6d\t%6d\t%6d\t%6d\t" + poly.getName() + "\n", i, polyRows.size(), polygon.size(), polygonSize);
		}

		return polygons;
	}

	private static String usingBufferedReader(File filePath) {
		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				contentBuilder.append(sCurrentLine).append(" ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
	}

	private static double linScale(double preMin, double preMax, double postMin, double postMax, double value) {
		double slope = (postMax - postMin) / (preMax - preMin);

		return slope * (value - preMin) + postMin;
	}

	private static final String dataFolder = "satellite-image-generator-temp/";

	private static File downloadFile(String url, String fileName) throws IOException {
//		System.out.println("Downloading from: " + url);
		URL dataURL = new URL(url);

		File dataDir = new File(dataFolder);
//		System.out.println("Creating Directory: " + dataFolder);
		dataDir.mkdirs();
		InputStream is = dataURL.openStream();
		
		int kbDownloaded = 0;

//		System.out.println("Output File: " + dataFolder + fileName);
		OutputStream os = new FileOutputStream(dataFolder + fileName);
		byte[] buffer = new byte[16 * 1024];
		int transferredBytes = is.read(buffer);
		while (transferredBytes > -1) {
			os.write(buffer, 0, transferredBytes);
			// System.out.println("Transferred "+transferredBytes+" for "+fileName);
			transferredBytes = is.read(buffer);
			kbDownloaded += 16;
			
			if((kbDownloaded/1024.0)%8 == 0) {
//				System.out.printf("%d MB downloaded... (%d)\n", (kbDownloaded/1024), kbDownloaded);
			}
		}
		is.close();
		os.close();

		return new File(dataFolder + fileName);
	}

	private static ArrayList<String> listWebDirectory(String url) throws IOException {
		ArrayList<String> filesInDirectory = new ArrayList<>();
		
		File webDirectoryFile = downloadFile(url, "gridsatDirectory.xml");
		
		String webDirectory = usingBufferedReader(webDirectoryFile);
		
		Pattern p = Pattern.compile("(?<=<a href=\")GridSat.*?(?=\">)");
		Matcher m = p.matcher(webDirectory);
		
		while(m.find()) {
			String link = m.group();
			
			filesInDirectory.add(link);
		}
		
		return filesInDirectory;
	}
	
	/**
	 * 
	 * @param arr
	 * @param startI (inclusive)
	 * @param endI (inclusive)
	 * @return
	 */
	@SuppressWarnings("unused")
	private static float[] subsetArray1D(float[] arr, int startI, int endI) {
		float[] subset = new float[endI + 1 - startI];
		
		for(int i = 0; i < subset.length; i++) {
			subset[i] = arr[startI + i];
		}
		
		return subset;
	}
	
	/**
	 * 
	 * @param arr
	 * @param startI (inclusive)
	 * @param endI (inclusive)
	 * @param startJ (inclusive)
	 * @param endJ (inclusive)
	 * @return
	 */
	@SuppressWarnings("unused")
	private static float[][] subsetArray2D(float[][] arr, int startI, int endI, int startJ, int endJ) {
		float[][] subset = new float[endI + 1 - startI][endJ + 1 - startJ];
		
		for(int i = 0; i < subset.length; i++) {
			for(int j = 0; j < subset[i].length; j++) {
				subset[i][j] = arr[startI + i][startJ + j];
			}
		}
		
		return subset;
	}

	public static File unzipGz(File gz) {
		byte[] buffer = new byte[1024];

		String fullFilename = gz.getAbsolutePath();

		try {
			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fullFilename));
			FileOutputStream out = new FileOutputStream(fullFilename.substring(0, fullFilename.length() - 3));

			int len;
			while ((len = gzip.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

			gzip.close();
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return new File(fullFilename.substring(0, fullFilename.length() - 3));
	}

	private static void loadCities() {
		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");

		// creates a CSV parser
		CsvParser parser = new CsvParser(settings);

		// parses all rows in one go.
		List<String[]> allRows = parser.parseAll(RadarImageGenerator.loadResourceAsFile("res/worldCities.csv"));

		for (int i = 0; i < allRows.size(); i++) {
			String[] row = allRows.get(i);

			City city = new City(row[0], Double.valueOf(row[1]), Double.valueOf(row[2]), Integer.valueOf(row[3]));

			city.setProminence(Double.valueOf(row[4]));

			// code to calculate city prominence
//			double closestLargerCityDis = 10000;
//			int closestLargerCityPop = 0;
//			if(cities == null) {
//				city.setProminence(100);
//			} else {
//				for(City c : cities) {
//					double cityDis = Math.hypot(city.getLatitude() - c.getLatitude(), city.getLongitude() - c.getLongitude());
//					
//					if(cityDis < closestLargerCityDis) {
//						closestLargerCityDis = cityDis;
//						closestLargerCityPop = c.getPopulation();
//					}
//				}
//				
//				double prominence = (double) city.getPopulation()*Math.log10(city.getPopulation())/closestLargerCityPop * closestLargerCityDis;
//				
//				// special handling for weird okc and norman prominence ratings
//				if(city.getLatitude() > 35 && city.getLatitude() < 36 && city.getLongitude() > -98 && city.getLongitude() < -97) {
//					if("Norman".equals(city.getName()) || "Oklahoma City".equals(city.getName())) {
//						prominence *= 2;
//					}
//				}
//				
//				city.setProminence(prominence);
//			}

			cities.add(city);
		}

		Collections.reverse(cities);
	}
}
