package com.waterapps.lib;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

/**
 * Created by Steve on 4/6/2014.
 */
public class GdalUtils {
    static LatLngBounds getLatLngBounds(String filename) {
        //initialize GDAL by loading drivers
        gdal.AllRegister();
        ogr.RegisterAll();
        //open up the TIFF as a GDAL dataset
        Dataset dataset = gdal.Open(filename);
        double[] geotrans = dataset.GetGeoTransform();

        double north = dataset.GetGeoTransform()[3]; // returns 4304781
        double west = dataset.GetGeoTransform()[0];  // returns 537897
        double south = north - (dataset.getRasterYSize() * dataset.GetGeoTransform()[1]); // returns 4303770
        double east = west + (dataset.getRasterXSize() * dataset.GetGeoTransform()[1]); // returns 539292

        SpatialReference inputSR = new SpatialReference(dataset.GetProjection());
        SpatialReference wgsSR = new SpatialReference("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");
        CoordinateTransformation trans = CoordinateTransformation.CreateCoordinateTransformation(inputSR, wgsSR);
        double[] northEast = trans.TransformPoint(east, north);
        double[] southWest = trans.TransformPoint(west, south);
        LatLng northEastLL = new LatLng(northEast[1], northEast[0]); // (38.89102355592375, -86.54693372142856)
        LatLng southWestLL = new LatLng(southWest[1], southWest[0]); // (38.88197446432154, -86.56307448405549)

        return new LatLngBounds(southWestLL, northEastLL);
    }
}
