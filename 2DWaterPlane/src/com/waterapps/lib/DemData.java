package com.waterapps.lib;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.widget.SeekBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.waterapps.waterplane.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Steve on 7/21/13.
 */
public class DemData {
    private int ncols;
    private int nrows;
    float [][]elevationData;
    private float minElevation;
    private float maxElevation;
    private LatLng sw;
    private LatLng ne;

    //bitmap represents rasterized elevation data
    private Bitmap elevationBitmap;
    Polyline polyline;

    //defines the edges of the field
    private LatLngBounds fieldBounds;

    static MapFragment mapFragment;
    public static SeekBar seekBar;
    public static GroundOverlay prevoverlay;

    public DemData(Bitmap bitmap, LatLng southwest, LatLng northeast, double minHeight, double maxHeight) {
        setElevationBitmap(bitmap);
        sw = southwest;
        ne = northeast;
        setFieldBounds(new LatLngBounds(sw, ne));
        setMinElevation((float)minHeight);
        setMaxElevation((float)maxHeight);
        prevoverlay = createOverlay(bitmap, getFieldBounds());
    }

    public DemData(int w, int h, float [][]data) {
        setMinElevation(Float.POSITIVE_INFINITY);
        setMaxElevation(Float.NEGATIVE_INFINITY);
        setNcols(w);
        setNrows(h);
        setElevationData(data);
    }

    public DemData(int w, int h) {
        setMinElevation(Float.POSITIVE_INFINITY);
        setMaxElevation(Float.NEGATIVE_INFINITY);
        setNcols(w);
        setNrows(h);
        setElevationData(new float[getNcols()][getNrows()]);
    }

    public DemData() {
        setMinElevation(Float.POSITIVE_INFINITY);
        setMaxElevation(Float.NEGATIVE_INFINITY);
        setNcols(1);
        setNrows(1);
        setElevationData(new float[getNcols()][getNrows()]);
    }

    public void setDimensions(int w, int h) {
        setNcols(w);
        setNrows(h);
        setElevationData(new float[getNcols()][getNrows()]);
    }

    public float[][] getData() {
        return getElevationData();
    }

    public LatLngBounds getBounds() {
        return new LatLngBounds(getLowerLeft(), getUpperRight());
    }

    public LatLng getCenter() {
        LatLngBounds bounds = getBounds();
        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        double newLat = (sw.latitude + ne.latitude)/2;
        double newLong = (sw.longitude + ne.longitude)/2;
        return new LatLng(newLat, newLong);
    }

    public Bitmap getBitmap() {
        Log.i("bitmap", "bitmap being created");
        Bitmap bitmap;
        int intpixels[] = new int[getNrows()*getNcols()];

        Log.i("min value", Double.toString(getMinElevation()));
        Log.i("max value", Double.toString(getMaxElevation()));
        Log.i("range value", Double.toString(getMaxElevation()-getMinElevation()));
        for(int k = 0; k<getNrows(); k++) {
            for(int m=0; m<getNcols(); m++) {
                //normalize each float to a value from 0-255

                double range = 255/(getMaxElevation()-getMinElevation());
                intpixels[k+(m*getNrows())] = (int)(range*(getElevationData()[k][m]-getMinElevation()));

                //Log.i("pixel value", Integer.toString(intpixels[m+(k*getNcols())]));
                //intpixels[k] = (int)( ((pixels[k]-min)/(max-min))*(double)255.0);
                //convert to greyscale ARGB value
                //intpixels[m+(k*getNcols())] = 0xFF000000 + intpixels[m+(k*getNcols())] + intpixels[m+(k*getNcols())]<<8 + intpixels[m+(k*getNcols())]<<16;
            }
        }

        for(int l=0;l<getNcols()*getNrows(); l++) {
            //intpixels[l] = 0xFF000000 + intpixels[l] + intpixels[l]<<8 + intpixels[l]<<16;
            intpixels[l] = Color.argb(255, intpixels[l], intpixels[l], intpixels[l]);
        }
        bitmap = Bitmap.createBitmap(intpixels, 0, getNrows(), getNrows(), getNcols(), Bitmap.Config.ARGB_8888);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);

        return bitmap;
    }


    public float [][] getElevationData() {
        return elevationData;
    }

    public void setElevationData(float [][] elevationData) {
        this.elevationData = elevationData;
    }

    public void setMaxElevation(float maxElevation) {
        this.maxElevation = maxElevation;
    }

    public void setMinElevation(float minElevation) {
        this.minElevation = minElevation;
    }

    public int getNcols() {
        return ncols;
    }

    public void setNcols(int ncols) {
        this.ncols = ncols;
    }

    public int getNrows() {
        return nrows;
    }

    public void setNrows(int nrows) {
        this.nrows = nrows;
    }

    public LatLng getLowerLeft() {
        return sw;
    }

    public void setLowerLeft(LatLng lowerLeft) {
        this.sw = lowerLeft;
    }

    public LatLng getUpperRight() {
        return ne;
    }

    public void setUpperRight(LatLng upperRight) {
        this.ne = upperRight;
    }

    public void calculateTenths() {
        int size = nrows*ncols;
        float []tempArray = new float[size];
        for(int i=0; i<nrows; i++) {
            for(int j=0; j<ncols; j++) {
                tempArray[i+(nrows*j)] = elevationData[i][j];
            }
        }
        Arrays.sort(tempArray);
        float min = tempArray[(size/100)*3];
        float max = tempArray[(size/100)*97];
        MainActivity.sliderMin = min;
        MainActivity.sliderMax = max;
        MainActivity.updateEditText(min, max);
    }

    //access methods
    public static void setMapFragment(MapFragment map) {
        mapFragment = map;
    }

    public static void setSeekBar(SeekBar bar) {
        seekBar = bar;
    }

    public void setBitmap(Bitmap bits) {
        setElevationBitmap(bits);
    }

    public void setBounds(LatLngBounds bounds) {
        setFieldBounds(bounds);
        ne = bounds.northeast;
        sw = bounds.southwest;
    }

    public void setNorthEast(LatLng northeast) {
        ne = northeast;
    }

    public void setSouthWest(LatLng southwest) {
        sw = southwest;
    }

    //creates an overlay view of the field on the specified map object
    public GroundOverlay createOverlay(GoogleMap map) {
        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(sw.latitude, ne.longitude))
                .add(sw)
                .add(new LatLng(ne.latitude, sw.longitude))
                .add(ne)
                .add(new LatLng(sw.latitude, ne.longitude)); // Closes the polyline.
        polyline = mapFragment.getMap().addPolyline(rectOptions);

        GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(getElevationBitmap()))
                .positionFromBounds(getFieldBounds())
                .transparency(0));
        groundOverlay.setVisible(true);
        return groundOverlay;
    }

    public Polyline updatePolyLine() {
        polyline.remove();
        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(sw.latitude, ne.longitude))
                .add(sw)
                .add(new LatLng(ne.latitude, sw.longitude))
                .add(ne)
                .add(new LatLng(sw.latitude, ne.longitude))
                .zIndex(1.0f); // Closes the polyline.
        return polyline = mapFragment.getMap().addPolyline(rectOptions);
    }
    //returns elevation of given point
    public double elevationFromLatLng(LatLng point) {
        /*
		System.out.println(getFieldBounds());
		System.out.println(point);
		*/

        if (getFieldBounds().contains(point)) {
            //use linear interpolation to figure out which pixel to get data from
            //should be accurate since fields <= ~1 mile wide
            double north = ne.latitude;
            double east = ne.longitude;
            double south = sw.latitude;
            double west = sw.longitude;

            int width = getElevationBitmap().getWidth();
            int height = getElevationBitmap().getHeight();

            double x = (double) width*(point.longitude-west)/(east-west);
            double y = (double) height*(north - point.latitude)/(north-south);

            //retrieve packed int
            int waterLevel = getElevationBitmap().getPixel((int)x, (int)y);

            //pixels are represented as packed ARGB, so discard all but blue channel
            //this gives range of 0-255
            waterLevel &= 0x000000FF;

            //convert 0-255 pixel data to elevation float
            double waterLevelMeters = getMinElevation() + ((double)waterLevel*(maxElevation-getMinElevation())/255.0);
            return waterLevelMeters;

        }
        else {
            //point isn't in the field
            return 0.0;
        }
    }

    private GroundOverlay createOverlay(Bitmap overlayBitmap, LatLngBounds bounds) {
        BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(overlayBitmap);
        GoogleMap map = mapFragment.getMap();
        GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
                .image(image)
                .positionFromBounds(bounds)
                .transparency(0));
        groundOverlay.setVisible(true);
        return groundOverlay;
    }

    public double getMinElevation() {
        return minElevation;
    }

    public double getMaxElevation() {
        return maxElevation;
    }

    public Bitmap getElevationBitmap() {
        return elevationBitmap;
    }

    public void setElevationBitmap(Bitmap elevationBitmap) {
        this.elevationBitmap = elevationBitmap;
    }

    public LatLngBounds getFieldBounds() {
        return fieldBounds;
    }

    public void setFieldBounds(LatLngBounds fieldBounds) {
        this.fieldBounds = fieldBounds;
    }

    public float[] getMinMaxLine(LatLng p1, LatLng p2) {

        List<Integer> values = new ArrayList<Integer>();
        int x1 = 1, x2 = 1, y1 = 1, y2 = 1;
        if (getFieldBounds().contains(p1) && getFieldBounds().contains(p2)) {
            //use linear interpolation to figure out which pixel to get data from
            //should be accurate since fields <= ~1 mile wide
            double north = ne.longitude;
            double east = ne.latitude;
            double south = sw.longitude;
            double west = sw.latitude;

            int width = getElevationBitmap().getWidth();
            int height = getElevationBitmap().getHeight();

            x1 = (int)((double)width*(p1.latitude-west)/(east-west));
            y1 = (int)((double)height*(p1.longitude-south)/(north-south));
            x2 = (int)((double)width*(p2.latitude-west)/(east-west));
            y2 = (int)((double)height*(p2.longitude-south)/(north-south));
            System.out.println(x1);
            System.out.println(x2);
            System.out.println(y1);
            System.out.println(y2);
            System.out.println(width);
            System.out.print(height);
        }
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        int err = dx - dy;

        while (true) {
            values.add(elevationBitmap.getPixel(x1, y1));

            if (x1 == x2 && y1 == y2) {
                break;
            }

            int e2 = 2 * err;

            if (e2 > -dy) {
                err = err - dy;
                x1 = x1 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y1 = y1 + sy;
            }
        }
        Collections.sort(values);
        Log.i("thing", "text");

        double min = getMinElevation() + ((double)values.get(0)*(maxElevation-getMinElevation())/255.0);
        double max = getMinElevation() + ((double)values.get(values.size()-1)*(maxElevation-getMinElevation())/255.0);

        if (elevationFromLatLng(p1) < elevationFromLatLng(p2)) {
            min = elevationFromLatLng(p1);
            max = elevationFromLatLng(p2);
        }
        else {
            max = elevationFromLatLng(p2);
            min = elevationFromLatLng(p1);
        }
        float[] returnValue = {(float)min, (float)max};
        return returnValue;
    }

    public void setWaterLevel(double level) {
        seekBar.setProgress((int)(255.0*(level-MainActivity.sliderMin)/(MainActivity.sliderMax-MainActivity.sliderMin)));
        //updateColors();
        //MainActivity.updateColors(this);
    }

}
