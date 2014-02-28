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

        TiffDecoder.nativeTiffOpen(fileUri.getPath());

        //read latitude and longitude (assumed to the UTM format)
        float longitude = TiffDecoder.nativeTiffGetCornerLongitude();
        float latitude = TiffDecoder.nativeTiffGetCornerLatitude();
        double latLng[];

        //convert from UTM->LatLng as GMaps expects
        CoordinateConversion conversion = new CoordinateConversion();
        //String UTM = TiffDecoder.nativeTiffGetParams();
        //String UTMZone = UTM.substring(18, 20).concat(" ").concat(UTM.substring(20, 21)).concat(" ");

        //figure out UTM zone
        //gdal returns positive zone for north hemisphere, negative for south
        String UTMZone;
        gdal.AllRegister();
        ogr.RegisterAll();
        Dataset ds = gdal.Open(fileUri.getPath());
        int zone = new SpatialReference(ds.GetProjectionRef()).GetUTMZone();
        if (zone > 0) {
            UTMZone = Integer.toString(zone) + " N ";
        } else {
            UTMZone = Integer.toString(-zone) + " S ";
        }
        latLng = conversion.utm2LatLon(UTMZone + Integer.toString((int)longitude) + " " + Integer.toString((int)latitude));
        double scaleX = TiffDecoder.nativeTiffGetScaleX();
        double scaleY = TiffDecoder.nativeTiffGetScaleY();

        //calculate position of northeast corner, as only southwest and scale are provided
        double width = scaleX*TiffDecoder.nativeTiffGetWidth()/(111111.0);
        double height = scaleY*TiffDecoder.nativeTiffGetHeight()/(111111.0*Math.cos(Math.toRadians(latLng[0])));
        return new DemFile((float)(latLng[0]-width), ((float)latLng[1]), (float)latLng[0], (float)(latLng[1]+height), fileName, timeStamp, fileUri);
    }
}
