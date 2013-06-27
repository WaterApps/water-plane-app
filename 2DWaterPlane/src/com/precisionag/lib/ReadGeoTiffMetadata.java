package com.precisionag.lib;

import java.io.File;
import java.text.DateFormat;

/**
 * Created by steve on 6/7/13.
 */
public class ReadGeoTiffMetadata {
    public static Dem readMetadata(File file) {
        String fileName = file.getName();
        DateFormat df = DateFormat.getDateInstance();
        String timeStamp = df.format(file.lastModified());
        float sw_lat = 0.0f, sw_long = 0.0f, ne_lat = 0.0f, ne_long = 0.0f;

        return new Dem(sw_lat, sw_long, ne_lat, ne_long, fileName, timeStamp);
    }
}
