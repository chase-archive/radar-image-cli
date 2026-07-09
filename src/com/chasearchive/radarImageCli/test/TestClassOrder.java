package com.chasearchive.radarImageCli.test;

import com.chasearchive.radarImageCli.nceiClass.NceiClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TestClassOrder {
    public static void main(String[] args) throws IOException {
        ArrayList<File> files = NceiClass.getFilesInOrder("8559148304");

        System.out.println(files.size());

        for (File f : files) {
            System.out.println(f.getAbsolutePath());
        }
//        NceiClass.downloadClassOrder("8559148304");
    }
}
