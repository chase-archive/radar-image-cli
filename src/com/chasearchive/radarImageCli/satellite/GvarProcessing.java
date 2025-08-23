package com.chasearchive.radarImageCli.satellite;

public class GvarProcessing {
    // Source for values used:
    // https://ospo.noaa.gov/operations/goes/gvar-conversion.html

    // Values for GOES-8 through -11
    private static float GOES_8_CH_2_M = 227.3889f;
    private static float GOES_8_CH_3_M = 38.8383f;
    private static float GOES_8_CH_4_M = 5.2285f;
    private static float GOES_8_CH_5_M = 5.0273f;
    private static float GOES_8_CH_2_B = 68.2167f;
    private static float GOES_8_CH_3_B = 29.1287f;
    private static float GOES_8_CH_4_B = 15.6854f;
    private static float GOES_8_CH_5_B = 15.3332f;

    // Values for GOES-12 through (presumably) -15
    // The site only lists -12 and -O, will see if this behaves okay on 13 and 15
    //
    // "They depend on the channel selected, but for a given channel they are
    // constant for all time and are the same for all satellites of the series."
    // Seems like a good enough justification to me
    private static float GOES_12_CH_2_M = 227.3889f;
    private static float GOES_12_CH_3_M = 38.8383f;
    private static float GOES_12_CH_4_M = 5.2285f;
    private static float GOES_12_CH_5_M = 5.5297f;
    private static float GOES_12_CH_2_B = 68.2167f;
    private static float GOES_12_CH_3_B = 29.1287f;
    private static float GOES_12_CH_4_B = 15.6854f;
    private static float GOES_12_CH_5_B = 16.5892f;

    // Where are the band 1 values? Good question.
    // Our robotic overlord says this should be roughly correct so I'll try it
    // I might try to avoid actually using these values though
    private static float GOES_8_CH_1_M = 1.5f;
    private static float GOES_8_CH_1_B = 70.0f;
    private static float GOES_12_CH_1_M = 1.5f;
    private static float GOES_12_CH_1_B = 70.0f;

    // This basically just makes it easier to clamp the minimum values to zero.
    // I'll admit it's lazy but it'll work perfectly anyway.
    // Maybe I wrote that last line with too much hubris.
    public float spectralRadiance(float gvar, int band, String satelliteId) {
        float R = spectralRadianceRaw(gvar, band, satelliteId);

        return (R < 0) ? 0 : R;
    }

    private float spectralRadianceRaw(float gvar, int band, String satelliteId) {
        // Only two possible values, 8 or 12
        int series = -1;

        satelliteId = satelliteId.trim();
        if("GOES-8".equals(satelliteId) || "GOES-9".equals(satelliteId) ||
                "GOES-10".equals(satelliteId) || "GOES-11".equals(satelliteId)) {
            series = 8;
        } else if("GOES-12".equals(satelliteId) || "GOES-13".equals(satelliteId) ||
                    "GOES-14".equals(satelliteId) || "GOES-15".equals(satelliteId)) {
            series = 12;
        }

        switch(series) {
            case 8:
                switch (band) {
                    case 1:
                        return (gvar - GOES_8_CH_1_B)/GOES_8_CH_1_M;
                    case 2:
                        return (gvar - GOES_8_CH_2_B)/GOES_8_CH_2_M;
                    case 3:
                        return (gvar - GOES_8_CH_3_B)/GOES_8_CH_3_M;
                    case 4:
                        return (gvar - GOES_8_CH_4_B)/GOES_8_CH_4_M;
                    case 5:
                        return (gvar - GOES_8_CH_5_B)/GOES_8_CH_5_M;
                    default:
                        return -1024;
                }
            case 12:
                switch (band) {
                    case 1:
                        return (gvar - GOES_12_CH_1_B)/GOES_12_CH_1_M;
                    case 2:
                        return (gvar - GOES_12_CH_2_B)/GOES_12_CH_2_M;
                    case 3:
                        return (gvar - GOES_12_CH_3_B)/GOES_12_CH_3_M;
                    case 4:
                        return (gvar - GOES_12_CH_4_B)/GOES_12_CH_4_M;
                    case 5:
                        return (gvar - GOES_12_CH_5_B)/GOES_12_CH_5_M;
                    default:
                        return -1024;
                }
            default:
                return -1024;
        }
    }
}
