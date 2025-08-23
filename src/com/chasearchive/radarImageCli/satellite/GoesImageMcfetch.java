package com.chasearchive.radarImageCli.satellite;

import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;

public class GoesImageMcfetch extends CdmFile implements SatelliteImage{
    public static void main(String[] args) throws IOException {
        GoesImageMcfetch image = GoesImageMcfetch.loadFromFile(new File("test-files/goes13_1_2016_102_2245.nc"));
    }

    public static GoesImageMcfetch loadFromFile(File f) throws IOException {
        if(f == null) {
            return null;
        }

        GoesImageMcfetch image = new GoesImageMcfetch();

        image.locationOnDisk = f.getAbsolutePath();

        NetcdfFile ncfile = NetcdfFile.open(image.locationOnDisk);
        System.out.println(ncfile);

        image.permaFields.put("data", DataField.fromCdmVar(ncfile.findVariable("data")));
        image.permaFields.get("data").bundleField("scale_factor", DataField.fromNumber(1.0/32.0));
        image.permaFields.get("data").bundleField("add_offset", DataField.fromNumber(0));
        image.permaFields.get("data").processOffsets();

        image.permaFields.put("lat", DataField.fromCdmVar(ncfile.findVariable("lat")));

        image.permaFields.put("lon", DataField.fromCdmVar(ncfile.findVariable("lon")));

        image.permaFields.put("crDate", DataField.fromCdmVar(ncfile.findVariable("crDate")));
        image.permaFields.put("crTime", DataField.fromCdmVar(ncfile.findVariable("crTime")));

        ncfile.close();
        return image;
    }

    public DataField field(String key) {
        if(permaFields.containsKey(key)) {
            return permaFields.get(key);
        } else {
            if (!swapFields.containsKey(key)) {
                try {
                    loadIntoSwap(key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return swapFields.get(key);
        }
    }

    public float dataFromField(String key) {
        return dataFromField(key, 0);
    }

    public float dataFromField(String key, int... indices) {
        if(permaFields.containsKey(key)) {
            return permaFields.get(key).getData(indices);
        } else {
            return fromSwap(key, indices);
        }
    }

    public float fromSwap(String key) {
        return fromSwap(key, 0);
    }

    public float fromSwap(String key, int... indices) {
        if (!swapFields.containsKey(key)) {
            try {
                loadIntoSwap(key);
            } catch (IOException e) {
                // pass up the chain actually this just makes it compile for now
            }
        }

        return swapFields.get(key).getData(indices);
    }

    public void loadIntoSwap(String... keys) throws IOException {
        // no swap fields to load
    }
}
