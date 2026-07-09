package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.satellite.DataField;
import ucar.nc2.NetcdfFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GvarAreaTest {
    public static void main(String[] args) throws IOException {
        NetcdfFile file = NetcdfFile.open("/home/a-urq/IdeaProjects/Chase Archive Radar Image CLI/radar-image-cli-temp/class-orders/8559260104/001/goes12.2004.150.204513.BAND_06.nc");

        System.out.println(file);

        DataField gvarCounts = DataField.fromCdmVar(file.findVariable("data"));

        System.out.println(gvarCounts);

        float[][] gvar = gvarCounts.array3D()[0];

        float min = min(gvar);
        float max = max(gvar);

        BufferedImage img = new BufferedImage(gvar[0].length * 2, gvar.length * 4, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();

        for(int i = 0; i < gvar.length; i++) {
            for(int j = 0; j < gvar[i].length; j++) {
                int val = (int) linScale(min, max, 255, 0, gvar[i][j]);
                g.setColor(new Color(val, val, val));
                g.fillRect(j * 2, i * 4, 4, 4);
            }
        }

        ImageIO.write(img, "PNG", new File("ncei-class-gvar-test-band-06.png"));
    }

    private static float linScale(float preMin, float preMax, float postMin, float postMax, float value) {
        float slope = (postMax - postMin) / (preMax - preMin);

        return slope * (value - preMin) + postMin;
    }

    private static float min(float[][] arr) {
        float min = Float.MAX_VALUE;

        for(int i = 0; i < arr.length; i++) {
            for(int j = 0; j < arr[i].length; j++) {
                min = Float.min(min, arr[i][j]);
            }
        }

        return min;
    }

    private static float max(float[][] arr) {
        float max = -Float.MAX_VALUE;

        for(int i = 0; i < arr.length; i++) {
            for(int j = 0; j < arr[i].length; j++) {
                max = Float.max(max, arr[i][j]);
            }
        }

        return max;
    }
}
