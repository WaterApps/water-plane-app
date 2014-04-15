package com.waterapps.lib;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.tiffdecoder.TiffDecoder;

import java.net.URI;

import com.ibm.util.CoordinateConversion;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

/**
 * Reads data from a GeoTIFF into a DemData object
 */
public class ReadGeoTiff implements ReadDemData {

    /**
     * Reads data from a GeoTIFF into a DemData object
     * @param filename File location to be read
     * @return The DEM data
     */
    public DemData readFromFile(String filename) {
        DemData raster = new DemData();
        raster.setMaxElevation(Float.NEGATIVE_INFINITY);
        raster.setMinElevation(Float.POSITIVE_INFINITY);
        TiffDecoder.nativeTiffOpen(filename);
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

        LatLngBounds demBounds = GdalUtils.getLatLngBounds(filename);

        raster.setSouthWest(demBounds.southwest);
        raster.setNorthEast(demBounds.northeast);
        return raster;
    }

}
