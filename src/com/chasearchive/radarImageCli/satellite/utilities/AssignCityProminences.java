package com.chasearchive.radarImageCli.satellite.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.chasearchive.radarImageCli.City;
import com.chasearchive.radarImageCli.LambertConformalProjection;
import com.chasearchive.radarImageCli.RadarImageGenerator;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class AssignCityProminences {
	public static void main(String[] args) throws FileNotFoundException {
		List<City> cities = new ArrayList<>();
		
		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");

		// creates a CSV parser
		CsvParser parser = new CsvParser(settings);

		// parses all rows in one go.
		List<String[]> allRows = parser.parseAll(RadarImageGenerator.loadResourceAsFile("res/usCities.csv"));

		for (int i = 0; i < allRows.size(); i++) {
			String[] row = allRows.get(i);

			City city = new City(row[0], Double.valueOf(row[1]), Double.valueOf(row[2]), Integer.valueOf(row[3]));
			
			cities.add(city);
		}
		settings.getFormat().setLineSeparator("\n");

		// parses all rows in one go.
		List<String[]> allRowsWorld = parser.parseAll(new File("/home/a-urq/Downloads/worldcities/worldcities.csv"));

		for (int i = 0; i < allRowsWorld.size(); i++) {
			String[] row = allRowsWorld.get(i);

			if(row[9] == null) {
				row[9] = "1";
			}
			
			System.out.printf("%s,%s,%s,%s,%s\n", row[0], row[2], row[3], row[9], row[4]);
			City city = new City(row[0], Double.valueOf(row[2]), Double.valueOf(row[3]), (int) (double) Double.valueOf(row[9]));
			
			if(!"United States".equals(row[4])) {
				cities.add(city);
			}
		}

		int processedCities = 0;
		for (City city : cities) {
				// code to calculate city prominence
			if(processedCities % 100 == 0) {
				System.out.printf("Processed %d cities...\n", processedCities);
			}
			double closestLargerCityDis = 1000000;
			int closestLargerCityPop = 0;
			if(cities == null) {
				city.setProminence(100);
			} else {
				for(City c : cities) {
					double cityDis = greatCircleDistance(city.getLatitude(), city.getLongitude(), c.getLatitude(), c.getLongitude());
					
					if(cityDis < closestLargerCityDis && c.getPopulation() > city.getPopulation()) {
						closestLargerCityDis = cityDis;
						closestLargerCityPop = c.getPopulation();
					}
				}
				
				double prominence = (double) city.getPopulation()*Math.log10(city.getPopulation())/closestLargerCityPop * closestLargerCityDis/100.0;
				
				// special handling for weird okc and norman prominence ratings
				if(city.getLatitude() > 35 && city.getLatitude() < 36 && city.getLongitude() > -98 && city.getLongitude() < -97) {
					if("Norman".equals(city.getName()) || "Oklahoma City".equals(city.getName())) {
						prominence *= 2;
					}
				}
				
//				System.out.printf("promn calc %s, %d, %f, %d, %f:\n", city.getName(), city.getPopulation(), closestLargerCityDis, closestLargerCityPop, prominence);
				
				city.setProminence(prominence);
			}
			processedCities++;
		}

		Collections.sort(cities, new CityComparator());
		Collections.reverse(cities);
		
		PrintWriter pw = new PrintWriter("worldCities.csv");
		
		for(int i = 0; i < cities.size(); i++) {
			City city = cities.get(i);
			
			pw.printf("%s,%s,%s,%s,%s\n", city.getName(), city.getLatitude(), city.getLongitude(), city.getPopulation(), city.getProminence());
		}
		
		pw.close();
	}
	
	private static class CityComparator implements Comparator<City> {
		@Override
		public int compare(City o1, City o2) {
			// TODO Auto-generated method stub
			return o1.getPopulation() - o2.getPopulation();
		}
	}

	private static double greatCircleDistance(double lat1, double lon1, double lat2, double lon2) {
		double arccosArg = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
						* Math.cos(Math.toRadians(lon1 - lon2));

		double arcAngle = Math.acos(arccosArg);

		double distance = arcAngle * LambertConformalProjection.R;

		return distance;
	}
}
