package com.chasearchive.radarImageCli.test;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.chasearchive.radarImageCli.RadarImageCli;
import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class FullSuiteTest {
	public static void main(String[] args) {
		// Case info
		DateTime time = new DateTime(2025, 4, 27, 21, 30, 0, DateTimeZone.UTC);
		double lat = 41.99;
		double lon = -102.16;
		String caseName = "goes-bug-from-jim";

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
				"-a", "16:9", "-s", "2.5", "-c", "MRMS", "-r", "14000", "-debug", "SILENT", "-lyr", "SEPARATE", "-tms", "FALSE", "-o", "caseTests/" + caseName};
		String[] argsSatVis = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "4:3", "-s", "15.0", "-r", "720", "-debug", "SILENT", "-lyr", "SEPARATE-NO-BASEMAP", "-tms", "FALSE", "-o", "caseTests/" + caseName};
		String[] argsSatLir = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "4:3", "-s", "3.0", "-t", "LIR", "-r", "720", "-debug", "SILENT", "-lyr", "SEPARATE-NO-BASEMAP", "-tms", "FALSE", "-o", "caseTests/" + caseName};

//		RadarImageCli.main(argsRadLoc);
//		RadarImageCli.main(argsRadReg);
		SatelliteImageCli.main(argsSatVis);
//		SatelliteImageCli.main(argsSatLir);
	}
}
