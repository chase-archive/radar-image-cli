package com.chasearchive.radarImageCli.test;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.chasearchive.radarImageCli.RadarImageCli;
import com.chasearchive.radarImageCli.satellite.SatelliteImageCli;

public class FullSuiteTest {
	public static void main(String[] args) {
		// Case info
		DateTime time = new DateTime(2024, 5, 3, 0, 15, 0, DateTimeZone.UTC);
		double lat = 32.607;
		double lon = -99.855;
		String caseName = "hawley2024";

		// Run full suite
		runSuite(time, lat, lon, caseName);
	}

	private static void runSuite(DateTime time, double lat, double lon, String caseName) {
		String dt = String.format("%04d%02d%02d_%02d%02d", time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
				time.getHourOfDay(), time.getMinuteOfHour());
		String _lat = String.valueOf(lat);
		String _lon = String.valueOf(lon);
		
		String[] argsRadLoc = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "1:1", "-s", "0.5", "-r", "1200", "-debug", "SILENT", "-o", "caseTests/radloc-" + caseName + ".png"};
		String[] argsRadReg = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "1:1", "-s", "2.0", "-c", "MRMS", "-r", "1200", "-debug", "SILENT", "-o", "caseTests/radreg-" + caseName + ".png"};
		String[] argsSatReg = {"-dt", dt, "-lat", _lat, "-lon", _lon, 
				"-a", "4:3", "-s", "2.0", "-r", "1080", "-debug", "SILENT", "-o", "caseTests/satreg-" + caseName + ".png"};

//		RadarImageCli.main(argsRadLoc);
//		RadarImageCli.main(argsRadReg);
		SatelliteImageCli.main(argsSatReg);
	}
}
