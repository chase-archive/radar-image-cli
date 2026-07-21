package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.nceiClass.ClassSatFile;
import com.chasearchive.radarImageCli.nceiClass.NceiClass;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class TestClassOrder {
    public static void main(String[] args) throws IOException {
        NceiClass.downloadClassOrder("8561091534");

        ArrayList<ClassSatFile> files = NceiClass.getGoesFilesInOrder("8561091534");

        Collections.sort(files);

        System.out.println(files.size());

        for (ClassSatFile f : files) {
            System.out.println(f);
        }

        HashMap<Integer, ClassSatFile> filesByTime = NceiClass.getGoesFilesInOrderFromTime("8561091534", new DateTime(2003, 5, 8, 22, 45, DateTimeZone.UTC));

        for (Iterator<Integer> it = filesByTime.keySet().iterator(); it.hasNext(); ) {
            int i = it.next();

            System.out.println(i + "\t" + filesByTime.get(i));
        }
    }
}
