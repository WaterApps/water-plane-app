package com.waterapps.lib;

import android.graphics.Bitmap;
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
import java.util.Collections;
import java.util.List;

public class Field {
	//bitmap represents rasterized elevation data
	private Bitmap elevationBitmap;
    Polyline polyline;

	//defines the edges of the field
	private LatLngBounds fieldBounds;
	LatLng sw;
	LatLng ne;
	
	//minimum elevation corresponds to black in the bitmap, in meters above sea level
	private double minElevation;
	
	//maximum elevation, corresponds to white in the bitmap, in meters above sea level
	double maxElevation;
	
	static MapFragment mapFragment;
	public static SeekBar seekBar;
	public static GroundOverlay prevoverlay;
	
	//constructor method
	public Field(Bitmap bitmap, LatLng southwest, LatLng northeast, double minHeight, double maxHeight) {
		setElevationBitmap(bitmap);
		sw = southwest;
		ne = northeast;
		setFieldBounds(new LatLngBounds(sw, ne));
		setMinElevation(minHeight);
		maxElevation = maxHeight;
		prevoverlay = createOverlay(bitmap, getFieldBounds());

        //mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(sw, ne), 50));
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
	
	public void setMinElevation(double elevation) {
		minElevation = elevation;
	}
	
	public void setMaxElevation(double elevation) {
		maxElevation = elevation;
	}
	
	public void setNortheast(LatLng northeast) {
		ne = northeast;
	}
	
	public void setSouthwest(LatLng southwest) {
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
        MainActivity.updateColors(this);
    }
}
