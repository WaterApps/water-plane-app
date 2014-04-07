package com.waterapps.lib;

import android.net.Uri;

import com.ibm.util.CoordinateConversion;
import com.tiffdecoder.TiffDecoder;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;

import java.io.File;
import java.net.URI;
import java.text.DateFormat;

/**
 * Reads metadata from a GeoTIFF file into a DemFile object
 */
public class ReadGeoTiffMetadata {
    public static DemFile readMetadata(File file) {
        String fileName = file.getPath();
        DateFormat df = DateFormat.getDateInstance();
        String timeStamp = df.format(file.lastModified());
        URI fileUri = URI.create(Uri.fromFile(file).toString());
        float sw_lat = 0.0f, sw_long = 0.0f, ne_lat = 0.0f, ne_long = 0.0f;

        return new DemFile(GdalUtils.getLatLngBounds(fileUri.getPath()), fileName, timeStamp, fileUri);
    }
}
