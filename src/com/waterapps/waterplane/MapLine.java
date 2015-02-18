package com.waterapps.waterplane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.openatk.openatklib.atkmap.ATKMap;
import com.openatk.openatklib.atkmap.listeners.ATKPointClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointDragListener;
import com.openatk.openatklib.atkmap.models.ATKPoint;
import com.openatk.openatklib.atkmap.models.ATKPolyline;
import com.openatk.openatklib.atkmap.views.ATKPointView;
import com.openatk.openatklib.atkmap.views.ATKPolylineView;
import com.waterapps.waterplane.R;

/**
 * Contains a polyline and some markers with useful information
 */
public class MapLine {
    private ATKPolylineView polyline;
    private ArrayList<ATKPointView> vertices;
    private ATKPointView minMarker;
    private ATKPointView maxMarker;
    private ATKPointView primaryMarker;
    private static ATKPointView selected;
    private double minElevation;
    private double maxElevation;
    private float dpWidth;
    private float dpHeight;
    private List<ElevationPoint> points;
    private Bitmap lines;
    private int width;
    private int height;
    private ATKMap map;
    private DemDataWrapper demDataWrapper;
    /**
     *
     * @param currentLine google maps polyline object
     * @param lineJoints list of joints for the polyline
     */
    public MapLine(ATKMap map, DemDataWrapper demDataWrapper, LatLng point, int width, int height, ATKPointClickListener clickListener, ATKPointDragListener dragListener) {
    	this.demDataWrapper = demDataWrapper;
    	this.map = map;
    	this.width = demDataWrapper.getDemData().getElevationData()[0].length;
        this.height =  demDataWrapper.getDemData().getElevationData().length;
    	
        minElevation = MainActivity.sliderMin;
        maxElevation = MainActivity.sliderMax;
//    	DecimalFormat df = new DecimalFormat("#.#");
//      polyline = currentLine;
//      this.joints = lineJoints;
        
        // Create the polyline
        List<LatLng> list = new ArrayList<LatLng>();
    	list.add(point);
    	ATKPolyline tempPoly = new ATKPolyline("polyline", list);
    	polyline = map.addPolyline(tempPoly);
    	polyline.getAtkPolyline().viewOptions.setStrokeColor(Color.WHITE);
    	polyline.getAtkPolyline().viewOptions.setStrokeWidth(5.0f);
    	polyline.update();
    	
    	// Create the first joint marker.
    	ATKPoint newPoint = new ATKPoint("line joint", point);
		ATKPointView ptView = map.addPoint(newPoint);
		ptView.setAnchor(0.5f, 0.5f);
		ptView.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.dot), 100, 200);
		vertices = new ArrayList<ATKPointView>();
		vertices.add(ptView);
		ptView.update();
		
		// Add markers to the map		
		ATKPoint mnPoint = new ATKPoint("min line pt", point);
		minMarker = map.addPoint(mnPoint);
		minMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker), 100, 200);
		minMarker.setAnchor(0.5f, 0.5f);
		minMarker.update();

		
		ATKPoint mxPoint = new ATKPoint("max line pt", point);
		maxMarker = map.addPoint(mxPoint);
		maxMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker), 100, 200);
		maxMarker.setAnchor(0.5f, 0.5f);
		maxMarker.update();
		
		ATKPoint primaryPoint = new ATKPoint("Line", point);
		primaryMarker = map.addPoint(primaryPoint);
		primaryMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.linemarker), 100, 200);
		primaryMarker.setAnchor(0.5f, 0.5f);
		primaryMarker.setOnClickListener(clickListener);
        primaryMarker.setOnDragListener(dragListener);
        primaryMarker.setTitle("Line");
		primaryMarker.update();
	}
    
    public void update() {
        points = demDataWrapper.getLineElevations(polyline.getAtkPolyline().boundary);

    	//set min and max to +/- infinity
        ElevationPoint minPoint = new ElevationPoint(Double.POSITIVE_INFINITY, new LatLng(0.0, 0.0));
        ElevationPoint maxPoint = new ElevationPoint(Double.NEGATIVE_INFINITY, new LatLng(0.0, 0.0));
        
        Iterator<ElevationPoint> iterator = points.iterator();
        ElevationPoint point;

        //find min and max elevation points
        while (iterator.hasNext()) {
            point = iterator.next();
            minPoint = point.getElevation() < minPoint.getElevation() ? point : minPoint;
            maxPoint = point.getElevation() > maxPoint.getElevation() ? point : maxPoint;
        }

        minMarker.setPosition(minPoint.getLocation());
        minMarker.update();
        maxMarker.setPosition(maxPoint.getLocation());
        minMarker.update();
        primaryMarker.setPosition(centerOfLine(polyline));
        primaryMarker.update();
		
		// Generate bitmap
        lines = drawLines();
    }
    
    public void addPoint(LatLng point) {
    	// Add new joint
    	ATKPoint newPoint = new ATKPoint("line joint", point);
		ATKPointView ptView = map.addPoint(newPoint);
		ptView.setAnchor(0.5f, 0.5f);
		ptView.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.dot), 100, 200);
		ptView.update();
		vertices.add(ptView);
		
		// Add new polyline segment
	    map.removePolyline(polyline.getAtkPolyline());
    	polyline.getAtkPolyline().boundary.add(point);
    	map.addPolyline(polyline.getAtkPolyline());
    	polyline.getAtkPolyline().viewOptions.setStrokeColor(Color.WHITE);
    	polyline.getAtkPolyline().viewOptions.setStrokeWidth(5.0f);
    	polyline.update();
    	
    	//Update other mapline features (min, max, etc)
    	update();
    }
    
    public LatLng centerOfLine(ATKPolylineView polyline2) {
        List<LatLng> linePoints = polyline.getAtkPolyline().boundary;
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

    public ATKPointView getMarker() {
        return primaryMarker;
    }
    
    public ArrayList<ATKPointView> getVertices() {
    	return this.vertices;
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
        Iterator<ATKPointView> iterator = vertices.iterator();
        while (iterator.hasNext()) {
            iterator.next().remove();
        }
        vertices = null;
    }

    /**
     * Draws the side profile to a bitmap
     */
    public Bitmap drawLines() {
    	int tempHeight;
    	int tempWidth;
        tempWidth = tempHeight = width < height ? height : width;
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.TRANSPARENT);
        Canvas c = new Canvas(b);
        dpWidth = pixelToDP(tempWidth);
        dpHeight = pixelToDP(tempHeight);
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
        double el;
        while (iter.hasNext()) {
            prevY = y;
            el = iter.next().getElevation();
            Log.w("el", Double.toString(el));
            y = elevationToDP(el);
            c.drawLine(x, prevY, x+dpPerPoint, y, p);
            x+=dpPerPoint;
        }

        return b;
    }

    /**
     * Draws a side profile of the line
     */
    public void drawProfile(LatLng userLocation) {
    	double userElevation = demDataWrapper.getDemData().getElevationFromLatLng(userLocation);
        int tempWidth;// = demDataWrapper.getDemData().getElevationData()[0].length;
        int tempHeight;// =  demDataWrapper.getDemData().getElevationData().length;
        //sets dimensions to square of whichever side is largest
        tempWidth = tempHeight = width < height ? height : width;
        //create empty canvas and set up paint
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.TRANSPARENT);
        Canvas c = new Canvas(b);
        dpWidth = pixelToDP(tempWidth);
        dpHeight = pixelToDP(tempHeight);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(5.0f);

//        float dpPerPoint = dpWidth/points.size();
//        float x = 0.0f;

        //show current water level
        p.setColor(Color.BLUE);
        if(MainActivity.waterLevel > minElevation) {
            c.drawRect(0.0f, elevationToDP(MainActivity.waterLevel), dpWidth, dpHeight, p);
        }

        //show current user elevation
        p.setColor(Color.RED);
        c.drawLine(0.0f, elevationToDP(userElevation), dpWidth, elevationToDP(userElevation), p);

        //draw the elevation points
        c.drawBitmap(lines, 0.0f, 0.0f, p);

        //display on screen
        MainActivity.iv.setImageBitmap(b);
    }

    /**
     * converts raw pixel count to density-independent DP
     * @param px pixels
     * @return pixels converted to dp
     */
    private float pixelToDP(int px) {
        return 2.0f*(float)px/MainActivity.getResource().getDisplayMetrics().density;
    }

    /**
     * gives a location of a given elevation in DP in the bitmap
     * @param elevation elevation in meters
     * @return Y coordinate of pixel mapped to elevation
     */
    private float elevationToDP(double elevation) {
    	Log.w("what is this returning", Float.toString(dpHeight-(float)(dpHeight*(elevation-minElevation)/(maxElevation-minElevation))));
        return dpHeight-(float)(dpHeight*(elevation-minElevation)/(maxElevation-minElevation));
    }
    
    public void updateProfile(LatLng userLocation) {
    	
    }

}
