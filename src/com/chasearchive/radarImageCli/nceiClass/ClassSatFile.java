package com.chasearchive.radarImageCli.nceiClass;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.util.Arrays;

public class ClassSatFile implements Comparable<ClassSatFile> {
    private File file;
    private String satelliteName;
    private DateTime dateTime;
    private int bandNumber;

    ClassSatFile(File file, String satelliteName, DateTime dateTime, int bandNumber) {
        this.file = file;
        this.bandNumber = bandNumber;
        this.dateTime = dateTime;
        this.satelliteName = satelliteName;
    }

    public static ClassSatFile fromGoes(File file) {
        String fileName = file.getName();

        String[] tokens = fileName.split("\\.");

        String satNameRaw = tokens[0];
        String yearRaw = tokens[1];
        String dayRaw = tokens[2];
        String timeRaw = tokens[3];
        String bandRaw = tokens[4];

        String satelliteName = satNameRaw.replace("goes", "GOES-");

        int year = Integer.parseInt(yearRaw);
        int dayOfYear = Integer.parseInt(dayRaw);
        int hour = Integer.parseInt(timeRaw.substring(0, 2));
        int minute = Integer.parseInt(timeRaw.substring(2, 4));
        int second = Integer.parseInt(timeRaw.substring(4, 6));

        DateTime dateTime = new DateTime(year, 1, 1, hour, minute, second, DateTimeZone.UTC);

        dateTime = dateTime.withDayOfYear(dayOfYear);

        int bandNumber = Integer.parseInt(bandRaw.substring(5, 7));

        return new ClassSatFile(file, satelliteName, dateTime, bandNumber);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getSatelliteName() {
        return satelliteName;
    }

    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getBandNumber() {
        return bandNumber;
    }

    public void setBandNumber(int bandNumber) {
        this.bandNumber = bandNumber;
    }

    @Override
    public int compareTo(ClassSatFile o) {
        int comp1 = dateTime.compareTo(o.dateTime);

        if(comp1 == 0) {
            return bandNumber - o.bandNumber;
        } else {
            return comp1;
        }
    }

    @Override
    public String toString() {
        return "ClassSatFile{" +
                "file=" + file +
                ", satelliteName='" + satelliteName + '\'' +
                ", dateTime=" + dateTime +
                ", bandNumber=" + bandNumber +
                '}';
    }
}
