package com.chasearchive.radarImageCli.nceiClass;

import com.chasearchive.radarImageCli.ResourceLoader;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class NceiClass {
    public static ArrayList<File> getFilesInOrder(String orderId) {
        String dir = String.format(ResourceLoader.DATA_FOLDER + "class-orders/%s", orderId);

        return getFilesInDirectory(dir);
    }

    private static ArrayList<File> getFilesInDirectory(String dir) {
        File directory = new File(dir);
        File[] files = directory.listFiles();
        ArrayList<File> fileList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if(file.getName().contains(".")) {
                    fileList.add(file);
                } else {
                    fileList.addAll(getFilesInDirectory(file.getPath()));
                }
            }
        }

        return fileList;
    }

    public static void downloadClassOrder(String orderId) throws IOException {
        new File(String.format(ResourceLoader.DATA_FOLDER + "class-orders/%s/", orderId)).mkdirs();

        File manifest = downloadFile(String.format("https://order.class.noaa.gov/public/%s/001/", orderId), String.format("class-orders/%s/manifest.html", orderId));

        ArrayList<String> filenames = readFilenamesFromManifest(manifest);

        for (String name : filenames) {
            downloadFile(String.format("https://order.class.noaa.gov/public/%s/%s", orderId, name), String.format("class-orders/%s/%s", orderId, name));
        }
    }

    public static ArrayList<String> readFilenamesFromManifest(File f) throws IOException {
        ArrayList<String> filenames = new ArrayList<>();

        Pattern p = Pattern.compile("<a href=\"\\/downloads\\/public\\/\\d*?\\/(.*?)\">");
        Matcher m = p.matcher(readFile(f.getPath(), Charset.defaultCharset()));

        while(m.find()) {
            String group = m.group(1);
            System.out.println(group);

            filenames.add(group);
        }

        return filenames;
    }

    private static File downloadFile(String url, String fileName) throws IOException {
        if(new File(fileName).exists()) {
            return new File(fileName);
        }

        System.out.println("Downloading from: " + url);
        URL dataURL = new URL(url);

        File dataDir = new File(ResourceLoader.DATA_FOLDER);
//		System.out.println("Creating Directory: " + dataFolder);
        dataDir.mkdirs();
        InputStream is = dataURL.openStream();

        int kbDownloaded = 0;

		System.out.println("Output File: " + ResourceLoader.DATA_FOLDER + fileName);
        Path file = Paths.get(ResourceLoader.DATA_FOLDER + fileName);
        System.out.println(file.getParent());
        new File(file.getParent().toUri()).mkdirs();

        OutputStream os = Files.newOutputStream(Paths.get(ResourceLoader.DATA_FOLDER + fileName));
        byte[] buffer = new byte[16 * 1024];
        int transferredBytes = is.read(buffer);
        while (transferredBytes > -1) {
            os.write(buffer, 0, transferredBytes);
            // System.out.println("Transferred "+transferredBytes+" for "+fileName);
            transferredBytes = is.read(buffer);
            kbDownloaded += 16;

            if((kbDownloaded/1024.0)%8 == 0) {
//				System.out.printf("%d MB downloaded... (%d)\n", (kbDownloaded/1024), kbDownloaded);
            }
        }
        is.close();
        os.close();

        return new File(ResourceLoader.DATA_FOLDER + fileName);
    }

    // Source - https://stackoverflow.com/a/326440
    // Posted by erickson, modified by community. See post 'Timeline' for change history
    // Retrieved 2026-07-09, License - CC BY-SA 4.0

    private static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
