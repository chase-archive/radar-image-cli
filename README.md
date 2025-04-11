# Radar Image CLI

### Author: Amelia Urquhart

This program automatically generates radar images using the time, latitude, and longitude from any given case in the Chase Archive. Currently, this only works for cases in the Contiguous US. The command line interface can be used as follows:

`java -jar RadarImageCLI.jar -dt 20240503_0015 -lat 32.61 -lon -99.86 -o radargen-case-hawleyTx.png`

There are four mandatory flags and four additional optional flags that can be used to further customize the resulting image.

| Flag  | Description |
| ------------- | ------------- |
| -dt  | UTC date and time of the case. Formatted as yyyymmdd_hhmm.  |
| -lat  | Latitude of case, in degrees north.  |
| -lon  | Longitude of case, in degrees east.  |
| -o | Output folder name. Folder is made in the same directory as the JAR file. |
| -a (optional) | Aspect ratio of image. Options are "1:1", "4:3", "3:2", and "16:9". <br> Default is "3:2". |
| -m (optional) | "Moment" from the NEXRAD file. <br> Options are "BR" (base reflectivity) and "BV" (base velocity). <br> Default is "BR". |
| -s (optional) | Size of image, in degrees of arc from the center of the image to the edge. <br> Default is 0.5. |
| -c (optional) | Source of data. <br> Options are "NEXRAD" and "MRMS". <br> Default is "NEXRAD". |
| -r (optional) | Resolution of image, in pixels. Default is 1080. |
| -lyr (optional) | Whether to export as one image or as individual layers. <br> Options are "COMPOSITE", "SEPARATE", and "BOTH". <br> Default is "COMPOSITE".  |

A more detailed CLI call may look like this:

`java -jar RadarImageCLI.jar -dt 20240430_2353 -lat 34.86 -lon -98.96 -a 4:3 -m BV -r 720 -s 2.0 -lyr BOTH -o radargen-case-rooseveltOk.png`

NOTES: 
* This prototype is built in Java, since most of my already-existing code was in Java and the reuse of that code allowed for faster development. I am considering switching from Java to C++ later on for the best possible speed.
* This program downloads two large data files per run, and the quality of the network connection will have a dramatic impact on execution times. While using a wireless connection from WIFI@OU in the National Weather Center, the code usually takes 15-20 seconds to run.

## Satellite Image CLI

The satellite image CLI, which has been made as a part of this repository due to a very large amount of shared code, functions in an extremely similar manner to the radar image CLI. Only a few small differences exist between calls to the two CLIs. A basic call might look like this:

`java -jar SatelliteImageCLI.jar -dt 20240503_0015 -lat 32.61 -lon -99.86 -o satgen-case-hawleyTx.png`

There are four mandatory flags and four additional optional flags that can be used to further customize the resulting image.

| Flag  | Description |
| ------------- | ------------- |
| -dt  | UTC date and time of the case. Formatted as yyyymmdd_hhmm.  |
| -lat  | Latitude of case, in degrees north.  |
| -lon  | Longitude of case, in degrees east.  |
| -o | Output folder name. Folder is made in the same directory as the JAR file. |
| -a (optional) | Aspect ratio of image. Options are "1:1", "4:3", "3:2", and "16:9". <br> Default is "3:2". |
| -t (optional) | Type of satellite image to plot. <br> Options are "VIS" (Visible/Geocolor) and "LIR" (Longwave IR). <br> Default is "VIS". |
| -s (optional) | Size of image, in degrees of arc from the center of the image to the edge. <br> Default is 0.5. |
| -r (optional) | Resolution of image, in pixels. Default is 1080. |
| -lyr (optional) | Whether to export as one image or as individual layers. <br> Options are "COMPOSITE", "SEPARATE", and "BOTH". <br> Default is "COMPOSITE".  |

A more detailed CLI call may look like this:

`java -jar RadarImageCLI.jar -dt 20240430_2353 -lat 34.86 -lon -98.96 -a 4:3 -t LIR -r 720 -s 2.0 -lyr BOTH -o satgen-case-rooseveltOk-LIR.png`
