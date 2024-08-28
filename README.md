# Radar Image CLI

### Author: Amelia Urquhart

This program automatically generates radar images using the time, latitude, and longitude from any given case in the Chase Archive. Currently, this only works for cases in the Contiguous US. The command line interface can be used as follows:

`java -jar RadarImageCLI.jar -dt 20240503_0015 -lat 32.606786 -lon -99.855 -o radargen-case-hawleyTx.png`

There are four mandatory flags and four additional optional flags that can be used to further customize the resulting image.

| Flag  | Description |
| ------------- | ------------- |
| -dt  | UTC date and time of the case. Formatted as yyyymmdd_hhmm.  |
| -lat  | Latitude of case, in degrees north.  |
| -lon  | Longitude of case, in degrees east.  |
| -o | Output file name. File is made in the same directory as the JAR file. |
| -a (optional) | Aspect ratio of image. Options are "1:1", "4:3", and "16:9". <br> Default is "1:1". |
| -m (optional) | "Moment" from the NEXRAD file. <br> Options are "BR" (base reflectivity) and "BV" (base velocity). <br> Default is "BR". |
| -s (optional | Size of image, in degrees of arc from the center of the image to the edge. <br> Default is 0.5. |
| -r (optional) | Resolution of image, in pixels. Default is 1080. |

A more detailed CLI call may look like this:

`java -jar RadarImageCLI.jar -dt 20240430_2353 -lat 34.86 -lon -98.96 -a 4:3 -m BV -r 720 -s 2.0 -o radargen-case-rooseveltOk.png`

NOTES: 
* This prototype is built in Java, since most of my already-existing code was in Java and the reuse of that code allowed for faster development. I am considering switching from Java to C++ later on for the best possible speed.
* This program downloads two large data files per run, and the quality of the network connection will have a dramatic impact on execution times. While using a wireless connection from WIFI@OU in the National Weather Center, the code usually takes 15-20 seconds to run.

