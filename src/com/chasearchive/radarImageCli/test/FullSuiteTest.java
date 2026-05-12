package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.satellite.GvarProcessing;
import com.chasearchive.radarImageCli.satellite.ModisBlueMarble;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.chasearchive.radarImageCli.radar.RadarImageCli;
import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

import java.io.File;
import java.util.Arrays;

public class FullSuiteTest {
	// MAKE HRRR COMPARISON RETROFIT FOR CAPSTONE FIG 1A ENDERLIN EF5

	public static void main(String[] args) {
//		String testString = "31.74-71.92";
//		String regex = "(?<=\\d)-(?=\\d)";
//		System.out.println(testString);
//		System.out.println(testString.replaceAll(regex, " -"));
//		System.exit(44);

//		// make loop stuff
//
//		System.out.println("before args check");
//		System.out.println(Arrays.toString(args));
//
//		if(args.length == 0) {
//			args = new String[] { "20250924-1000", "20251007-0600", "31.74", "-71.92", "humberto-imelda-fujiwhara-2025", "16.0", "TRUE" };
//		} else if (args[0].startsWith("-Xmx")) {
//			args = new String[] { "20250924-1000", "20251007-0600", "31.74", "-71.92", "humberto-imelda-fujiwhara-2025", "16.0", "TRUE" };
//		}
//
//		System.out.println("after args check");
//		System.out.println(Arrays.toString(args));
//
//		DateTime startTime = convertCliDtToDatetime(args[0]);
//		DateTime endTime = convertCliDtToDatetime(args[1]);
//		double latitude = Double.parseDouble(args[2]);
//		double longitude = Double.parseDouble(args[3]);
//		String caseTestDir = args[4];
//		double size = Double.parseDouble(args[5]);
//		boolean forceFD = "TRUE".equals(args[6]);
//
//		makeLoop(startTime, endTime, latitude, longitude, caseTestDir, size, forceFD);
//
////		System.out.println(GvarProcessing.spectralRadiance(600, 1, "GOES-13"));
//		System.exit(0);

		// Case info
		// do adaptive white point thing
//		DateTime time = new DateTime(2008, 4, 7, 22, 45, 0, DateTimeZone.UTC);
//		double lat = 34.08;
//		double lon = -98.96;
//		String caseName = String.format("electra-tx-2008-test", time.getHourOfDay(), time.getMinuteOfHour());

		DateTime time = new DateTime(1999, 5, 4, 0, 0, 0, DateTimeZone.UTC);
		double lat = 35;
		double lon = -98;
		String caseName = "1999-05-03-moore"; // also need midday

//		makeLoop(time, time.plusHours(8), lat, lon, caseName, 8.0, true);

//		DateTime time = new DateTime(2025, 7, 10, 2, 0, 0, DateTimeZone.UTC);
//		double lat = 32.77;
//		double lon = -114.81;
//		String caseName = "arizona-2025-alignment-test"; // also need midday

		long maxMemory = Runtime.getRuntime().maxMemory();
		long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		runSuite(time, lat, lon, caseName);

//		// Run full suite
//		for(int i = 0; i < 12 * 6 + 1; i++) {
//			runSuite(time, lat, lon, caseName);
//			time = time.plusMinutes(5);
//
//			new File("/home/a-urq/IdeaProjects/Chase Archive Radar Image CLI/caseTests/2025-06-16-wellfleet-is-toty/radar-local/composite.png")
//					.renameTo(new File(
//						String.format("/home/a-urq/IdeaProjects/Chase Archive Radar Image CLI/caseTests/2025-06-16-wellfleet-is-toty/radar-local/composite-%03d.png",
//								i)
//					));
//		}
	}

	public static DateTime convertCliDtToDatetime(String input) {
		int year = Integer.parseInt(input.substring(0, 4));
		int month = Integer.parseInt(input.substring(4, 6));
		int day = Integer.parseInt(input.substring(6, 8));
		int hour = Integer.parseInt(input.substring(9, 11));
		int minute = Integer.parseInt(input.substring(11, 13));

		return new DateTime(year, month, day, hour, minute, 0, DateTimeZone.UTC);
	}

	private static void runSuite(DateTime time, double lat, double lon, String caseName) {
		String dt = String.format("%04d%02d%02d_%02d%02d", time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
				time.getHourOfDay(), time.getMinuteOfHour());
		String _lat = String.valueOf(lat);
		String _lon = String.valueOf(lon);

		String[] argsRadLoc = {"-dt", dt, "-lat", _lat, "-lon", _lon,
				"-a", "4:3", "-s", "0.5", "-r", "1080", "-debug", "SILENT", "-lyr", "COMPOSITE", "-tms", "TRUE", "-o", "caseTests/" + caseName};
		String[] argsRadReg = {"-dt", dt, "-lat", _lat, "-lon", _lon,
				"-a", "16:9", "-s", "8.0", "-c", "MRMS", "-r", "720", "-debug", "SILENT", "-lyr", "COMPOSITE", "-tms", "FALSE", "-o", "caseTests/" + caseName};
		String[] argsSatVis = {"-dt", dt, "-lat", _lat, "-lon", _lon,
				"-a", "16:9", "-s", "8.0", "-r", "1080", "-debug", "SILENT", "-lyr", "COMPOSITE", "-tms", "FALSE", "-o", "caseTests/" + caseName};
		String[] argsSatLir = {"-dt", dt, "-lat", _lat, "-lon", _lon,
				"-a", "4:3", "-s", "3.0", "-t", "LIR", "-r", "720", "-debug", "SILENT", "-lyr", "SEPARATE-NO-BASEMAP", "-tms", "FALSE", "-o", "caseTests/" + caseName};

//		RadarImageCli.main(argsRadLoc);
		SatelliteImageCli.main(argsSatVis);
		RadarImageCli.main(argsRadReg);
//		SatelliteImageCli.main(argsSatLir);
	}

	private static void makeLoop(DateTime startTime, DateTime endTime, double lat, double lon, String caseName, double size, boolean forceFD) {
		DateTime currentTime = startTime;

		while (!currentTime.isAfter(endTime)) {
			DateTime time = currentTime;

			String dt = String.format("%04d%02d%02d_%02d%02d", time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
					time.getHourOfDay(), time.getMinuteOfHour());
			String _lat = String.valueOf(lat);
			String _lon = String.valueOf(lon);
			String _size = String.valueOf(size);

			String _forceFD = forceFD ? "MESO1" : "FALSE";

//			String[] argsRadLoc = {"-dt", dt, "-lat", _lat, "-lon", _lon,
//					"-a", "4:3", "-s", "0.5", "-r", "720", "-debug", "SILENT", "-lyr", "SEPARATE", "-tms", "TRUE", "-o", "caseTests/" + caseName};
//			String[] argsRadReg = {"-dt", dt, "-lat", _lat, "-lon", _lon,
//					"-a", "16:9", "-s", "2.5", "-c", "MRMS", "-r", "720", "-debug", "SILENT", "-lyr", "COMPOSITE", "-tms", "FALSE", "-o", "caseTests/" + caseName};
			String[] argsSatVis = {"-dt", dt, "-lat", _lat, "-lon", _lon,
					"-a", "3:2", "-s", _size, "-r", "1080", "-debug", "SILENT", "-lyr", "COMPOSITE", "-tms", "FALSE", "-forceFD", _forceFD, "-o", "caseTests/" + caseName};
//			String[] argsSatLir = {"-dt", dt, "-lat", _lat, "-lon", _lon,
//					"-a", "4:3", "-s", "3.0", "-t", "LIR", "-r", "720", "-debug", "SILENT", "-lyr", "SEPARATE-NO-BASEMAP", "-tms", "FALSE", "-o", "caseTests/" + caseName};

//		RadarImageCli.main(argsRadLoc);
//		RadarImageCli.main(argsRadReg);
			SatelliteImageCli.main(argsSatVis);
//		SatelliteImageCli.main(argsSatLir);

			currentTime = currentTime.plusMinutes(5);
		}
	}

	private static String convToGigaMega(long bytes) {
		int b = (int) (bytes % 1024);
		bytes = bytes >> 10;
		int kiB = (int) (bytes % 1024);
		bytes = bytes >> 10;
		int miB = (int) (bytes % 1024);
		bytes = bytes >> 10;
		int giB = (int) (bytes % 1024);

		return String.format("%4d GiB %4d MiB %4d KiB %4d B", giB, miB, kiB, b);
	}
}

