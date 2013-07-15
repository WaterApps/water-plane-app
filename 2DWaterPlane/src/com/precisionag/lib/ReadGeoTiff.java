package com.precisionag.lib;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.tiffdecoder.TiffDecoder;

import java.io.File;
import java.net.URI;

import static com.tiffdecoder.TiffDecoder.nativeTiffGetCornerLatitude;
import com.ibm.util.CoordinateConversion;

/**
 * Created by steve on 5/30/13.
 */
public class ReadGeoTiff implements ReadElevationRaster {

    public ElevationRaster readFromFile(URI fileUri) {
        float nodata = -9999;
        Log.i("readgeotiff", "beginning of read function");
        ElevationRaster raster = new ElevationRaster();
        raster.setMaxElevation(Float.NEGATIVE_INFINITY);
        raster.setMinElevation(Float.POSITIVE_INFINITY);
        Log.i("readgeotiff", "line before first libtiff call");
        TiffDecoder.nativeTiffOpen(fileUri.getPath());
        Log.i("readgeotiff", "line after first libtiff call");
        raster.setDimensions(TiffDecoder.nativeTiffGetWidth(), TiffDecoder.nativeTiffGetHeight());
        Log.i("readgeotiff", "line before reading in floats");
        float[] pixels = TiffDecoder.nativeTiffGetFloats();
        Log.i("readgeotiff", "line after reading in floats");
        Log.i("readgeotiff", "Actual reading part");
        Log.i("readgeotiff", Integer.toString(raster.getNcols()));
        Log.i("readgeotiff", Integer.toString(raster.getNrows()));
        raster.elevationData = new float[raster.getNrows()][raster.getNcols()];

        //Log.i("longitude puru", Float.toString(com.tiffdecoder.TiffDecoder.nativeTiffGetCornerLongitude()));
        //Log.i("latitude puru", Float.toString(com.tiffdecoder.TiffDecoder.nativeTiffGetCornerLatitude()));
        /*
        for(int i=0; i<raster.getNrows(); i++) {
            for(int j=0; j<raster.getNcols(); j++) {
                raster.elevationData[raster.getNrows()-1-i][raster.getNcols()-1-j] = pixels[j+(raster.getNcols()*i)];
                if (Math.abs(raster.getElevationData()[raster.getNrows()-1-i][raster.getNcols()-1-j]-nodata) > 5.0) {
                    if (raster.getElevationData()[raster.getNrows()-1-i][raster.getNcols()-1-j] < raster.getMinElevation()) raster.setMinElevation(raster.getElevationData()[raster.getNrows()-1-i][raster.getNcols()-1-j]);
                    if (raster.getElevationData()[raster.getNrows()-1-i][raster.getNcols()-1-j] > raster.getMaxElevation()) raster.setMaxElevation(raster.getElevationData()[raster.getNrows()-1-i][raster.getNcols()-1-j]);
                }
            }
        }
        */

        /*
        for(int i=0; i<raster.getNrows(); i++) {
            for(int j=0; j<raster.getNcols(); j++) {
                raster.elevationData[i][j] = pixels[j+(raster.getNcols()*i)];
                if (Math.abs(raster.getElevationData()[i][j]-nodata) > 5.0) {
                    if (raster.getElevationData()[i][j] < raster.getMinElevation()) raster.setMinElevation(raster.getElevationData()[i][j]);
                    if (raster.getElevationData()[i][j] > raster.getMaxElevation()) raster.setMaxElevation(raster.getElevationData()[i][j]);
                }
            }
        }
        */

        for(int i=0; i<raster.getNrows(); i++) {
            for(int j=0; j<raster.getNcols(); j++) {
                raster.elevationData[i][raster.getNcols()-1-j] = pixels[j+(raster.getNcols()*i)];
                //Log.i("Elevation", Float.toString(raster.elevationData[i][raster.getNcols()-1-j]));
                if (Math.abs(raster.getElevationData()[i][raster.getNcols()-1-j]-nodata) > 5.0) {
                    if (raster.getElevationData()[i][raster.getNcols()-1-j] < raster.getMinElevation()) raster.setMinElevation(raster.getElevationData()[i][raster.getNcols()-1-j]);
                    if (raster.getElevationData()[i][raster.getNcols()-1-j] > raster.getMaxElevation()) raster.setMaxElevation(raster.getElevationData()[i][raster.getNcols()-1-j]);
                }
            }
        }

        Log.i("min elevation", Float.toString(raster.getMinElevation()));
        Log.i("max elevation", Float.toString(raster.getMaxElevation()));
        //float []anchor = nativeTiffGetCornerLatitude();
        float longitude = TiffDecoder.nativeTiffGetCornerLongitude();
        float latitude = TiffDecoder.nativeTiffGetCornerLatitude();
        double latLng[];
        CoordinateConversion conversion = new CoordinateConversion();
        latLng = conversion.utm2LatLon("16 N " + Integer.toString((int)longitude) + " " + Integer.toString((int)latitude));
        double scale = TiffDecoder.nativeTiffGetScale();
        double width = scale*raster.getNrows()/(111111.0);
        double height = scale*raster.getNcols()/(111111.0*Math.cos(Math.toRadians(latLng[0])));
        raster.setLowerLeft(new LatLng(latLng[0]-width, latLng[1]));
        raster.setUpperRight(new LatLng(latLng[0], latLng[1]+height));

        return raster;
    }

}
