package com.waterapps.lib;

import com.google.android.gms.maps.model.LatLng;

/**
 * Associates an elevation with a LatLng location
 */
public class ElevationPoint implements Comparable<ElevationPoint> {
    private LatLng location;
    private double elevation;

    ElevationPoint(double elevation, LatLng location) {
        this.setLocation(location);
        this.setElevation(elevation);
    }


    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    @Override
    public int compareTo(ElevationPoint o) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
        if(this.elevation < o.getElevation()) {
            return BEFORE;
        }
        else if(this.elevation > o.getElevation()) {
            return AFTER;
        }
        else return EQUAL;
    }
}
