package com.chasearchive.radarImageCli.satellite;

import com.ameliaWx.wxArchives.earthWeather.goes.SatelliteSector;
import org.joda.time.DateTime;

import java.io.File;

import static com.chasearchive.radarImageCli.satellite.GoesMcfetchCutoffs.*;

public class McfetchData {
    public static File[] downloadGoes(DateTime time, SatelliteSource source) throws NoMcfetchFileFoundException {
        if(source == SatelliteSource.GOES_EAST_MCFETCH) {
            //datetime decision tree
            if(!time.isBefore(GOES_14_TEMP2_END)) {
                // do goes 13
            } else if(!time.isBefore(GOES_14_TEMP2_START)) {
                // do goes 14
            } else if(!time.isBefore(GOES_14_TEMP1_END)) {
                // do goes 13
            } else if(!time.isBefore(GOES_14_TEMP1_START)) {
                // do goes 14
            } else if(!time.isBefore(GOES_12_13_CUTOFF)) {
                // do goes 13
            } else if(!time.isBefore(GOES_8_12_CUTOFF)) {
                // do goes 12
            } else if(!time.isBefore(GOES_8_START)) {
                // do goes 8
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
}