package com.chasearchive.radarImageCli;

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
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.wxArchives.PointF;
import com.ameliaWx.wxArchives.earthWeather.iemWarnings.DamageTag;
import com.ameliaWx.wxArchives.earthWeather.iemWarnings.TornadoTag;
import com.ameliaWx.wxArchives.earthWeather.iemWarnings.WarningArchive;
import com.ameliaWx.wxArchives.earthWeather.iemWarnings.WarningPolygon;
import com.ameliaWx.wxArchives.earthWeather.nexrad.NexradAws;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import ucar.nc2.NetcdfFile;

public class RadarImageGenerator {
	public static ArrayList<City> cities;
	static {
		cities = new ArrayList<>();
		loadCities();
		loadRadarSites();
	}

	public static BufferedImage generateRadar(DateTime time, double lat, double lon, RadarGeneratorSettings settings)
			throws IOException {
		RotateLatLonProjection plotProj = new RotateLatLonProjection(lat, lon, 111.32, 111.32, 1000, 1000);

		BufferedImage basemap = generateBasemap(lat, lon, settings, plotProj);

		BufferedImage radarPlot = null;

		if (settings.getSource() == Source.NEXRAD) {
			File radarFile = null;
			try {
				radarFile = getClosestRadarData(time, lat, lon);
			} catch (NoValidRadarScansFoundException e) {
				e.printStackTrace();
			}

			if (radarFile != null) {				
				if (radarFile.getAbsolutePath().endsWith(".gz")) {
	
					File radarFileUnzipped = unzipGz(radarFile);
	
					radarFile = radarFileUnzipped;
				}
	
				RadarScan radarScan = new RadarScan(radarFile);
				
				if(radarScan.radarLat == 0) {
					String station = radarFile.getName().substring(6, 10);
					PointD stationCoords = radarSiteMap.get(station).getSiteCoords();
					
					radarScan.radarLat = stationCoords.getX();
					radarScan.radarLon = stationCoords.getY();
				}
	
				radarPlot = generateRadarPlot(radarScan, time, lat, lon, settings, plotProj);
			} else {
				radarPlot = null;
			}
		} else if (settings.getSource() == Source.MRMS) {
			radarPlot = generateRadarMosaicPlot(time, lat, lon, settings, plotProj);
		}

		BufferedImage warningPlot = generateWarningPlot(time, lat, lon, settings, plotProj);
		BufferedImage citiesPlot = generateCityPlot(lat, lon, settings, plotProj);

		BufferedImage logo = ImageIO.read(loadResourceAsFile("res/chase-archive-logo-256pix.png"));

		BufferedImage plot = new BufferedImage(basemap.getWidth(), basemap.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = plot.createGraphics();

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, basemap.getWidth(), basemap.getHeight());

		if(radarPlot != null) {
			g.drawImage(radarPlot, 0, 0, null);
		}
		g.drawImage(basemap, 0, 0, null);
		if(radarPlot != null) {
			g.drawImage(citiesPlot, 0, 0, null);
			g.drawImage(warningPlot, 0, 0, null);
		}
		g.drawImage(logo, 0, 0, null);
		if(radarPlot == null) {
			g.drawImage(noDataAvailableNotice(settings), 0, 0, null);
		}

//		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
//		g.setColor(Color.WHITE);
//		g.drawString("Ⓒ Chase Archive ", 3, basemap.getHeight() - 45);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
		g.setColor(Color.BLACK);
		g.drawString(dateStringAlt(time), 2, basemap.getHeight() - 14);
		g.drawString(dateStringAlt(time), 4, basemap.getHeight() - 16);
		g.drawString(dateStringAlt(time), 2, basemap.getHeight() - 16);
		g.drawString(dateStringAlt(time), 4, basemap.getHeight() - 14);
		g.setColor(Color.WHITE);
		g.drawString(dateStringAlt(time), 3, basemap.getHeight() - 15);
//		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
//		g.setColor(Color.LIGHT_GRAY);
//		g.drawString("chasearchive.com ", 63, basemap.getHeight() - 15);

		g.dispose();

		return plot;
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

	private static final ColorTable reflectivityColorTable = new ColorTable(
			loadResourceAsFile("res/awips-ii-official-mod-low-filter-2.pal"), 0.1f, 10, "dBZ");
	private static final ColorTable velocityColorTable = new ColorTable(loadResourceAsFile("res/SRRadarLoopsVlcy.pal"),
			0.1f, 10, "mph");

	private static BufferedImage generateBasemap(double lat, double lon, RadarGeneratorSettings settings,
			RotateLatLonProjection plotProj) throws IOException {
		ArrayList<ArrayList<PointD>> countyBorders;
		ArrayList<ArrayList<PointD>> stateBorders;
		ArrayList<ArrayList<PointD>> interstates;
		ArrayList<ArrayList<PointD>> majorRoads;

		File countyBordersKML = loadResourceAsFile("res/usCounties.kml");
		File stateBordersKML = loadResourceAsFile("res/usStates.kml");
		File interstatesPoly = loadResourceAsFile("res/primary-roads.poly");
		File interstatesPolyMeta = loadResourceAsFile("res/primary-roads.poly.meta");
		File majorRoadsPoly = loadResourceAsFile("res/prisec-roads.poly");
		File majorRoadsPolyMeta = loadResourceAsFile("res/prisec-roads.poly.meta");

		countyBorders = getPolygons(countyBordersKML);
		stateBorders = getPolygons(stateBordersKML);
		interstates = getPolygons(interstatesPoly, interstatesPolyMeta);
		majorRoads = getPolygons(majorRoadsPoly, majorRoadsPolyMeta);

		PointD latLonProjected = plotProj.rotateLatLon(lon, lat);
		System.out.println("latLonProjected (need to zero this out): " + latLonProjected);
		PointD latLonProjected1 = plotProj.rotateLatLon(lon, lat + 10);
		System.out.println("latLonProjected (need to 10 this out): " + latLonProjected1);
		PointD latLonProjected2 = plotProj.rotateLatLon(lon + 10, lat);
		System.out.println("latLonProjected (need to ??? this out): " + latLonProjected2);
		PointD trueNorthPointer = plotProj.rotateLatLon(lon, lat + 0.01); // i love finite differencing

		double trueNorth_dx = trueNorthPointer.getX() - latLonProjected.getX();
		double trueNorth_dy = trueNorthPointer.getY() - latLonProjected.getY();

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

				PointD p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				PointD p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getY());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getX());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getY());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getX());

				if (Math.abs(p1.getY() - p2.getY()) < 100) {
					g1.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
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

				PointD p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				PointD p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getY());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getX());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getY());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getX());

				if (Math.abs(p1.getY() - p2.getY()) < 100) {
					g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		g3.setColor(new Color(0, 0, 0));
		g3.setStroke(ts);
		for (ArrayList<PointD> polygon : interstates) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				PointD p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				PointD p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getY());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getX());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getY());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getX());

				if (Math.abs(p1.getY() - p2.getY()) < 100) {
					g3.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		g3.setColor(new Color(128, 0, 0));
		g3.setStroke(bs);
		for (ArrayList<PointD> polygon : interstates) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				PointD p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				PointD p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getY());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getX());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getY());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getX());

				if (Math.abs(p1.getY() - p2.getY()) < 100) {
					g3.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		g4.setColor(new Color(0, 0, 0));
		g4.setStroke(ts);
		for (ArrayList<PointD> polygon : majorRoads) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				PointD p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				PointD p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getY());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getX());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getY());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getX());

				if (Math.abs(p1.getY() - p2.getY()) < 100) {
					g4.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		g4.setColor(new Color(128, 128, 255));
		g4.setStroke(bs);
		for (ArrayList<PointD> polygon : majorRoads) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				PointD p1 = plotProj.rotateLatLon(polygon.get(i).getX(), polygon.get(i).getY());
				PointD p2 = plotProj.rotateLatLon(polygon.get(j).getX(), polygon.get(j).getY());

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getY());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getX());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getY());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getX());

				if (Math.abs(p1.getY() - p2.getY()) < 100) {
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

	private static BufferedImage generateCityPlot(double lat, double lon, RadarGeneratorSettings settings,
			RotateLatLonProjection plotProj) {
		BufferedImage citiesImg = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = citiesImg.createGraphics();

		final Font CITY_FONT = new Font(Font.MONOSPACED, Font.BOLD, 18);
		final Font TOWN_FONT = new Font(Font.MONOSPACED, Font.BOLD, 12);
//		final Font CITY_FONT = new Font(Font.MONOSPACED, Font.BOLD, (int) (18 * ((double) settings.getSize()/1080.0)));
//		final Font TOWN_FONT = new Font(Font.MONOSPACED, Font.BOLD, (int) (12 * ((double) settings.getSize()/1080.0)));

		double pixelsPerDegree = settings.getResolution() / settings.getSize();

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

			PointD cityP = plotProj.rotateLatLon(cLon, cLat);

			PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
					(settings.getSize()));
			PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
					-(settings.getSize()));

			int cityX = (int) linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, citiesImg.getWidth(),
					cityP.getY());
			int cityY = (int) linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, citiesImg.getHeight(),
					cityP.getX());

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

	private static BufferedImage generateRadarPlot(RadarScan scan, DateTime time, double lat, double lon,
			RadarGeneratorSettings settings, RotateLatLonProjection plotProj) {
//		PointD latLonProjected = plotProj.rotateLatLon(lon, lat);

		double radarLat = scan.radarLat;
		double radarLon = scan.radarLon;

		PointD radarProjected = plotProj.rotateLatLon(radarLon, radarLat);
		PointD radarNorthPointer = plotProj.rotateLatLon(radarLon, radarLat + 0.01); // i love finite differencing

		System.out.println("radarProjected: " + radarProjected);
		System.out.println("radarNorthPointer: " + radarNorthPointer);

		double rTrueNorth_dx = radarNorthPointer.getX() - radarProjected.getX();
		double rTrueNorth_dy = radarNorthPointer.getY() - radarProjected.getY();
		System.out.println("rTrueNorth_dx: " + rTrueNorth_dx);
		System.out.println("rTrueNorth_dy: " + rTrueNorth_dy);
		double rotationAngleRadar = Math.atan2(rTrueNorth_dy, rTrueNorth_dx);

		BufferedImage radarPlot = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = radarPlot.createGraphics();

		PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
				(settings.getSize()));
		PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
				-(settings.getSize()));

		float[][] data = null;
		ColorTable colorTable = null;

		switch (settings.getMoment()) {
		case REFLECTIVITY:
			data = scan.getReflectivity(time, false);
			colorTable = reflectivityColorTable;
			break;
		case VELOCITY:
			data = scan.getRadialVelocity(time, false);
			colorTable = velocityColorTable;
			break;
		default:
			return radarPlot;
		}

		System.out.println("radar projected: " + radarProjected);
		System.out.println("rotationAngleRadar: " + rotationAngleRadar);

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				PointD radarPolygon1 = new PointD(
						radarProjected.getY() + (1.0 / plotProj.dx) * (scan.coneOfSilence + scan.dr * (j - 0.5))
								* Math.sin(rotationAngleRadar + Math.toRadians(scan.da * (i - 0.5))),
						radarProjected.getX() + (1.0 / plotProj.dy) * (scan.coneOfSilence + scan.dr * (j - 0.5))
								* Math.cos(rotationAngleRadar + Math.toRadians(scan.da * (i - 0.5))));
				PointD radarPolygon2 = new PointD(
						radarProjected.getY() + (1.0 / plotProj.dx) * (scan.coneOfSilence + scan.dr * (j - 0.5))
								* Math.sin(rotationAngleRadar + Math.toRadians(scan.da * (i + 0.5))),
						radarProjected.getX() + (1.0 / plotProj.dy) * (scan.coneOfSilence + scan.dr * (j - 0.5))
								* Math.cos(rotationAngleRadar + Math.toRadians(scan.da * (i + 0.5))));
				PointD radarPolygon3 = new PointD(
						radarProjected.getY() + (1.0 / plotProj.dx) * (scan.coneOfSilence + scan.dr * (j + 0.5))
								* Math.sin(rotationAngleRadar + Math.toRadians(scan.da * (i + 0.5))),
						radarProjected.getX() + (1.0 / plotProj.dy) * (scan.coneOfSilence + scan.dr * (j + 0.5))
								* Math.cos(rotationAngleRadar + Math.toRadians(scan.da * (i + 0.5))));
				PointD radarPolygon4 = new PointD(
						radarProjected.getY() + (1.0 / plotProj.dx) * (scan.coneOfSilence + scan.dr * (j + 0.5))
								* Math.sin(rotationAngleRadar + Math.toRadians(scan.da * (i - 0.5))),
						radarProjected.getX() + (1.0 / plotProj.dy) * (scan.coneOfSilence + scan.dr * (j + 0.5))
								* Math.cos(rotationAngleRadar + Math.toRadians(scan.da * (i - 0.5))));

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
						radarPolygon1.getX());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
						radarPolygon1.getY());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
						radarPolygon2.getX());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
						radarPolygon2.getY());
				double x3 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
						radarPolygon3.getX());
				double y3 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
						radarPolygon3.getY());
				double x4 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
						radarPolygon4.getX());
				double y4 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
						radarPolygon4.getY());

				double value = data[i][j];
				Color color = colorTable.getColor(value);

				int[] xPoints = { (int) Math.round(x1), (int) Math.round(x2), (int) Math.round(x3),
						(int) Math.round(x4) };
				int[] yPoints = { (int) Math.round(y1), (int) Math.round(y2), (int) Math.round(y3),
						(int) Math.round(y4) };

				boolean anyInsideImage = false;
				for (int k = 0; k < 4; k++) {
					int x = xPoints[k];
					int y = yPoints[k];

					if (x >= 0 && x < radarPlot.getWidth() && y >= 0 && y < radarPlot.getHeight()) {
						anyInsideImage = true;
						break;
					}
				}

				if (anyInsideImage) {
					g.setColor(color);
					g.fillPolygon(xPoints, yPoints, 4);
				}
			}
		}

		return radarPlot;
	}

	private static DateTime GRIDRAD_OP_START = new DateTime(1995, 1, 1, 0, 0, 0, DateTimeZone.UTC);
	private static DateTime GRIDRAD_OP_END = new DateTime(2017, 12, 31, 23, 0, 1, DateTimeZone.UTC);
	private static DateTime MRMS_OP_START = new DateTime(2020, 10, 14, 21, 14, 0, DateTimeZone.UTC);

	private static BufferedImage generateRadarMosaicPlot(DateTime time, double lat, double lon,
			RadarGeneratorSettings settings, RotateLatLonProjection plotProj) throws IOException {
		if(!time.isBefore(GRIDRAD_OP_START) && !time.isAfter(GRIDRAD_OP_END)) {
			return generateGridradPlot(time, lat,  lon, settings, plotProj);
		} else if(time.isAfter(GRIDRAD_OP_END) && !time.isAfter(MRMS_OP_START)) {
			return null;
		} else if(!time.isBefore(MRMS_OP_START)) {
			return generateMrmsPlot(time, lat,  lon, settings, plotProj);
		} else {
			return null;
		}
	}

	private static BufferedImage generateGridradPlot(DateTime time, double lat, double lon,
			RadarGeneratorSettings settings, RotateLatLonProjection plotProj) throws IOException {
//		PointD latLonProjected = plotProj.rotateLatLon(lon, lat);

		BufferedImage radarPlot = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = radarPlot.createGraphics();

		PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
				(settings.getSize()));
		PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
				-(settings.getSize()));

		String gridradUrl = String.format(
				"https://data.rda.ucar.edu/d841000/%04d%02d/nexrad_3d_v3_1_%04d%02d%02dT%02d%02d%02dZ.nc",
				time.getYear(), time.getMonthOfYear(), time.getYear(), time.getMonthOfYear(),
				time.getDayOfMonth(), time.getHourOfDay(), 0, 0);
		File gridradFile = downloadFile(gridradUrl, "gridrad.composite");
//		File gridradFile = unzipGz(gridradGz);
		GridradScan gridrad = new GridradScan(gridradFile);

		float[][] data = null;
		ColorTable colorTable = null;

		switch (settings.getMoment()) {
		case REFLECTIVITY:
			data = gridrad.refl1km;
			colorTable = reflectivityColorTable;
			break;
		default:
			return radarPlot;
		}

		System.out.println("gridrad.lat.length: " + gridrad.lat.length);
		System.out.println("gridrad.lon.length: " + gridrad.lon.length);
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				float qLat = gridrad.lat[gridrad.lat.length - 1 - i];
				float qLon = gridrad.lon[j];

				float lat1 = qLat - gridrad.dLat / 2.0f;
				float lat2 = qLat + gridrad.dLat / 2.0f;
				float lon1 = qLon - gridrad.dLat / 2.0f;
				float lon2 = qLon + gridrad.dLat / 2.0f;

				PointD p1P = plotProj.rotateLatLon(lon1, lat1);
				PointD p2P = plotProj.rotateLatLon(lon1, lat2);
				PointD p3P = plotProj.rotateLatLon(lon2, lat2);
				PointD p4P = plotProj.rotateLatLon(lon2, lat1);

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
						p1P.getY());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
						p1P.getX());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
						p2P.getY());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
						p2P.getX());
				double x3 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
						p3P.getY());
				double y3 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
						p3P.getX());
				double x4 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
						p4P.getY());
				double y4 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
						p4P.getX());

				double value = data[i][j];
				Color color = colorTable.getColor(value);

				int[] xPoints = { (int) Math.round(x1), (int) Math.round(x2), (int) Math.round(x3),
						(int) Math.round(x4) };
				int[] yPoints = { (int) Math.round(y1), (int) Math.round(y2), (int) Math.round(y3),
						(int) Math.round(y4) };

				boolean anyInsideImage = false;
				for (int k = 0; k < 4; k++) {
					int x = xPoints[k];
					int y = yPoints[k];

					if (x >= 0 && x < radarPlot.getWidth() && y >= 0 && y < radarPlot.getHeight()) {
						anyInsideImage = true;
						break;
					}
				}

				if (anyInsideImage) {
					g.setColor(color);
					g.fillPolygon(xPoints, yPoints, 4);
				}
			}
		}
		
//		BufferedImage gridradTest = new BufferedImage(data[0].length, data.length, BufferedImage.TYPE_3BYTE_BGR);
//		Graphics2D g2 = gridradTest.createGraphics();
//		
//		for(int i = 0; i < gridradTest.getWidth(); i++) {
//			for(int j = 0; j < gridradTest.getHeight(); j++) {
//				Color c = colorTable.getColor(data[j][i]);
//				
//				System.out.println("data[j][i]::color - " + data[j][i] + "::" + c);
//				
//				g2.setColor(c);
//				g2.fillRect(i, j, 1, 1);
//			}
//		}
//		
//		ImageIO.write(gridradTest, "PNG", new File("gridrad-test.png"));

		return radarPlot;
	}

	private static BufferedImage generateMrmsPlot(DateTime time, double lat, double lon,
			RadarGeneratorSettings settings, RotateLatLonProjection plotProj) throws IOException {
//		PointD latLonProjected = plotProj.rotateLatLon(lon, lat);

		BufferedImage radarPlot = new BufferedImage((int) (settings.getResolution() * settings.getAspectRatioFloat()),
				(int) settings.getResolution(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = radarPlot.createGraphics();

		PointD latLonProjectedUL = new PointD(-(settings.getSize() * settings.getAspectRatioFloat()),
				(settings.getSize()));
		PointD latLonProjectedDR = new PointD((settings.getSize() * settings.getAspectRatioFloat()),
				-(settings.getSize()));

		if (!time.isBefore(MRMS_OP_START)) {
			String mrmsUrl = String.format(
					"https://noaa-mrms-pds.s3.amazonaws.com/CONUS/SeamlessHSR_00.00/%04d%02d%02d/MRMS_SeamlessHSR_00.00_%04d%02d%02d-%02d%02d%02d.grib2.gz",
					time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(), time.getYear(), time.getMonthOfYear(),
					time.getDayOfMonth(), time.getHourOfDay(), time.getMinuteOfHour() - (time.getMinuteOfHour() % 2),
					0);
			File mrmsGz = downloadFile(mrmsUrl, "mrms.composite");
			File mrmsFile = unzipGz(mrmsGz);
			MrmsComposite mrms = new MrmsComposite(mrmsFile);

			float[][] data = null;
			ColorTable colorTable = null;

			switch (settings.getMoment()) {
			case REFLECTIVITY:
				data = mrms.reflAtLowestAltitude[0][0];
				colorTable = reflectivityColorTable;
				break;
			default:
				return radarPlot;
			}

			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					float qLat = mrms.lat[mrms.lat.length - 1 - i];
					float qLon = mrms.lon[j];

					float lat1 = qLat - mrms.dLat / 2.0f;
					float lat2 = qLat + mrms.dLat / 2.0f;
					float lon1 = qLon - mrms.dLat / 2.0f;
					float lon2 = qLon + mrms.dLat / 2.0f;

					PointD p1P = plotProj.rotateLatLon(lon1, lat1);
					PointD p2P = plotProj.rotateLatLon(lon1, lat2);
					PointD p3P = plotProj.rotateLatLon(lon2, lat2);
					PointD p4P = plotProj.rotateLatLon(lon2, lat1);

					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
							p1P.getY());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
							p1P.getX());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
							p2P.getY());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
							p2P.getX());
					double x3 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
							p3P.getY());
					double y3 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
							p3P.getX());
					double x4 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, radarPlot.getWidth(),
							p4P.getY());
					double y4 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, radarPlot.getHeight(),
							p4P.getX());

					double value = data[i][j];
					Color color = colorTable.getColor(value);

					int[] xPoints = { (int) Math.round(x1), (int) Math.round(x2), (int) Math.round(x3),
							(int) Math.round(x4) };
					int[] yPoints = { (int) Math.round(y1), (int) Math.round(y2), (int) Math.round(y3),
							(int) Math.round(y4) };

					boolean anyInsideImage = false;
					for (int k = 0; k < 4; k++) {
						int x = xPoints[k];
						int y = yPoints[k];

						if (x >= 0 && x < radarPlot.getWidth() && y >= 0 && y < radarPlot.getHeight()) {
							anyInsideImage = true;
							break;
						}
					}

					if (anyInsideImage) {
						g.setColor(color);
						g.fillPolygon(xPoints, yPoints, 4);
					}
				}
			}

			return radarPlot;
		} else {
			return null;
		}
	}

	private static BufferedImage generateWarningPlot(DateTime time, double lat, double lon,
			RadarGeneratorSettings settings, RotateLatLonProjection plotProj) throws IOException {
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
			WarningArchive wa = new WarningArchive("radar-image-generator-temp/");
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

					PointD p1 = plotProj.rotateLatLon(polygon.get(i));
					PointD p2 = plotProj.rotateLatLon(polygon.get(j));

					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p1.getY());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p1.getX());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p2.getY());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p2.getX());

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

					PointD p1 = plotProj.rotateLatLon(polygon.get(i));
					PointD p2 = plotProj.rotateLatLon(polygon.get(j));

					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p1.getY());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p1.getX());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p2.getY());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p2.getX());

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

					PointD p1 = plotProj.rotateLatLon(polygon.get(i));
					PointD p2 = plotProj.rotateLatLon(polygon.get(j));

					double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p1.getY());
					double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p1.getX());
					double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, warningPlot.getWidth(),
							p2.getY());
					double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, warningPlot.getHeight(),
							p2.getX());

					g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				}
			}
		}

		return warningPlot;
	}

	private static final Font NOTICE_FONT = new Font(Font.MONOSPACED, Font.BOLD + Font.ITALIC, 36);
	private static BufferedImage noDataAvailableNotice(RadarGeneratorSettings settings) {
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

	private static File getClosestRadarData(DateTime time, double lat, double lon)
			throws NoValidRadarScansFoundException {
		if (radarSites == null) {
			loadRadarSites();
		}

		HashMap<Double, String> closestRadars = findClosestRadarSites(lat, lon);
		SortedSet<Double> closestRadarDists = new TreeSet<Double>(closestRadars.keySet());
		String[] closestRadarCodes = new String[closestRadarDists.size()];

		logger.println(Arrays.toString(closestRadarDists.toArray()), DebugLoggerLevel.VERBOSE);

		for (int i = 0; i < closestRadarDists.size(); i++) {
			closestRadarCodes[i] = closestRadars.get(closestRadarDists.toArray()[i]);
		}

		logger.println(Arrays.toString(closestRadarCodes), DebugLoggerLevel.BRIEF);

		String chosenStation = "";
		ArrayList<String> validFiles = new ArrayList<>();
		for (int i = 0; i < closestRadarCodes.length; i++) {
			DateTime prevUtcDay = time.minusDays(1);
			DateTime nextUtcDay = time.plusDays(1);

			List<String> nexradFilesPrev = new ArrayList<>();
			List<String> nexradFilesNext = new ArrayList<>();

			List<String> nexradFilesCurr = NexradAws.nexradLevel2Files(time.getYear(), time.getMonthOfYear(),
					time.getDayOfMonth(), closestRadarCodes[i], true);

			if (time.getHourOfDay() < 1) {
				nexradFilesPrev = NexradAws.nexradLevel2Files(prevUtcDay.getYear(), prevUtcDay.getMonthOfYear(),
						prevUtcDay.getDayOfMonth(), closestRadarCodes[i], true);
			}
			if (time.getHourOfDay() >= 23) {
				nexradFilesNext = NexradAws.nexradLevel2Files(nextUtcDay.getYear(), nextUtcDay.getMonthOfYear(),
						nextUtcDay.getDayOfMonth(), closestRadarCodes[i], true);
			}

			List<String> nexradFiles = new ArrayList<>();
			nexradFiles.addAll(nexradFilesPrev);
			nexradFiles.addAll(nexradFilesCurr);
			nexradFiles.addAll(nexradFilesNext);

			ArrayList<String> filesWithinTolerance = new ArrayList<>();
			for (int j = 0; j < nexradFiles.size(); j++) {
				String[] awsPath = nexradFiles.get(j).split("/");
				String filename = awsPath[awsPath.length - 1];

				DateTime fileTimestamp = new DateTime(Integer.valueOf(filename.substring(4, 8)),
						Integer.valueOf(filename.substring(8, 10)), Integer.valueOf(filename.substring(10, 12)),
						Integer.valueOf(filename.substring(13, 15)), Integer.valueOf(filename.substring(15, 17)),
						Integer.valueOf(filename.substring(17, 19)), DateTimeZone.UTC);

				if (time.minusMinutes(TIME_TOLERANCE).isBefore(fileTimestamp)
						&& time.plusMinutes(TIME_TOLERANCE).isAfter(fileTimestamp)) {
					logger.println("valid file added: " + filename, DebugLoggerLevel.VERBOSE);
					filesWithinTolerance.add(nexradFiles.get(j));
				}
			}

			if (filesWithinTolerance.size() > 0) {
				validFiles = filesWithinTolerance;
				chosenStation = closestRadarCodes[i];
				break;
			}
		}

		if (validFiles.size() == 0) {
			throw new NoValidRadarScansFoundException();
		}

		// file selection

		String mostRecentFile = validFiles.get(0);
		for (int i = 1; i < validFiles.size(); i++) {
			String[] awsPath = validFiles.get(i).split("/");
			String filename = awsPath[awsPath.length - 1];

			DateTime fileTimestamp = new DateTime(Integer.valueOf(filename.substring(4, 8)),
					Integer.valueOf(filename.substring(8, 10)), Integer.valueOf(filename.substring(10, 12)),
					Integer.valueOf(filename.substring(13, 15)), Integer.valueOf(filename.substring(15, 17)),
					Integer.valueOf(filename.substring(17, 19)), DateTimeZone.UTC);

			if (fileTimestamp.isBefore(time)) {
				mostRecentFile = validFiles.get(i);
			} else {
				break;
			}
		}

		logger.println("chose file: " + mostRecentFile, DebugLoggerLevel.BRIEF);

		try {
			logger.println("try returning file: " + mostRecentFile, DebugLoggerLevel.BRIEF);

			File nexradFile = downloadFile(mostRecentFile,
					"radar-" + chosenStation + ".nexrad" + (mostRecentFile.endsWith(".gz") ? ".gz" : ""));

			logger.println("returning file: " + mostRecentFile, DebugLoggerLevel.BRIEF);

			return nexradFile;
		} catch (IOException e) {
			return null;
		}
	}

	private static final double RADAR_DISTANCE_TOLERANCE = 300; // km

	private static HashMap<Double, String> findClosestRadarSites(double lat, double lon) {
		HashMap<Double, String> ret = new HashMap<>();

		for (int i = 0; i < radarSites.size(); i++) {
			PointD radarLocation = radarSites.get(i).getSiteCoords();
			String radarCode = radarCodes.get(i);

			double rLat = radarLocation.getX();
			double rLon = radarLocation.getY();

			double distance = greatCircleDistance(lat, lon, rLat, rLon);

			if (distance < RADAR_DISTANCE_TOLERANCE) {
				ret.put(distance, radarCode);
			}
		}

		return ret;
	}

	private static double greatCircleDistance(double lat1, double lon1, double lat2, double lon2) {
		double arccosArg = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
						* Math.cos(Math.toRadians(lon1 - lon2));

		double arcAngle = Math.acos(arccosArg);

		double distance = arcAngle * LambertConformalProjection.R;

		return distance;
	}

	private static ArrayList<RadarSite> radarSites = null;
	private static HashMap<String, RadarSite> radarSiteMap = null;
	private static ArrayList<String> radarCodes = null;

	private static void loadRadarSites() {
		radarSites = new ArrayList<>();
		radarCodes = new ArrayList<>();
		radarSiteMap = new HashMap<>();

		TsvParserSettings settings = new TsvParserSettings();
		// the file used in the example uses '\n' as the line separator sequence.
		// the line separator sequence is defined here to ensure systems such as MacOS
		// and Windows
		// are able to process this file correctly (MacOS uses '\r'; and Windows uses
		// '\r\n').
		settings.getFormat().setLineSeparator("\n");

		// creates a TSV parser
		TsvParser parser = new TsvParser(settings);

		// parses all rows in one go.
		List<String[]> allRows = parser.parseAll(loadResourceAsFile("res/radarSites.tsv"));

		for (int i = 0; i < allRows.size(); i++) {
			String[] row = allRows.get(i);

			String code = row[0];
			String city = row[1];

			String lat = row[2];
			String lon = row[3];

			RadarSite r = new RadarSite(code, city, new PointD(Double.valueOf(lat), Double.valueOf(lon)));

			radarSites.add(r);
			radarCodes.add(code);
			radarSiteMap.put(code, r);
		}

		Collections.reverse(radarSites);
		Collections.reverse(radarCodes);
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

	public static File loadResourceAsFile(String urlStr) {
		logger.println("loading " + urlStr, DebugLoggerLevel.VERBOSE);
		URL url = RadarImageGenerator.class.getResource(urlStr);
		URL tilesObj = url;

		// System.out.println("Temp-file created.");

		File file = new File("radar-image-generator-temp/" + urlStr + "");

		if (tilesObj == null) {
			System.out.println("Loading failed to start.");
			return null;
		}

		// System.out.println("Loading successfully started.");

		try {
			FileUtils.copyURLToFile(tilesObj, file);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}

		return file;
	}

	private static final String dataFolder = "radar-image-generator-temp/";

	private static File downloadFile(String url, String fileName) throws IOException {
//		System.out.println("Downloading from: " + url);
		URL dataURL = new URL(url);

		File dataDir = new File(dataFolder);
//		System.out.println("Creating Directory: " + dataFolder);
		dataDir.mkdirs();
		InputStream is = dataURL.openStream();

//		System.out.println("Output File: " + dataFolder + fileName);
		OutputStream os = new FileOutputStream(dataFolder + fileName);
		byte[] buffer = new byte[16 * 1024];
		int transferredBytes = is.read(buffer);
		while (transferredBytes > -1) {
			os.write(buffer, 0, transferredBytes);
			// System.out.println("Transferred "+transferredBytes+" for "+fileName);
			transferredBytes = is.read(buffer);
		}
		is.close();
		os.close();

		return new File(dataFolder + fileName);
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
