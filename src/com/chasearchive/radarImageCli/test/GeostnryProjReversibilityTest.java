package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.satellite.GeoCoord;
import com.chasearchive.radarImageCli.satellite.GeostationaryProjection;
import com.chasearchive.radarImageCli.satellite.VirtualCoord;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GeostnryProjReversibilityTest {
    public static void main(String[] args) throws IOException {
        System.out.println("=== SIMPLE TEST ===");
        simpleTest();
        System.out.println("=== MODERATE TEST ===");
        moderateTest();
        System.out.println("=== RIGOROUS TEST ===");
        rigorousTest();
    }

    public static void simpleTest() {
        // well-tested
        VirtualCoord xy = new VirtualCoord(1.3999641E-5, -1.40070915E-5);
        GeoCoord ll = GeostationaryProjection.GOES_EAST.projectXYToLatLon(xy);
        System.out.println(xy);
        System.out.println("GOES pixel space to lat-lon space");
        System.out.println(ll);

        // for all the marbles
        VirtualCoord xy2 = GeostationaryProjection.GOES_EAST.projectLatLonToXY(ll);
        System.out.println("Lat-lon space back to GOES pixel space");
        System.out.println(xy2);
        // looks pretty promising ngl
    }

    public static void moderateTest() {
        for(int i = 0; i < 100; i++) {
            // well-tested
            VirtualCoord xy = new VirtualCoord(1.3999641E-5 * 0 * i, -1.40070915E-5 * 100 * i);
            GeoCoord ll = GeostationaryProjection.GOES_EAST.projectXYToLatLon(xy);
//            System.out.println(xy);
//            System.out.println("GOES pixel space to lat-lon space");
//            System.out.println(ll);

            // for all the marbles
            VirtualCoord xy2 = GeostationaryProjection.GOES_EAST.projectLatLonToXY(ll);
//            System.out.println("Lat-lon space back to GOES pixel space");
//            System.out.println(xy2);

//            System.out.printf("---ERROR---: %.9f\n", error(xy, xy2));
            System.out.printf("%.2f\t%.9f\n", ll.getLat(), error(xy, xy2));
        }
    }

    public static void rigorousTest() throws IOException {
        double[][] error = new double[10001][10001];

        for(int i = 0; i < error.length; i++) {
            System.out.println(i);
            for(int j = 0; j < error[i].length; j++) {
                VirtualCoord xy = new VirtualCoord(1.3999641E-5 * 3 * (i - 5000), -1.40070915E-5 * 3 * (j - 5000));
                GeoCoord ll = GeostationaryProjection.GOES_EAST.projectXYToLatLon(xy);
                VirtualCoord xy2 = GeostationaryProjection.GOES_EAST.projectLatLonToXY(ll);

                double errorLL = error(xy, xy2);
                errorLL = Double.isNaN(errorLL) ? 0 : errorLL;
                error[i][j] = errorLL;
            }
        }

        BufferedImage errorImg = new BufferedImage(10001, 10001, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = errorImg.createGraphics();

        for(int i = 0; i < error.length; i++) {
            System.out.println(i);
            for (int j = 0; j < error[i].length; j++) {
                double errorLL = error[i][j] / 1.4E-5;
                errorLL = (errorLL > 255) ? 255 : errorLL;
                errorLL = (errorLL < 0) ? 0 : errorLL;
                g.setColor(new Color((int) errorLL, 0, 0));
                g.fillRect(i, j, 1, 1);
            }
        }

        ImageIO.write(errorImg, "PNG", new File("geosReverseError.png"));
    }

    private static double error(VirtualCoord xy1, VirtualCoord xy2) {
        return Math.hypot(xy1.getX() - xy2.getX(), xy1.getY() - xy2.getY());
    }
}
