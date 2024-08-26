package com.chasearchive.radarImageCli;

import static com.chasearchive.radarImageCli.LambertConformalProjection.HRRR_PROJ;
import static com.chasearchive.radarImageCli.RadarImageCli.logger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.wxArchives.earthWeather.nexrad.NexradAws;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

public class RadarImageGenerator {
	public static BufferedImage generateRadar(DateTime time, double lat, double lon, GeneratorSettings settings)
			throws IOException {
		BufferedImage basemap = generateBasemap(lat, lon, settings);
		
		File radarFile = null;
		try {
			radarFile = getClosestRadarData(time, lat, lon);
		} catch (NoValidRadarScansFoundException e) {
			e.printStackTrace();
		}

		return basemap;
	}

	private static BufferedImage generateBasemap(double lat, double lon, GeneratorSettings settings)
			throws IOException {
		ArrayList<ArrayList<PointD>> countyBorders;
		ArrayList<ArrayList<PointD>> stateBorders;
		ArrayList<ArrayList<PointD>> interstates;
		ArrayList<ArrayList<PointD>> majorRoads;

		File countyBordersKML = loadResourceAsFile("res/usCounties.kml");
		File stateBordersKML = loadResourceAsFile("res/usStates.kml");
		File interstatesKML = loadResourceAsFile("res/wms.kml");
		File majorRoadsKML = loadResourceAsFile("res/roadtrl020.kml");

		countyBorders = getPolygons(countyBordersKML);
		stateBorders = getPolygons(stateBordersKML);
		interstates = getPolygons(interstatesKML);
		majorRoads = getPolygons(majorRoadsKML);

		PointD latLonProjected = HRRR_PROJ.projectLatLonToIJ(lon, lat);
		PointD trueNorthPointer = HRRR_PROJ.projectLatLonToIJ(lon, lat + 0.01); // i love finite differencing

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

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, basemap.getWidth(), basemap.getHeight());

		PointD latLonProjectedUL = new PointD(
				latLonProjected.getX() - (111.32 / HRRR_PROJ.dx * settings.getSize() * settings.getAspectRatioFloat()),
				latLonProjected.getY() - (111.32 / HRRR_PROJ.dy * settings.getSize()));
		PointD latLonProjectedDR = new PointD(
				latLonProjected.getX() + (111.32 / HRRR_PROJ.dx * settings.getSize() * settings.getAspectRatioFloat()),
				latLonProjected.getY() + (111.32 / HRRR_PROJ.dy * settings.getSize()));

		logger.println("latLonProjectedUL: " + latLonProjectedUL, DebugLoggerLevel.VERBOSE);
		logger.println("latLonProjectedDR: " + latLonProjectedDR, DebugLoggerLevel.VERBOSE);

		g1.setColor(new Color(255, 255, 255));
		g1.setStroke(bs);
		for (ArrayList<PointD> polygon : stateBorders) {
			for (int i = 0; i < polygon.size(); i++) {
				int j = i + 1;
				if (j == polygon.size())
					j = 0;

				PointD p1 = HRRR_PROJ.projectLatLonToIJ(polygon.get(i));
				PointD p2 = HRRR_PROJ.projectLatLonToIJ(polygon.get(j));

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getX());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getY());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getX());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getY());

				PointD xy1P = rotateAroundCenter(x1, y1, basemap.getWidth(), basemap.getHeight(), rotationAngle);
				PointD xy2P = rotateAroundCenter(x2, y2, basemap.getWidth(), basemap.getHeight(), rotationAngle);

				double x1P = xy1P.getX();
				double y1P = xy1P.getY();
				double x2P = xy2P.getX();
				double y2P = xy2P.getY();

				g1.drawLine((int) x1P, (int) y1P, (int) x2P, (int) y2P);
			}
		}

		g2.setColor(new Color(255, 255, 255));
		g2.setStroke(bs);
		for (ArrayList<PointD> polygon : countyBorders) {
			for (int i = 0; i < polygon.size(); i++) {
				int j = i + 1;
				if (j == polygon.size())
					j = 0;

				PointD p1 = HRRR_PROJ.projectLatLonToIJ(polygon.get(i));
				PointD p2 = HRRR_PROJ.projectLatLonToIJ(polygon.get(j));

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getX());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getY());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getX());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getY());

				PointD xy1P = rotateAroundCenter(x1, y1, basemap.getWidth(), basemap.getHeight(), rotationAngle);
				PointD xy2P = rotateAroundCenter(x2, y2, basemap.getWidth(), basemap.getHeight(), rotationAngle);

				double x1P = xy1P.getX();
				double y1P = xy1P.getY();
				double x2P = xy2P.getX();
				double y2P = xy2P.getY();

				g2.drawLine((int) x1P, (int) y1P, (int) x2P, (int) y2P);
			}
		}

		g3.setColor(new Color(0, 0, 0));
		g3.setStroke(ts);
		for (ArrayList<PointD> polygon : interstates) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				PointD p1 = HRRR_PROJ.projectLatLonToIJ(polygon.get(i));
				PointD p2 = HRRR_PROJ.projectLatLonToIJ(polygon.get(j));

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getX());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getY());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getX());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getY());

				PointD xy1P = rotateAroundCenter(x1, y1, basemap.getWidth(), basemap.getHeight(), rotationAngle);
				PointD xy2P = rotateAroundCenter(x2, y2, basemap.getWidth(), basemap.getHeight(), rotationAngle);

				double x1P = xy1P.getX();
				double y1P = xy1P.getY();
				double x2P = xy2P.getX();
				double y2P = xy2P.getY();

				g3.drawLine((int) x1P, (int) y1P, (int) x2P, (int) y2P);
			}
		}

		g3.setColor(new Color(128, 0, 0));
		g3.setStroke(bs);
		for (ArrayList<PointD> polygon : interstates) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				PointD p1 = HRRR_PROJ.projectLatLonToIJ(polygon.get(i));
				PointD p2 = HRRR_PROJ.projectLatLonToIJ(polygon.get(j));

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getX());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getY());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getX());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getY());

				PointD xy1P = rotateAroundCenter(x1, y1, basemap.getWidth(), basemap.getHeight(), rotationAngle);
				PointD xy2P = rotateAroundCenter(x2, y2, basemap.getWidth(), basemap.getHeight(), rotationAngle);

				double x1P = xy1P.getX();
				double y1P = xy1P.getY();
				double x2P = xy2P.getX();
				double y2P = xy2P.getY();

				g3.drawLine((int) x1P, (int) y1P, (int) x2P, (int) y2P);
			}
		}

		g4.setColor(new Color(0, 0, 0));
		g4.setStroke(ts);
		for (ArrayList<PointD> polygon : majorRoads) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				PointD p1 = HRRR_PROJ.projectLatLonToIJ(polygon.get(i));
				PointD p2 = HRRR_PROJ.projectLatLonToIJ(polygon.get(j));

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getX());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getY());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getX());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getY());

				PointD xy1P = rotateAroundCenter(x1, y1, basemap.getWidth(), basemap.getHeight(), rotationAngle);
				PointD xy2P = rotateAroundCenter(x2, y2, basemap.getWidth(), basemap.getHeight(), rotationAngle);

				double x1P = xy1P.getX();
				double y1P = xy1P.getY();
				double x2P = xy2P.getX();
				double y2P = xy2P.getY();

				g4.drawLine((int) x1P, (int) y1P, (int) x2P, (int) y2P);
			}
		}

		g4.setColor(new Color(128, 128, 255));
		g4.setStroke(bs);
		for (ArrayList<PointD> polygon : majorRoads) {
			for (int i = 0; i < polygon.size() - 1; i++) {
				int j = i + 1;

				PointD p1 = HRRR_PROJ.projectLatLonToIJ(polygon.get(i));
				PointD p2 = HRRR_PROJ.projectLatLonToIJ(polygon.get(j));

				double x1 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p1.getX());
				double y1 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p1.getY());
				double x2 = linScale(latLonProjectedUL.getX(), latLonProjectedDR.getX(), 0, basemap.getWidth(),
						p2.getX());
				double y2 = linScale(latLonProjectedUL.getY(), latLonProjectedDR.getY(), 0, basemap.getHeight(),
						p2.getY());

				PointD xy1P = rotateAroundCenter(x1, y1, basemap.getWidth(), basemap.getHeight(), rotationAngle);
				PointD xy2P = rotateAroundCenter(x2, y2, basemap.getWidth(), basemap.getHeight(), rotationAngle);

				double x1P = xy1P.getX();
				double y1P = xy1P.getY();
				double x2P = xy2P.getX();
				double y2P = xy2P.getY();

				g4.drawLine((int) x1P, (int) y1P, (int) x2P, (int) y2P);
			}
		}

		float[] scales = { 1f, 1f, 1f, 0.25f };
		float[] offsets = new float[4];
		RescaleOp rop = new RescaleOp(scales, offsets, null);
		float[] scales2 = { 1f, 1f, 1f, 0.2f };
		float[] offsets2 = new float[4];
		RescaleOp rop2 = new RescaleOp(scales2, offsets2, null);

		g.drawImage(roads, rop2, 0, 0);
		g.drawImage(highways, 0, 0, null);
		g.drawImage(counties, rop, 0, 0);
		g.drawImage(states, 0, 0, null);

		return basemap;
	}

	private static final int TIME_TOLERANCE = 20; // minutes
	private static File getClosestRadarData(DateTime time, double lat, double lon) throws NoValidRadarScansFoundException {
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

		ArrayList<String> validFiles = new ArrayList<>();
		for (int i = 0; i < closestRadarCodes.length; i++) {
			DateTime prevUtcDay = time.minusDays(1);
			DateTime nextUtcDay = time.plusDays(1);
			
			List<String> nexradFilesPrev = new ArrayList<>();
			List<String> nexradFilesNext = new ArrayList<>();
			
			List<String> nexradFilesCurr = NexradAws.nexradLevel2Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
					closestRadarCodes[i], true);
			
			if(time.getHourOfDay() < 1) {
				nexradFilesPrev = NexradAws.nexradLevel2Files(prevUtcDay.getYear(), prevUtcDay.getMonthOfYear(), prevUtcDay.getDayOfMonth(),
						closestRadarCodes[i], true);
			}
			if(time.getHourOfDay() >= 23) {
				nexradFilesNext = NexradAws.nexradLevel2Files(nextUtcDay.getYear(), nextUtcDay.getMonthOfYear(), nextUtcDay.getDayOfMonth(),
						closestRadarCodes[i], true);
			}
			
			List<String> nexradFiles = new ArrayList<>();
			nexradFiles.addAll(nexradFilesPrev);
			nexradFiles.addAll(nexradFilesCurr);
			nexradFiles.addAll(nexradFilesNext);

			ArrayList<String> filesWithinTolerance = new ArrayList<>();
			for(int j = 0; j < nexradFiles.size(); j++) {
				String[] awsPath = nexradFiles.get(j).split("/");
				String filename = awsPath[awsPath.length - 1];
				
				DateTime fileTimestamp = new DateTime(
						Integer.valueOf(filename.substring(4, 8)),
						Integer.valueOf(filename.substring(8, 10)),
						Integer.valueOf(filename.substring(10, 12)),
						Integer.valueOf(filename.substring(13, 15)),
						Integer.valueOf(filename.substring(15, 17)),
						Integer.valueOf(filename.substring(17, 19)),
						DateTimeZone.UTC
					);
				
				if(time.minusMinutes(TIME_TOLERANCE).isBefore(fileTimestamp)
						&& time.plusMinutes(TIME_TOLERANCE).isAfter(fileTimestamp)) {
					logger.println("valid file added: " + filename, DebugLoggerLevel.VERBOSE);
					filesWithinTolerance.add(nexradFiles.get(j));
				}
			}
			
			if (filesWithinTolerance.size() > 0) {
				validFiles = filesWithinTolerance;
				break;
			}
		}
		
		if (validFiles.size() == 0) {
			throw new NoValidRadarScansFoundException();
		}
		
		// file selection
		
		String mostRecentFile = validFiles.get(0);
		for(int i = 1; i < validFiles.size(); i++) {
			String[] awsPath = validFiles.get(i).split("/");
			String filename = awsPath[awsPath.length - 1];
			
			DateTime fileTimestamp = new DateTime(
					Integer.valueOf(filename.substring(4, 8)),
					Integer.valueOf(filename.substring(8, 10)),
					Integer.valueOf(filename.substring(10, 12)),
					Integer.valueOf(filename.substring(13, 15)),
					Integer.valueOf(filename.substring(15, 17)),
					Integer.valueOf(filename.substring(17, 19)),
					DateTimeZone.UTC
				);
			
			if(fileTimestamp.isBefore(time)) {
				mostRecentFile = filename;
			} else {
				break;
			}
		}
		
		logger.println("chose file: " + mostRecentFile, DebugLoggerLevel.BRIEF);
		
		try {
			File nexradFile = downloadFile(mostRecentFile, "radar.nexrad");
			
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
	private static ArrayList<String> radarCodes = null;

	private static void loadRadarSites() {
		radarSites = new ArrayList<>();
		radarCodes = new ArrayList<>();

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
		}

		Collections.reverse(radarSites);
		Collections.reverse(radarCodes);
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

	private static File loadResourceAsFile(String urlStr) {
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
}
