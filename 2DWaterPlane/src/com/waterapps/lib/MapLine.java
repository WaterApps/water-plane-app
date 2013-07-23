package com.waterapps.lib;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.waterapps.waterplane.MainActivity;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Steve on 7/23/13.
 */
public class MapLine {
    Polyline polyline;
    Marker minMarker;
    Marker maxMarker;
    Marker primaryMarker;
    static GoogleMap map;

    public MapLine(Polyline line) {
        DecimalFormat df = new DecimalFormat("#.#");
        polyline = line;
        ElevationPoint minPoint = MainActivity.getDemData().getMinLine(polyline.getPoints());
        ElevationPoint maxPoint = MainActivity.getDemData().getMaxLine(polyline.getPoints());

        Log.d("min elevation", Double.toString(minPoint.getElevation()));
        Log.d("min location", minPoint.getLocation().toString());

        minMarker = map.addMarker(new MarkerOptions()
                .position(minPoint.getLocation())
                .title("Min elevation along line: " + df.format(minPoint.getElevation())));
        maxMarker = map.addMarker(new MarkerOptions()
                .position(maxPoint.getLocation())
                .title("Max elevation along line: " + df.format(maxPoint.getElevation())));
        primaryMarker = map.addMarker(new MarkerOptions()
                .position(centerOfLine(polyline))
                .title("Line"));
    }

    public static void setMap(GoogleMap newMap) {
        map = newMap;
    }

    LatLng centerOfLine(Polyline line) {
        List<LatLng> linePoints = polyline.getPoints();
        if(linePoints.size() == 2) {
            return centerOfPoints(linePoints.get(0), linePoints.get(1));
        }
        LatLng point1 = linePoints.get(linePoints.size()/2);
        LatLng point2 = linePoints.get((linePoints.size()/2)+1);
        return centerOfPoints(point1, point2);
    }

    LatLng centerOfPoints(LatLng p1, LatLng p2) {
        return new LatLng((p1.latitude+p2.latitude)/2.0, (p1.longitude+p2.longitude)/2.0);
    }

}
