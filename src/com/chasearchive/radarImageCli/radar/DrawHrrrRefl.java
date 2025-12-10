package com.chasearchive.radarImageCli.radar;

import com.chasearchive.radarImageCli.ColorTable;
import com.chasearchive.radarImageCli.PointD;
import com.chasearchive.radarImageCli.ResourceLoader;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DrawHrrrRefl {
    public static void main(String[] args) throws IOException {
        System.out.println(LambertConformalProjection.HRRR_PROJ.projectLatLonToIJ(-105, 50.5));
        System.out.println(LambertConformalProjection.HRRR_PROJ.projectLatLonToIJ(-94, 50.5));
        System.out.println(LambertConformalProjection.HRRR_PROJ.projectLatLonToIJ(-94, 45));
        System.out.println(LambertConformalProjection.HRRR_PROJ.projectLatLonToIJ(-105, 45));

        File f = new File("/home/a-urq/Downloads/nam.t00z.conusnest.hiresf02.tm00.grib2");

        double lat = 46.69;
        double lon = -99.1;
        double s = 6.0;
        double r = 720;

        double ppd = r/s;

        RotateLatLonProjection plotProj = new RotateLatLonProjection(lat, lon, 111.32, 111.32, 1000, 1000);
        RotateLatLonProjection reversePlotProj = new RotateLatLonProjection(-lat, -lon, 111.32, 111.32, 1000, 1000);

        NetcdfFile ncfile = NetcdfFile.open(f.getAbsolutePath());
        Variable reflVar = ncfile.findVariable("Reflectivity_height_above_ground");

        double[][] refl1km = readVariable4Dim(reflVar)[0][0];

        BufferedImage output = new BufferedImage((int) ((16.0)/(9.0) * r), (int) r, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = output.createGraphics();

        for(int i = 0; i < output.getWidth(); i++) {
            for(int j = 0; j < output.getHeight(); j++) {
                double iCenter = output.getWidth()/2.0;
                double jCenter = output.getHeight()/2.0;

                double latPrime = -(j - jCenter) / ppd;
                double lonPrime = (i - iCenter) / ppd;

                PointD revProjPoint = plotProj.reverseRotateLatLon(lonPrime, latPrime);

                PointD ij = LambertConformalProjection.HRRR_PROJ.projectLatLonToIJ(revProjPoint.getY(), revProjPoint.getX());

                double refl1km_0 = bilinearInterpolation(refl1km, ij.getX(), ij.getY());

                g.setColor(reflectivityColorTable.getColor(refl1km_0));
                g.fillRect(i, j, 1, 1);
            }
        }

        ImageIO.write(output, "PNG", new File("nam3km-enderlin.png"));
    }

    private static double bilinearInterpolation(double[][] data, double i, double j) {
            if (i == (int) i) {
                if (j == (int) j) {
                    return data[(int) j][(int) i];
                } else {
                    double[] linearWeights = calculateLinearWeights(i);

                    double dataI0 = data[(int) j][(int) i];
                    double dataI1 = data[(int) j][(int) i + 1];

                    return linearWeights[0] * dataI0 + linearWeights[1] * dataI1;
                }
            } else {
                if (j == (int) j) {
                    double[] linearWeights = calculateLinearWeights(i);

                    double dataJ0 = data[(int) j][(int) i];
                    double dataJ1 = data[(int) j + 1][(int) i];

                    return linearWeights[0] * dataJ0 + linearWeights[1] * dataJ1;
                } else {
                    double[][] bilinearWeights = calculateBilinearWeights(i, j);

                    double data00 = data[(int) j][(int) i];
                    double data01 = data[(int) j + 1][(int) i];
                    double data10 = data[(int) j][(int) i + 1];
                    double data11 = data[(int) j + 1][(int) i + 1];

                    return bilinearWeights[0][0] * data00 + bilinearWeights[0][1] * data01
                            + bilinearWeights[1][0] * data10 + bilinearWeights[1][1] * data11;
                }
            }
    }

    // assumes a regular 1-dimensional grid with a spacing of 1
    private static double[] calculateLinearWeights(double i) {
        double weightI1 = i % 1.0;
        double weightI0 = 1 - weightI1;

        double[] linearWeights = { weightI0, weightI1 };

        return linearWeights;
    }

    // assumes a regular i-j grid with a spacing of [1, 1]
    private static double[][] calculateBilinearWeights(double i, double j) {
        double weightI1 = i % 1.0;
        double weightI0 = 1 - weightI1;
        double weightJ1 = j % 1.0;
        double weightJ0 = 1 - weightJ1;

        double[][] bilinWeights = { { weightI0 * weightJ0, weightI0 * weightJ1 },
                { weightI1 * weightJ0, weightI1 * weightJ1 } };

        return bilinWeights;
    }

    private static final ColorTable reflectivityColorTable = new ColorTable(
            ResourceLoader.loadResourceAsFile("res/awips-ii-official-mod-low-filter-2.pal"), 0.1f, 10, "dBZ");

    private static double[][][][] readVariable4Dim(Variable rawData) {
        int[] shape = rawData.getShape();
        Array _data = null;

        try {
            _data = rawData.read();
        } catch (IOException e) {
            e.printStackTrace();
            return new double[shape[0]][shape[1]][shape[2]][shape[3]];
        }

        double[][][][] data = new double[shape[0]][shape[1]][shape[2]][shape[3]];
        // see if an alternate data-reading algorithm that avoids division and modulos
        // could be faster
        for (int i = 0; i < _data.getSize(); i++) {
            int x = i % shape[3];
            int y = (i / shape[3]) % shape[2];
            int z = (i / (shape[3] * shape[2])) % shape[1];
            int t = (i / (shape[3] * shape[2] * shape[1])) % shape[0];

            double record = _data.getDouble(i);

            data[t][z][shape[2] - 1 - y][x] = record;
        }

        return data;
    }
}
