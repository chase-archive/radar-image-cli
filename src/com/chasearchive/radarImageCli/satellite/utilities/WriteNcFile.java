package com.chasearchive.radarImageCli.satellite.utilities;

import ucar.nc2.*;
import ucar.ma2.*;

public class WriteNcFile {
    public static void main(String[] args) {
        try {
            // Define the file to create
            String filename = "example.nc";

            // Create a new NetCDF file (create a new one if it doesn't exist)
            NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, filename);

            // Create a dimension
            Dimension timeDim = writer.addDimension(null, "time", 10);  // Dimension "time" of length 10

            // Create variables (in this case, a 1D variable for data)
//            Variable timeVar = writer.addVariable(null, "time", DataType.INT, "time");
//            writer.addVariableAttribute(timeVar, "units", "seconds since 2000-01-01");

            // Create the file structure
            writer.create();

            // Prepare data to write to the variable
            Array timeData = Array.factory(DataType.INT, new int[]{10}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

            // Write data to the variable
//            writer.write(timeVar, timeData);

            // Close the writer (this saves the changes to the file)
            writer.close();
            System.out.println("NetCDF file created and data written successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
