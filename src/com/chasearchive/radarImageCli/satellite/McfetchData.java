package com.chasearchive.radarImageCli.satellite;

import com.chasearchive.radarImageCli.ResourceLoader;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.chasearchive.radarImageCli.satellite.GoesMcfetchCutoffs.*;

public class McfetchData {
    // band 1:
    // band 2: https://mcfetch.ssec.wisc.edu/cgi-bin/mcfetch?dkey=fhnp35c2-cv5b-fpux-ygib-y4sh4lrmkdwn&satellite=GOES13&band=2&output=JPG&date=2016-04-11&time=22:45&lat=33.01+96.5&mag=-1&size=375+625
    // band 4: https://mcfetch.ssec.wisc.edu/cgi-bin/mcfetch?dkey=fhnp35c2-cv5b-fpux-ygib-y4sh4lrmkdwn&satellite=GOES13&band=4&output=JPG&date=2016-04-11&time=22:45&lat=33.01+96.5&mag=-1&size=375+625

    private static final String DKEY = "fhnp35c2-cv5b-fpux-ygib-y4sh4lrmkdwn";

    private static final String URL_FORMAT = "https://mcfetch.ssec.wisc.edu/cgi-bin/mcfetch?dkey=%s&satellite=%s&band=%d&output=NETCDF&date=%s&time=%s&lat=%s&mag=-1&size=%s";
    public static File[] downloadGoes(DateTime dateTime, SatelliteSource source, double lat, double lon) throws NoMcfetchFileFoundException, IOException {
        if(source == SatelliteSource.GOES_EAST_MCFETCH) {
            //datetime decision tree
            if(!dateTime.isBefore(GOES_14_TEMP2_END)) {
                // do goes 13
                String sat = "GOES-13";
                String date = String.format("%04d-%02d-%02d", dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
                String time = String.format("%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
                String latLon = String.format("%.2f+%.2f", lat, -lon);
                String size1 = "1500+2500";
                String size2 = "375+625";

                String band1Url = String.format(URL_FORMAT, DKEY, sat, 1, date, time, latLon, size1);
                String band2Url = String.format(URL_FORMAT, DKEY, sat, 2, date, time, latLon, size2);
                String band4Url = String.format(URL_FORMAT, DKEY, sat, 4, date, time, latLon, size2);

                File band1File = downloadFile(band1Url, "sat_band1.nc");
                File band2File = downloadFile(band2Url, "sat_band2.nc");
                File band4File = downloadFile(band4Url, "sat_band4.nc");

                return new File[] { band1File, band2File, band4File };
            } else if(!dateTime.isBefore(GOES_14_TEMP2_START)) {
                // do goes 14
                String sat = "GOES-14";
                String date = String.format("%04d-%02d-%02d", dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
                String time = String.format("%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
                String latLon = String.format("%.2f+%.2f", lat, -lon);
                String size1 = "1500+2500";
                String size2 = "375+625";

                String band1Url = String.format(URL_FORMAT, DKEY, sat, 1, date, time, latLon, size1);
                String band2Url = String.format(URL_FORMAT, DKEY, sat, 2, date, time, latLon, size2);
                String band4Url = String.format(URL_FORMAT, DKEY, sat, 4, date, time, latLon, size2);

                File band1File = downloadFile(band1Url, "sat_band1.nc");
                File band2File = downloadFile(band2Url, "sat_band2.nc");
                File band4File = downloadFile(band4Url, "sat_band4.nc");

                return new File[] { band1File, band2File, band4File };
            } else if(!dateTime.isBefore(GOES_14_TEMP1_END)) {
                // do goes 13
                String sat = "GOES-13";
                String date = String.format("%04d-%02d-%02d", dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
                String time = String.format("%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
                String latLon = String.format("%.2f+%.2f", lat, -lon);
                String size1 = "1500+2500";
                String size2 = "375+625";

                String band1Url = String.format(URL_FORMAT, DKEY, sat, 1, date, time, latLon, size1);
                String band2Url = String.format(URL_FORMAT, DKEY, sat, 2, date, time, latLon, size2);
                String band4Url = String.format(URL_FORMAT, DKEY, sat, 4, date, time, latLon, size2);

                File band1File = downloadFile(band1Url, "sat_band1.nc");
                File band2File = downloadFile(band2Url, "sat_band2.nc");
                File band4File = downloadFile(band4Url, "sat_band4.nc");

                return new File[] { band1File, band2File, band4File };
            } else if(!dateTime.isBefore(GOES_14_TEMP1_START)) {
                // do goes 14
                String sat = "GOES-14";
                String date = String.format("%04d-%02d-%02d", dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
                String time = String.format("%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
                String latLon = String.format("%.2f+%.2f", lat, -lon);
                String size1 = "1500+2500";
                String size2 = "375+625";

                String band1Url = String.format(URL_FORMAT, DKEY, sat, 1, date, time, latLon, size1);
                String band2Url = String.format(URL_FORMAT, DKEY, sat, 2, date, time, latLon, size2);
                String band4Url = String.format(URL_FORMAT, DKEY, sat, 4, date, time, latLon, size2);

                File band1File = downloadFile(band1Url, "sat_band1.nc");
                File band2File = downloadFile(band2Url, "sat_band2.nc");
                File band4File = downloadFile(band4Url, "sat_band4.nc");

                return new File[] { band1File, band2File, band4File };
            } else if(!dateTime.isBefore(GOES_12_13_CUTOFF)) {
                // do goes 13
                String sat = "GOES-13";
                String date = String.format("%04d-%02d-%02d", dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
                String time = String.format("%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
                String latLon = String.format("%.2f+%.2f", lat, -lon);
                String size1 = "1500+2500";
                String size2 = "375+625";

                String band1Url = String.format(URL_FORMAT, DKEY, sat, 1, date, time, latLon, size1);
                String band2Url = String.format(URL_FORMAT, DKEY, sat, 2, date, time, latLon, size2);
                String band4Url = String.format(URL_FORMAT, DKEY, sat, 4, date, time, latLon, size2);

                File band1File = downloadFile(band1Url, "sat_band1.nc");
                File band2File = downloadFile(band2Url, "sat_band2.nc");
                File band4File = downloadFile(band4Url, "sat_band4.nc");

                return new File[] { band1File, band2File, band4File };
            } else if(!dateTime.isBefore(GOES_8_12_CUTOFF)) {
                // do goes 12
                String sat = "GOES-12";
                String date = String.format("%04d-%02d-%02d", dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
                String time = String.format("%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
                String latLon = String.format("%.2f+%.2f", lat, -lon);
                String size1 = "1500+2500";
                String size2 = "375+625";

                String band1Url = String.format(URL_FORMAT, DKEY, sat, 1, date, time, latLon, size1);
                String band2Url = String.format(URL_FORMAT, DKEY, sat, 2, date, time, latLon, size2);
                String band4Url = String.format(URL_FORMAT, DKEY, sat, 4, date, time, latLon, size2);

                File band1File = downloadFile(band1Url, "sat_band1.nc");
                File band2File = downloadFile(band2Url, "sat_band2.nc");
                File band4File = downloadFile(band4Url, "sat_band4.nc");

                return new File[] { band1File, band2File, band4File };
            } else if(!dateTime.isBefore(GOES_8_START)) {
                // do goes 8
                String sat = "GOES-8";
                String date = String.format("%04d-%02d-%02d", dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
                String time = String.format("%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
                String latLon = String.format("%.2f+%.2f", lat, -lon);
                String size1 = "1500+2500";
                String size2 = "375+625";

                String band1Url = String.format(URL_FORMAT, DKEY, sat, 1, date, time, latLon, size1);
                String band2Url = String.format(URL_FORMAT, DKEY, sat, 2, date, time, latLon, size2);
                String band4Url = String.format(URL_FORMAT, DKEY, sat, 4, date, time, latLon, size2);

                File band1File = downloadFile(band1Url, "sat_band1.nc");
                File band2File = downloadFile(band2Url, "sat_band2.nc");
                File band4File = downloadFile(band4Url, "sat_band4.nc");

                return new File[] { band1File, band2File, band4File };
            } else {
                throw new NoMcfetchFileFoundException();
            }
        } else if (source == SatelliteSource.GOES_WEST_MCFETCH){

        } else {
            throw new NoMcfetchFileFoundException();
        }

        // if you reach this point, might as well
        throw new NoMcfetchFileFoundException();
    }

    private static File downloadFile(String url, String fileName) throws IOException {
//		System.out.println("Downloading from: " + url);
        URL dataURL = new URL(url);

        File dataDir = new File(ResourceLoader.DATA_FOLDER);
//		System.out.println("Creating Directory: " + dataFolder);
        dataDir.mkdirs();
        InputStream is = dataURL.openStream();

        int kbDownloaded = 0;

//		System.out.println("Output File: " + dataFolder + fileName);
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
}