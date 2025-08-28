package com.chasearchive.radarImageCli.satellite;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class GoesMcfetchCutoffs {
    // GOES-EAST sats
    static final DateTime GOES_8_START = new DateTime(1994, 9, 1, 0, 0, DateTimeZone.UTC);
    static final DateTime GOES_8_12_CUTOFF = new DateTime(2003, 2, 1, 0, 0, DateTimeZone.UTC);
    static final DateTime GOES_12_13_CUTOFF = new DateTime(2010, 2, 1, 0, 0, DateTimeZone.UTC);
    static final DateTime GOES_14_TEMP1_START = new DateTime(2012, 8, 24, 0, 0, DateTimeZone.UTC);
    static final DateTime GOES_14_TEMP1_END = new DateTime(2012, 10, 19, 0, 0, DateTimeZone.UTC);
    static final DateTime GOES_14_TEMP2_START = new DateTime(2013, 5, 23, 0, 0, DateTimeZone.UTC);
    static final DateTime GOES_14_TEMP2_END = new DateTime(2013, 6, 23, 0, 0, DateTimeZone.UTC); // approximate

    // GOES-WEST sats
    static final DateTime GOES_9_START = new DateTime(1996, 1, 1, 0, 0, DateTimeZone.UTC);
    static final DateTime GOES_9_10_CUTOFF = new DateTime(1998, 6, 1, 0, 0, DateTimeZone.UTC);
    static final DateTime GOES_10_11_CUTOFF = new DateTime(2006, 6, 1, 0, 0, DateTimeZone.UTC);
    static final DateTime GOES_11_15_CUTOFF = new DateTime(2011, 8, 1, 0, 0, DateTimeZone.UTC);
}
