package com.chasearchive.radarImageCli.test;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.chasearchive.radarImageCli.RadarImageCli;
import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class FullSuiteTest {
	public static void main(String[] args) {
		// Case info
		DateTime time = new DateTime(2019, 5, 7, 20, 36, 0, DateTimeZone.UTC);
		double lat = 36.04;
		double lon = -101.25;
		String caseName = "spearman-2019";

		// Run full suite
		runSuite(time, lat, lon, caseName);
	}

	private static void runSuite(DateTime time, double lat, double lon, String caseName) {
		String dt = String.format("%04d%02d%02d_%02d%02d", time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
				time.getHourOfDay(), time.getMinuteOfHour());
		String _lat = String.valueOf(lat);
		String _lon = String.valueOf(lon);
		
		String[] argsRadLoc = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "4:3", "-s", "0.5", "-r", "1080", "-debug", "SILENT", "-lyr", "BOTH", "-o", "caseTests/" + caseName};
		String[] argsRadReg = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "4:3", "-s", "2.0", "-c", "MRMS", "-r", "1080", "-debug", "SILENT", "-lyr", "BOTH", "-o", "caseTests/" + caseName};
		String[] argsSatVis = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "4:3", "-s", "2.0", "-r", "1080", "-debug", "SILENT", "-lyr", "BOTH", "-o", "caseTests/" + caseName};
		String[] argsSatLir = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "4:3", "-s", "2.0", "-t", "LIR", "-r", "1080", "-debug", "SILENT", "-lyr", "BOTH", "-o", "caseTests/" + caseName};
		
		RadarImageCli.main(argsRadLoc);
		RadarImageCli.main(argsRadReg);
		SatelliteImageCli.main(argsSatVis);
		SatelliteImageCli.main(argsSatLir);
	}
}
