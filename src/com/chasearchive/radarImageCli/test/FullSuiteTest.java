package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.satellite.GvarProcessing;
import com.chasearchive.radarImageCli.satellite.ModisBlueMarble;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.chasearchive.radarImageCli.radar.RadarImageCli;
import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class FullSuiteTest {
	public static void main(String[] args) {
//		System.out.println(GvarProcessing.spectralRadiance(600, 1, "GOES-13"));
//		System.exit(0);

		// Case info
		DateTime time = new DateTime(2010, 2, 11, 23, 0, 0, DateTimeZone.UTC);
		double lat = 33.01;
		double lon = -97.25;
		String caseName = "dallas-tx-record-snow-2010";

		long maxMemory = Runtime.getRuntime().maxMemory();
		long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		// Run full suite
		runSuite(time, lat, lon, caseName);
	}

	private static void runSuite(DateTime time, double lat, double lon, String caseName) {
		String dt = String.format("%04d%02d%02d_%02d%02d", time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
				time.getHourOfDay(), time.getMinuteOfHour());
		String _lat = String.valueOf(lat);
		String _lon = String.valueOf(lon);
		
		String[] argsRadLoc = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "4:3", "-s", "0.5", "-r", "720", "-debug", "SILENT", "-lyr", "SEPARATE", "-tms", "TRUE", "-o", "caseTests/" + caseName};
		String[] argsRadReg = {"-dt", dt, "-lat", _lat, "-lon", _lon,
				"-a", "16:9", "-s", "2.5", "-c", "MRMS", "-r", "720", "-debug", "SILENT", "-lyr", "COMPOSITE", "-tms", "FALSE", "-o", "caseTests/" + caseName};
		String[] argsSatVis = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "16:9", "-s", "3.0", "-r", "720", "-debug", "SILENT", "-lyr", "COMPOSITE", "-tms", "FALSE", "-o", "caseTests/" + caseName};
		String[] argsSatLir = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "4:3", "-s", "3.0", "-t", "LIR", "-r", "720", "-debug", "SILENT", "-lyr", "SEPARATE-NO-BASEMAP", "-tms", "FALSE", "-o", "caseTests/" + caseName};

//		RadarImageCli.main(argsRadLoc);
//		RadarImageCli.main(argsRadReg);
		SatelliteImageCli.main(argsSatVis);
//		SatelliteImageCli.main(argsSatLir);
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

