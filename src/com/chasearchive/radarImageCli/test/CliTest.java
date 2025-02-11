package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.RadarImageCli;

public class CliTest {
	public static void main(String[] args) {
//		String[] args1 = {"-dt", "20230227_0320", "-lat", "35.18", "-lon", "-97.44", "-a", "4:3", "-s", "4.0", "-c", "MRMS", "-debug", "SILENT", "-o", "radargen-case-normanEf2-zoomedOut-mrms.png"};
//		String[] args1 = {"-dt", "20240811_1100", "-lat", "35.18", "-lon", "-97.44", "-a", "4:3", "-s", "2.0", "-debug", "VERBOSE", "-o", "radargen-case-flood-zoomOut.png"};
//		String[] args1 = {"-dt", "20240503_0015", "-lat", "32.61", "-lon", "-99.86", "-a", "4:3", "-s", "0.5" "-debug", "SILENT", "-o", "radargen-case-hawleyTx-newProj-BV.png"};
//		String[] args1 = {"-dt", "20240520_0045", "-lat", "35.65", "-lon", "-98.96", "-a", "4:3", "-s", "2.0", "-m", "BR", "-c", "MRMS",  "-o", "radargen-case-custerCityOk-Mrms.png"};
//		String[] args1 = {"-dt", "20240129_1953", "-lat", "61.22", "-lon", "-149.90", "-a", "4:3", "-m", "BR", "-s", "3.0", "-o", "radargen-case-anchorageAk.png"};
//		String[] args1 = {"-dt", "20240823_0045", "-lat", "31.76", "-lon", "-106.44", "-a", "4:3", "-s", "0.5", "-o", "radargen-case-elPasoDustStorm.png"};
//		String[] args1 = {"-dt", "20231030_2324", "-lat", "45.34", "-lon", "-97.30", "-a", "4:3", "-s", "0.5", "-o", "radargen-case-waubaySdSnowSquall.png"};
//		String[] args1 = {"-dt", "20240430_2315", "-lat", "35.606786", "-lon", "-98.855", "-a", "4:3", "-s", "0.5", "-debug", "SILENT", "-o", "radargen-case-torP.png"};
//		String[] args1 = {"-dt", "20240520_0105", "-lat", "38.37", "-lon", "-97.15", "-a", "4:3", "-s", "2.0", "-debug", "SILENT", "-o", "radargen-case-svrD.png"};

		String[] args1 = {"-dt", "20150604_2345", "-lat", "39.14", "-lon", "-104.07", 
				"-a", "4:3", "-c", "NEXRAD", "-s", "0.5", "-debug", "VERBOSE", "-o", "radargen-case-simlaCo.png"};
		
//		long startTime = System.currentTimeMillis();
//		RadarImageCli.main(args1);
//		long endTime = System.currentTimeMillis();
		
//		DateTime start = new DateTime(2023, 2, 26, 21, 50, 0,DateTimeZone.UTC);
//		DateTime end = new DateTime(2023, 2, 27, 0, 0, 0,DateTimeZone.UTC);
		
//		while(start.isBefore(end)) {
//			String dtArg = String.format("%04d%02d%02d-%02d%02d", start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), start.getHourOfDay(), start.getMinuteOfHour());
//			String oArg = String.format("%04d%02d%02d%02d%02d", start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), start.getHourOfDay(), start.getMinuteOfHour());
//			
//			String[] args1 = {"-dt", dtArg, "-lat", "35.18", "-lon", "-97.44", "-a", "4:3", "-s", "3.0", "-c", "MRMS", "-debug", "SILENT", "-o", "radargen-apr27/" + oArg + ".png"};
//
//			RadarImageCli.main(args1);
//			
//			start = start.plusMinutes(2);
//		}

		RadarImageCli.main(args1);
	}
}
