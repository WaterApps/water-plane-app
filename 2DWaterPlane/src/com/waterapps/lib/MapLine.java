package com.waterapps.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

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
    double minElevation;
    double maxElevation;
    float dpWidth;
    float dpHeight;
    List<ElevationPoint> points;
    long prevTime;
    Bitmap lines;

    public MapLine(Polyline line, ArrayList<Marker> joints) {
        DecimalFormat df = new DecimalFormat("#.#");
        polyline = line;
        this.joints = joints;
        points = MainActivity.getDemData().getLineElevations(polyline.getPoints());

        //set min and max to +/- infinity
        ElevationPoint minPoint = new ElevationPoint(Double.POSITIVE_INFINITY, new LatLng(0.0, 0.0));
        ElevationPoint maxPoint = new ElevationPoint(Double.NEGATIVE_INFINITY, new LatLng(0.0, 0.0));
        Iterator<ElevationPoint> iterator = points.iterator();
        ElevationPoint point;

        //find min and max elevation points
        while (iterator.hasNext()) {
            point = iterator.next();
            minPoint = point.getElevation() < minPoint.getElevation() ? point : minPoint;
            maxPoint = point.getElevation() > minPoint.getElevation() ? point : maxPoint;
        }

        minElevation = minPoint.getElevation();
        maxElevation = maxPoint.getElevation();

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
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.linemarker)));
        prevTime = SystemClock.elapsedRealtime();
        lines = drawLines();
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

    /**
     * Draws the side profile to a bitmap
     */
    public Bitmap drawLines() {
        int width = MainActivity.getMapWidth();
        int height =  MainActivity.getMapHeight();
        //sets dimensions to square of whichever side is smallest
        width = height = width < height ? width : height;
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.TRANSPARENT);
        Canvas c = new Canvas(b);
        dpWidth = pixelToDP(width);
        dpHeight = pixelToDP(height);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(5.0f);

        float dpPerPoint = dpWidth/points.size();
        float x = 0.0f;

        //draw the elevation points
        p.setColor(Color.BLACK);
        Iterator<ElevationPoint> iter = points.iterator();
        float y = elevationToDP(iter.next().getElevation());
        float prevY = 0.0f;
        while (iter.hasNext()) {
            prevY = y;
            y = elevationToDP(iter.next().getElevation());
            c.drawLine(x, prevY, x+dpPerPoint, y, p);
            x+=dpPerPoint;
        }

        return b;
    }

    /**
     * Draws a side profile of the line
     */
    public void drawProfile() {
        prevTime = SystemClock.elapsedRealtime();
        int width = MainActivity.getMapWidth();
        int height =  MainActivity.getMapHeight();
        //sets dimensions to square of whichever side is smallest
        width = height = width < height ? width : height;
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.TRANSPARENT);
        Canvas c = new Canvas(b);
        dpWidth = pixelToDP(width);
        dpHeight = pixelToDP(height);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(5.0f);

        float dpPerPoint = dpWidth/points.size();
        float x = 0.0f;

        //show current water level
        p.setColor(Color.BLUE);
        if(MainActivity.waterLevelMeters > minElevation) {
            c.drawRect(0.0f, elevationToDP(MainActivity.waterLevelMeters), dpWidth, dpHeight, p);
        }

        //show current user elevation
        p.setColor(Color.RED);
        c.drawLine(0.0f, elevationToDP(MainActivity.getUserElevation()), dpWidth, elevationToDP(MainActivity.getUserElevation()), p);

        //draw the elevation points
        c.drawBitmap(lines, 0.0f, 0.0f, p);

        //show fps
        long time = SystemClock.elapsedRealtime();
        long deltaT = time - prevTime;
        prevTime = time;
        int fps = (int)(1000/deltaT);
        //c.drawText(Integer.toString(fps), 0.0f, 20.0f, p);

        MainActivity.iv.setImageBitmap(b);
    }

    private float pixelToDP(int px) {
        return 2.0f*(float)px/MainActivity.getResource().getDisplayMetrics().density;
    }

    private float elevationToDP(double elevation) {
        return dpHeight-(float)(dpHeight*(elevation-minElevation)/(maxElevation-minElevation));
    }

}
