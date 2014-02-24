package com.waterapps.lib;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.tiffdecoder.TiffDecoder;

import java.net.URI;

import com.ibm.util.CoordinateConversion;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;

/**
 * Reads data from a GeoTIFF into a DemData object
 */
public class ReadGeoTiff implements ReadDemData {

    /**
     * Reads data from a GeoTIFF into a DemData object
     * @param fileUri File location to be read
     * @return The DEM data
     */
    public DemData readFromFile(URI fileUri) {
        DemData raster = new DemData();
        raster.setMaxElevation(Float.NEGATIVE_INFINITY);
        raster.setMinElevation(Float.POSITIVE_INFINITY);
        TiffDecoder.nativeTiffOpen(fileUri.getPath());
        raster.setDimensions(TiffDecoder.nativeTiffGetWidth(), TiffDecoder.nativeTiffGetHeight());
        float[] pixels = TiffDecoder.nativeTiffGetFloats();
        raster.elevationData = new float[raster.getNrows()][raster.getNcols()];
        String noDataString = TiffDecoder.nativeTiffGetNoData();
        //float nodata = Float.parseFloat(noDataString);
        float nodata = -9999.0f;
        float lastGoodElevation = 0.0f;

        //loop throuogh each pixel, calculating min and max along the way and not setting nodata points
        for(int i=0; i<raster.getNrows(); i++) {
            for(int j=0; j<raster.getNcols(); j++) {
                raster.elevationData[i][raster.getNcols()-1-j] = pixels[j+(raster.getNcols()*i)];
                if (raster.getElevationData()[i][raster.getNcols()-1-j] != nodata ) {
                    lastGoodElevation = raster.elevationData[i][raster.getNcols()-1-j];
                    if (raster.getElevationData()[i][raster.getNcols()-1-j] < raster.getMinElevation()) raster.setMinElevation(raster.getElevationData()[i][raster.getNcols()-1-j]);
                    if (raster.getElevationData()[i][raster.getNcols()-1-j] > raster.getMaxElevation()) raster.setMaxElevation(raster.getElevationData()[i][raster.getNcols()-1-j]);
                }
                else {
                    raster.elevationData[i][raster.getNcols()-1-j] = lastGoodElevation;
                }

            }
        }

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
        double width = scaleX*raster.getNrows()/(111111.0);
        double height = scaleY*raster.getNcols()/(111111.0*Math.cos(Math.toRadians(latLng[0])));
        raster.setSouthWest(new LatLng(latLng[0] - width, latLng[1]));
        raster.setNorthEast(new LatLng(latLng[0], latLng[1] + height));
        return raster;
    }

}
