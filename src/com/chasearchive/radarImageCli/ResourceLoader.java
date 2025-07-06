package com.chasearchive.radarImageCli;

import com.chasearchive.radarImageCli.radar.RadarImageGenerator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

public class ResourceLoader {
    public static final String DATA_FOLDER = "radar-image-cli-temp/";
    public static String wwaFolder = "";
    public static final String PERSISTENT_DATA_FOLDER = "radar-image-cli-persistent/";

    public static File loadResourceAsFile(String urlStr) {
        URL tilesObj = ResourceLoader.class.getResource(urlStr);

        // System.out.println("Temp-file created.");

        File file = new File(DATA_FOLDER + urlStr);

        if (tilesObj == null) {
            System.out.println("Loading failed to start.");
            return null;
        }

        // System.out.println("Loading successfully started.");

        try {
            FileUtils.copyURLToFile(tilesObj, file);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return null;
        }

        return file;
    }
}
