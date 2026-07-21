package com.chasearchive.radarImageCli.test;

import com.ameliaWx.weatherUtils.WeatherUtils;
import com.chasearchive.radarImageCli.satellite.GvarProcessing;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PokeAtCDMFile {
    public static void main(String[] args) throws IOException {
//        System.out.println(
//                (float) WeatherUtils.brightnessTemperatureFromWavenumber(
//                        GvarProcessing.spectralRadiance(604, 4, "GOES-13") / 100000.0,
//                        WeatherUtils.wavelengthToWavenumber(10.7 / 1000000.0))
//        );

        NetcdfFile ncfile = NetcdfFile.open("/home/a-urq/IdeaProjects/Chase Archive Radar Image CLI/test-files/MYRORSS/20091224-230024.netcdf");

        System.out.println(ncfile);


    }

    public static void pokeAtSuviFile() throws IOException {
        NetcdfFile ncfile = NetcdfFile.open("OR_SUVI-L1b-Fe195_G19_s20253102001532_e20253102001542_c20253102002204.nc");
        System.out.println(ncfile);
        System.exit(44);

        System.out.println(ncfile.findVariable("crDate").read().getInt(0));
        System.out.println(ncfile.findVariable("crTime").read().getInt(0));
        System.out.println(ncfile.findVariable("imageDate").read().getInt(0));
        System.out.println(ncfile.findVariable("imageTime").read().getInt(0));
        System.out.println(ncfile.findVariable("time").read().getInt(0));

        System.exit(0);

        Variable gvarDataVar = ncfile.findVariable("data");
        Variable lonVar = ncfile.findVariable("lon");
        Variable latVar = ncfile.findVariable("lat");
        assert gvarDataVar != null;
        float[][] gvarData = varToArray3D(gvarDataVar)[0];
        assert lonVar != null;
        float[][] lon = varToArray2D(lonVar);
        assert latVar != null;
        float[][] lat = varToArray2D(latVar);

        float max = max(gvarData);
        System.out.println("max:" + max(gvarData)/32);
        System.out.println("min:" + min(gvarData)/32);
        System.out.println(ncfile.findVariable("startLine").read().getInt(0));
        System.out.println(ncfile.findVariable("startElem").read().getInt(0));
        System.out.println("min-lat:" + min(lat));
        System.out.println("max-lat:" + max(lat));
        System.out.println("min-lon:" + min(lon));
        System.out.println("max-lon:" + max(lon));

        BufferedImage img = new BufferedImage(gvarData[0].length, gvarData.length, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();

        for(int i = 0; i < img.getWidth(); i++) {
            for(int j = 0; j < img.getHeight(); j++) {
                g.setColor(new Color(gvarData[j][img.getWidth() - 1 - i]/max, gvarData[j][img.getWidth() - 1 - i]/max, gvarData[j][img.getWidth() - 1 - i]/max));
                g.fillRect(i, j, 1, 1);
            }
        }

        ImageIO.write(img, "JPG", new File("mcfetch-sat.jpg"));
    }

    private static float[][] varToArray2D(Variable v) {
        if (v == null) return new float[0][0];

        try {
            Array arr = v.read();

            int[] shape = arr.getShape();

            float[][] ret = new float[shape[0]][shape[1]];

            for (int h = 0; h < arr.getSize(); h++) {
                ret[h / shape[1]][h % shape[1]] = arr.getFloat(h);
            }

            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new float[0][0];
    }

    private static float[][][] varToArray3D(Variable rawData) {
        int[] shape = rawData.getShape();
        Array _data = null;

        try {
            _data = rawData.read();
        } catch (IOException e) {
            e.printStackTrace();
            return new float[shape[0]][shape[1]][shape[2]];
        }

        float[][][] data = new float[shape[0]][shape[1]][shape[2]];
        // see if an alternate data-reading algorithm that avoids division and modulos
        // could be faster
        for (int i = 0; i < _data.getSize(); i++) {
            int y = (i) % shape[2];
            int z = (i / (shape[2])) % shape[1];
            int t = (i / (shape[2] * shape[1])) % shape[0];

            float record = _data.getFloat(i);

            data[t][z][shape[2] - 1 - y] = record;
        }

        return data;
    }

    private static float max(float[][] arr) {
        float max = -Float.MAX_VALUE;

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                max = Float.max(arr[i][j], max);
            }
        }

        return max;
    }

    private static float min(float[][] arr) {
        float min = Float.MAX_VALUE;

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                if (arr[i][j] != -1024) {
                    min = Float.min(arr[i][j], min);
                }
            }
        }

        return min;
    }
}
