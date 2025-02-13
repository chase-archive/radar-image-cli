package com.chasearchive.radarImageCli.satellite.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.chasearchive.radarImageCli.satellite.GeoCoord;
import com.chasearchive.radarImageCli.satellite.GeostationaryProjection;
import com.chasearchive.radarImageCli.satellite.GoesImage;
import com.chasearchive.radarImageCli.satellite.RotateLatLonProjection;

import ucar.nc2.NetcdfFile;

public class GenerateLatLonMesh {
	public static void main(String[] args) throws IOException {
		@SuppressWarnings("deprecation")
		NetcdfFile ncfile = NetcdfFile.open("/home/a-urq/eclipse-workspace/Chase Archive Radar Image CLI/goes/hires-band-test/OR_ABI-L1b-RadC-M6C02_G16_s20250372301171_e20250372303544_c20250372303571.nc");
		
		GoesImage goesRed = GoesImage.loadFromFile(new File(ncfile.getLocation()));
		
		float[] x = goesRed.field("x").array1D();
		float[] y = goesRed.field("y").array1D();
		float dx = goesRed.dataFromField("dx");
		float dy = goesRed.dataFromField("dy");
		
		GeostationaryProjection satProj = GeostationaryProjection.GOES_EAST;

		float x0 = -x[5000];
		float y0 = y[5000];
		
		long startTime = System.currentTimeMillis();
		for(int i = 0; i < 1000000; i++) {
			GeoCoord latLon1 = satProj.projectXYToLatLon(x0, y0);
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println("geostationary projection time: " + (endTime - startTime)/1000.0 + " us");
		System.out.println("geostationary full-mesh projection time: " + 10000.0*6000.0*4.0*(endTime - startTime)/1000000000.0 + " s");

		GeoCoord latLonTest = satProj.projectXYToLatLon(x0, y0);
		RotateLatLonProjection plotProj = new RotateLatLonProjection(35.18, -97.44, 111.32, 111.32, 1000, 1000);
		
		startTime = System.currentTimeMillis();
		for(int i = 0; i < 1000000; i++) {
			GeoCoord p1 = plotProj.rotateLatLon(latLonTest);
		}
		endTime = System.currentTimeMillis();
		
		System.out.println("rotate lat-lon projection time: " + (endTime - startTime)/1000.0 + " us");
		System.out.println("rotate lat-lon full-mesh projection time: " + 10000.0*6000.0*4.0*(endTime - startTime)/1000000000.0 + " s");

		startTime = System.currentTimeMillis();
		for(int i = 0; i < 1000000; i++) {
			float lat = latLonTest.getLat();
		}
		endTime = System.currentTimeMillis();
		
		System.out.println("variable read time: " + (endTime - startTime)/1000.0 + " us");
		System.out.println("variable full-mesh read time: " + 10000.0*6000.0*4.0*(endTime - startTime)/1000000000.0 + " s");
		
//		System.exit(0);

		startTime = System.currentTimeMillis();
		
		float[][][] latLonMesh = new float[x.length + 1][y.length + 1][2];
		
		for (int i = 0; i <= x.length; i++) {
			for (int j = 0; j <= y.length; j++) {
				float x1 = x0 + i * dx;
				float y1 = y0 + j * dy;

				GeoCoord latLon1 = satProj.projectXYToLatLon(x1, y1);
				
				latLonMesh[i][j][0] = latLon1.getLat();
				latLonMesh[i][j][0] = latLon1.getLon();
			}
		}
		//Saving of object in a file
        FileOutputStream file = new FileOutputStream(new File("goes-east.mesh"));
        ObjectOutputStream out = new ObjectOutputStream(file);
        
        // Method for serialization of object
        out.writeObject(latLonMesh);
        
        out.close();
        file.close();
        
		endTime = System.currentTimeMillis();
		
		System.out.println("actual mesh write time: " + (endTime - startTime)/1000.0 + " s");
	}
}
