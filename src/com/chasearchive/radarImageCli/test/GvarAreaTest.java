package com.chasearchive.radarImageCli.test;

import ucar.nc2.NetcdfFile;

import java.io.IOException;

public class GvarAreaTest {
    public static void main(String[] args) throws IOException {
        NetcdfFile file = NetcdfFile.open("test-files/CLASS GVAR Test/g12.2004.150.20");

        System.out.println(file);
    }
}
