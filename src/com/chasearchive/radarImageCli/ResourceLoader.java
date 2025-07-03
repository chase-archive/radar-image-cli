package com.chasearchive.radarImageCli;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

import static com.chasearchive.radarImageCli.RadarImageCli.logger;

public class ResourceLoader {
    public static final String DATA_FOLDER = "radar-image-cli-temp/";

    public static File loadResourceAsFile(String urlStr) {
        logger.println("loading " + urlStr, DebugLoggerLevel.VERBOSE);
        URL tilesObj = RadarImageGenerator.class.getResource(urlStr);

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
