package com.chasearchive.radarImageCli.satellite;

import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;

public class GoesImageMcfetch extends CdmFile implements SatelliteImage{
    // DON'T LOSE THESE URLS
    // band 1: https://mcfetch.ssec.wisc.edu/cgi-bin/mcfetch?dkey=fhnp35c2-cv5b-fpux-ygib-y4sh4lrmkdwn&satellite=GOES13&band=1&output=JPG&date=2016-04-11&time=22:45&lat=33.01+96.5&mag=-1&size=1500+2500
    // band 2: https://mcfetch.ssec.wisc.edu/cgi-bin/mcfetch?dkey=fhnp35c2-cv5b-fpux-ygib-y4sh4lrmkdwn&satellite=GOES13&band=2&output=JPG&date=2016-04-11&time=22:45&lat=33.01+96.5&mag=-1&size=375+625
    // band 4: https://mcfetch.ssec.wisc.edu/cgi-bin/mcfetch?dkey=fhnp35c2-cv5b-fpux-ygib-y4sh4lrmkdwn&satellite=GOES13&band=4&output=JPG&date=2016-04-11&time=22:45&lat=33.01+96.5&mag=-1&size=375+625
    // These three will pull the VIS, SIR, and LIR for the same exact geographical area and they should (should) be ready for compositing by the right code
    // https://mcfetch.ssec.wisc.edu/cgi-bin/mcfetch?dkey=fhnp35c2-cv5b-fpux-ygib-y4sh4lrmkdwn&satellite=GOES3&band=1&output=JPG&date=1981-01-01&time=18:45&lat=33.01+96.5&mag=-1&size=1500+2500
    // found one single working goes-3 image lol

    public static void main(String[] args) throws IOException {
        GoesImageMcfetch image = GoesImageMcfetch.loadFromFile(new File("test-files/goes13_1_2016_102_2245.nc"));
    }

    public static GoesImageMcfetch loadFromFile(File f) throws IOException, NotValidMcfetchFileException {
        if(f == null) {
            return null;
        }

        GoesImageMcfetch image = new GoesImageMcfetch();

        image.locationOnDisk = f.getAbsolutePath();

        NetcdfFile ncfile;
        try {
            ncfile = NetcdfFile.open(image.locationOnDisk);
        } catch (IOException e) {
            throw new NotValidMcfetchFileException();
        }
//        System.out.println(ncfile);

        image.permaFields.put("data", DataField.fromCdmVar(ncfile.findVariable("data")));
        image.permaFields.get("data").bundleField("scale_factor", DataField.fromNumber(1.0/32.0));
        image.permaFields.get("data").bundleField("add_offset", DataField.fromNumber(0));
        image.permaFields.get("data").processOffsets();

        image.permaFields.put("lat", DataField.fromCdmVar(ncfile.findVariable("lat")));

        image.permaFields.put("lon", DataField.fromCdmVar(ncfile.findVariable("lon")));

        image.permaFields.put("imageDate", DataField.fromCdmVar(ncfile.findVariable("imageDate")));
        image.permaFields.put("imageTime", DataField.fromCdmVar(ncfile.findVariable("imageTime")));

        image.permaFields.put("satellite", DataField.fromNexradAttrToStr(ncfile.findGlobalAttribute("Satellite_Sensor")));

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
