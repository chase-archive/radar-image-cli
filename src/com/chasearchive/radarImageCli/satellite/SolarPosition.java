package com.chasearchive.radarImageCli.satellite;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

// calculate the solar zenith angle in order to brighten the vis bands in the late afternoon
// lots of wikipedia as a source. very scientifically sound i know
public class SolarPosition {
	public static void main(String[] args) {
		DateTime dt = new DateTime(2025, 7, 1, 18, 0, DateTimeZone.UTC);
		double lat = 35.18;
		double lon = -97.44;
		
		System.out.println("solar altitude: " + Math.toDegrees(solarAltitude(dt, lat, lon)));
		System.out.println("cos(solar zenith): " + cosSolarZenithAngle(dt, lat, lon));
//		System.out.println("solar altitude - greenwich: " + Math.toDegrees(solarAltitude(dt, lat1, lon1)));
//		System.out.println("cos(solar zenith) - greenwich: " + cosSolarZenithAngle(dt, lat1, lon1));
	}
	
	public static double solarZenithAngle(DateTime dt, double lat, double lon) {
		return Math.acos(solarZenithAngle(dt, lat, lon));
	}
	
	public static double solarAltitude(DateTime dt, double lat, double lon) {
		return Math.asin(sinSolarAltitudeAngle(dt, lat, lon));
	}
	
	public static double cosSolarZenithAngle(DateTime dt, double lat, double lon) {
		return sinSolarAltitudeAngle(dt, lat, lon);
	}
	
	// https://en.wikipedia.org/wiki/Solar_zenith_angle
	public static double sinSolarAltitudeAngle(DateTime dt, double lat, double lon) {
		dt = dt.toDateTime(DateTimeZone.UTC); // ensures use of UTC
		
		double sunDeclination = solarDeclination(dt); // radians
		double hourAngle = hourAngle(dt, lon); // radians
		
		return Math.sin(Math.toRadians(lat)) * Math.sin(sunDeclination) + Math.cos(Math.toRadians(lat)) * Math.cos(sunDeclination) * Math.cos(hourAngle);
	}
	
	// https://en.wikipedia.org/wiki/Position_of_the_Sun#Declination_of_the_Sun_as_seen_from_Earth
	private static final double EARTH_OBLIQUITY = Math.toRadians(23.44); // radians
	private static final double WINTER_SOLSTICE_NEW_YEAR_INTERVAL = 10; // days
	private static final double NEW_YEAR_PERIHELION_INTERVAL = 2; // days
	private static final double EARTH_ECCENTRICITY = 0.0167; // unitless
	private static final double YEAR_LENGTH = 365.24;
	private static double solarDeclination(DateTime dt) {
		// days since midnight UT Jan 1
		double N0 = fractionalDay(dt);
		
		// adjustment to roughly account for leap years
		// doesn't account for skipped leap years (1900, 2100, etc)
		double deltaN =  deltaN(dt);
		
		double N = N0 + deltaN;
		
		double sinObliquity = Math.sin(EARTH_OBLIQUITY);
		
		// both in degrees, need radian conversions
		double elTerm1 = 360 / YEAR_LENGTH * (N + WINTER_SOLSTICE_NEW_YEAR_INTERVAL);
		double elTerm2A = 360 / YEAR_LENGTH * (N - NEW_YEAR_PERIHELION_INTERVAL);
		double elTerm2 = 360 / Math.PI * EARTH_ECCENTRICITY * Math.sin(Math.toRadians(elTerm2A));

		// in degrees, needs radian conversion
		double eclipticCoLongitude = elTerm1 + elTerm2;
		
		return Math.asin(-sinObliquity * Math.cos(Math.toRadians(eclipticCoLongitude)));
	}
	
	// source: buried in one of my notebooks | units: radians
	// lon units: degrees
	private static final double HOURS_IN_DAY = 24;
	private static double hourAngle(DateTime dt, double lon) {
		double solarMeanTimeOnPrimeMeridian = fractionalHour(dt); // units: fractional hours
		double solarMeanTime = solarMeanTimeOnPrimeMeridian + HOURS_IN_DAY/360 * lon; // units: fractional hours
		double apparentSolarTime = solarMeanTime + 1/60.0 * equationOfTime(dt);

//		System.out.println("solarMeanTimeOnPrimeMeridian: " + solarMeanTimeOnPrimeMeridian);
//		System.out.println("solarMeanTime: " + solarMeanTime);
//		System.out.println("equationOfTime(dt): " + equationOfTime(dt));
//		System.out.println("apparentSolarTime: " + apparentSolarTime);
		
		apparentSolarTime += 2 * HOURS_IN_DAY;
		apparentSolarTime %= HOURS_IN_DAY;
		
		double hourAngle = Math.PI / 12.0 * ((apparentSolarTime + 12) % 24);
		
		return hourAngle;
	}
	
	// https://en.wikipedia.org/wiki/Equation_of_time | units: minutes
	private static double equationOfTime(DateTime dt) {
		double D = 6.24004077 + 0.01720197 * (365.25*(dt.getYear() - 2000) + dt.getDayOfYear() - 1);
		
		double deltaMin = -7.659 * Math.sin(D) + 9.863 * Math.sin(2 * D + 3.5932);
		return deltaMin;
	}

	private static double deltaN(DateTime dt) {
		double fYear = fractionalYear(dt);
		
		double deltaN = 0.25 * (fYear % 4.0 * 2);
		return deltaN;
	}

	private static double fractionalHour(DateTime dt) {
		double fHour_second = 1.0/3600.0 * dt.getSecondOfDay();
		
		return fHour_second;
	}

	private static double fractionalDay(DateTime dt) {
		double fDay_day = dt.getDayOfYear() - 1;
		double fDay_second = 1/86400.0 * dt.getSecondOfDay();
		
		return fDay_day + fDay_second;
	}

	private static double fractionalYear(DateTime dt) {
		double fYear_year = dt.getYear();
		double fYear_day = 1.0/365.24 * dt.getDayOfYear() - 1;
		double fYear_second = 1.0/365.24 * 1.0/86400.0 * dt.getSecondOfDay();
		
		return fYear_year + fYear_day + fYear_second;
	}
}
