package com.waterapps.lib;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Steve on 2/27/14.
 */
public class GeoUtils {
    public static LatLngBounds makeSquare(LatLng center, float s) {
        double metersPerDegree = 111222.0;
        double metersPerDegreeLat = 111132.92 - 559.82 * Math.cos(2*(center.latitude*Math.PI/180.0)) + 1.175*Math.cos(4*(center.latitude*Math.PI/180.0));
        double metersPerDegreeLong = 111412.84 * Math.cos(center.latitude*Math.PI/180.0) - 93.5 * Math.cos(3*center.latitude*Math.PI/180.0);
        float r = s/2;
        double north = center.latitude + (r/metersPerDegreeLat);
        double south = center.latitude - (r/metersPerDegreeLat);
        double west = center.longitude - (r/(metersPerDegreeLong));
        double east = center.longitude + (r/(metersPerDegreeLong));
        return new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
    }

    public static LatLngBounds makeRectangle(LatLng center, float s, float aspect) {
        double metersPerDegree = 111222.0;
        double metersPerDegreeLat = 111132.92 - 559.82 * Math.cos(2*(center.latitude*Math.PI/180.0)) + 1.175*Math.cos(4*(center.latitude*Math.PI/180.0));
        double metersPerDegreeLong = 111412.84 * Math.cos(center.latitude*Math.PI/180.0) - 93.5 * Math.cos(3*center.latitude*Math.PI/180.0);
        float r = s/2;
        double north = center.latitude + (r/metersPerDegreeLat);
        double south = center.latitude - (r/metersPerDegreeLat);
        double west = center.longitude - aspect*(r/(metersPerDegreeLong));
        double east = center.longitude + aspect*(r/(metersPerDegreeLong));
        return new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
    }

    /**
     * Calculates distance between two LatLng objects
     * @param p1 Point 1
     * @param p2 Point 2
     * @return Distance between Point 1 and Point 2
     */
    public static float distanceBetween(LatLng p1, LatLng p2) {
        double metersPerDegree = 111222.0;
        double longDistance = (p1.longitude-p2.longitude)*metersPerDegree*Math.cos((p1.latitude+p2.latitude)/2);
        double latDistance = (p1.latitude-p2.latitude)*metersPerDegree;
        return (float)Math.sqrt((latDistance*latDistance) + (longDistance*longDistance));
    }

    public static LatLng center(LatLngBounds area) {
        return new LatLng( (area.southwest.latitude+area.northeast.latitude )/2.0f,
                (area.southwest.longitude+area.northeast.longitude)/2.0f);

    }

    public static float getArea(LatLngBounds extent) {
        return getWidth(extent)*getHeight(extent);
    }

    public static float getWidth(LatLngBounds extent) {
        LatLng sw = extent.southwest;
        LatLng ne = extent.northeast;
        LatLng se = new LatLng(extent.southwest.latitude, extent.northeast.longitude);
        LatLng nw = new LatLng(extent.northeast.latitude, extent.southwest.longitude);

        return distanceBetween(sw, se);
    }

    public static float getHeight(LatLngBounds extent) {
        LatLng sw = extent.southwest;
        LatLng ne = extent.northeast;
        LatLng se = new LatLng(extent.southwest.latitude, extent.northeast.longitude);
        LatLng nw = new LatLng(extent.northeast.latitude, extent.southwest.longitude);

        return distanceBetween(sw, nw);
    }
}
