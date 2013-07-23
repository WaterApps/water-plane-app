package com.waterapps.lib;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.waterapps.waterplane.MainActivity;
import com.waterapps.waterplane.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains a polyline and some markers with useful information
 */
public class MapLine {
    Polyline polyline;
    Marker minMarker;
    Marker maxMarker;
    Marker primaryMarker;
    ArrayList<Marker> joints;
    static GoogleMap map;
    private static Marker selected;

    public MapLine(Polyline line, ArrayList<Marker> joints) {
        DecimalFormat df = new DecimalFormat("#.#");
        polyline = line;
        this.joints = joints;
        ElevationPoint minPoint = MainActivity.getDemData().getMinLine(polyline.getPoints());
        ElevationPoint maxPoint = MainActivity.getDemData().getMaxLine(polyline.getPoints());

        Log.d("min elevation", Double.toString(minPoint.getElevation()));
        Log.d("min location", minPoint.getLocation().toString());

        minMarker = map.addMarker(new MarkerOptions()
                .position(minPoint.getLocation())
                .title("Min elevation along line: " + df.format(minPoint.getElevation()) + "m")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker))
                .anchor(0.5f,0.5f));
        maxMarker = map.addMarker(new MarkerOptions()
                .position(maxPoint.getLocation())
                .title("Max elevation along line: " + df.format(maxPoint.getElevation()) + "m")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker))
                .anchor(0.5f, 0.5f));
        primaryMarker = map.addMarker(new MarkerOptions()
                .position(centerOfLine(polyline))
                .title("Line")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.star)));
    }

    public static void setMap(GoogleMap newMap) {
        map = newMap;
    }

    LatLng centerOfLine(Polyline line) {
        List<LatLng> linePoints = polyline.getPoints();
        if(linePoints.size() == 2) {
            return centerOfPoints(linePoints.get(0), linePoints.get(1));
        }
        //if odd number of points, use the middle point
        if(linePoints.size()%2 == 1) {
            return linePoints.get(linePoints.size()/2);
        }
        LatLng point1 = linePoints.get(linePoints.size()/2);
        LatLng point2 = linePoints.get((linePoints.size()/2)+1);
        return centerOfPoints(point1, point2);
    }

    private LatLng centerOfPoints(LatLng p1, LatLng p2) {
        return new LatLng((p1.latitude+p2.latitude)/2.0, (p1.longitude+p2.longitude)/2.0);
    }

    public static void setSelected(Marker marker) {
        selected = marker;
    }

    public static Marker getSelected() {
        return selected;
    }

    public Marker getMarker() {
        return primaryMarker;
    }

    /**
     * Deletes the line and associated markers
     */
    public void remove() {
        polyline.remove();
        minMarker.remove();
        maxMarker.remove();
        primaryMarker.remove();
        polyline = null;
        minMarker = null;
        maxMarker = null;
        primaryMarker = null;
        Iterator<Marker> iterator = joints.iterator();
        while (iterator.hasNext()) {
            iterator.next().remove();
        }
        joints = null;
    }

}
