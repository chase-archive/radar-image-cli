package com.chasearchive.radarImageCli.radar;

import com.chasearchive.radarImageCli.ColorTable;
import com.chasearchive.radarImageCli.ResourceLoader;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

public class MyrorssComposite {
    float[] lat;
    float[] lon;
    float[][] rala;

    float dLat;
    float dLon;

    private static final ColorTable reflectivityColorTable = new ColorTable(
            ResourceLoader.loadResourceAsFile("res/awips-ii-official-mod-low-filter-2.pal"), 0.1f, 10, "dBZ");
    public static void main(String[] args) throws IOException {
        MyrorssComposite comp = new MyrorssComposite(new File("/home/a-urq/IdeaProjects/Chase Archive Radar Image CLI/test-files/MYRORSS/20091224-230024.netcdf"));

        for(int i = 3264; i <= 3264 + 100; i++) {
            System.out.println(comp.rala[2160][i]);
        }

        BufferedImage img = new BufferedImage(comp.lon.length, comp.lat.length, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        for(int i = 0; i < img.getWidth(); i++) {
            if(i % 100 == 0) System.out.println(i + "/" + comp.lon.length);
            for(int j = 0; j < img.getHeight(); j++) {
                Color c = reflectivityColorTable.getColor(comp.rala[j][i]);

                g.setColor(c);
                g.fillRect(i, j, 1, 1);
            }
        }
        ImageIO.write(img, "PNG", new File("myrorss-rala-2009-12-24-2300.png"));
    }

    public MyrorssComposite(File f) throws IOException {
        NetcdfFile ncfile = NetcdfFile.open(f.getAbsolutePath());

        System.out.println(ncfile);

        int latLength = ncfile.findDimension("Lat").getLength();
        int lonLength = ncfile.findDimension("Lon").getLength();
        float initLat = ncfile.findGlobalAttribute("Latitude").getNumericValue().floatValue();
        float initLon = ncfile.findGlobalAttribute("Longitude").getNumericValue().floatValue();
        dLat = ncfile.findGlobalAttribute("LatGridSpacing").getNumericValue().floatValue();
        dLon = ncfile.findGlobalAttribute("LonGridSpacing").getNumericValue().floatValue();

        System.out.println(latLength);
        System.out.println(lonLength);
        System.out.println(dLat);
        System.out.println(dLon);

        lat = new float[latLength];
        lon = new float[lonLength];

        for(int i = 0; i < lat.length; i++) {
            lat[i] = initLat - dLat * i;
        }

        for(int i = 0; i < lon.length; i++) {
            lon[i] = initLon + dLon * i;
        }

        float[] ralaRaw = varToArray1D(ncfile.findVariable("ReflectivityAtLowestAltitude"));
        float[] pixel_x = varToArray1D(ncfile.findVariable("pixel_x"));
        float[] pixel_y = varToArray1D(ncfile.findVariable("pixel_y"));

        rala = new float[latLength][lonLength];
        constructGrid(rala, ralaRaw, pixel_x, pixel_y);

        fillGapsPass(rala);
        fillGapsPass(rala);
        fill2x2GapsPass(rala);
        fillGapsPass(rala);
    }

    private void constructGrid(float[][] grid, float[] data, float[] x, float[] y) {
        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid[i].length; j++) {
                grid[i][j] = -1024;
            }
        }

        for(int i = 0; i < data.length; i++) {
            // original data types for x and y are shorts, this should be cast-safe
            grid[(int) x[i]][(int) y[i]] = data[i];
        }
    }

    // one pass of longitudinal and then latitudinal gap filling
    private void fillGapsPass(float[][] grid) {
        // longitudinal pass
        for(int i = 0; i < grid.length; i++) {
            for(int j = 1; j < grid[i].length - 1; j++) {
                if(grid[i][j] == -1024 && grid[i][j - 1] != -1024 && grid[i][j + 1] != -1024) {
                    grid[i][j] = (float) ((grid[i][j - 1] + grid[i][j + 1]) / 2.0);
                }
            }
        }

        // latitudinal pass
        for(int i = 1; i < grid.length - 1; i++) {
            for(int j = 0; j < grid[i].length; j++) {
                if(grid[i][j] == -1024 && grid[i - 1][j] != -1024 && grid[i + 1][j] != -1024) {
                    grid[i][j] = (float) ((grid[i - 1][j] + grid[i + 1][j]) / 2.0);
                }
            }
        }
    }

    // one pass of longitudinal and then latitudinal gap filling
    private void fill2x2GapsPass(float[][] grid) {
        // longitudinal pass
        for(int i = 0; i < grid.length; i++) {
            for(int j = 1; j < grid[i].length - 2; j++) {
                if(grid[i][j] == -1024 && grid[i][j + 1] == -1024 && grid[i][j - 1] != -1024 && grid[i][j + 2] != -1024) {
                    grid[i][j]     = (float) ((2 * grid[i][j - 1] + grid[i][j + 2]) / 3.0);
                    grid[i][j + 1] = (float) ((grid[i][j - 1] + 2 * grid[i][j + 2]) / 3.0);
                }
            }
        }

        // latitudinal pass
        for(int i = 1; i < grid.length - 2; i++) {
            for(int j = 0; j < grid[i].length; j++) {
                if(grid[i][j] == -1024 && grid[i + 1][j] == -1024 && grid[i - 1][j] != -1024 && grid[i + 2][j] != -1024) {
                    grid[i][j]     = (float) ((2 * grid[i - 1][j] + grid[i + 2][j]) / 3.0);
                    grid[i + 1][j] = (float) ((grid[i - 1][j] + 2 * grid[i + 2][j]) / 3.0);
                }
            }
        }
    }

    private float[] varToArray1D(Variable v) {
        int[] shape = v.getShape();
        Array _data = null;

        try {
            _data = v.read();
        } catch (IOException e) {
            e.printStackTrace();
            return new float[shape[0]];
        }

        float[] data = new float[shape[0]];
        for (int i = 0; i < _data.getSize(); i++) {
            int t = i;

            float record = _data.getFloat(i);

            data[t] = record;
        }

        return data;
    }
}
