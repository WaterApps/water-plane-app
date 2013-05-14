package com.precisionag.waterplane;

import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class Field {
	//bitmap represents rasterized elevation data
	Bitmap elevationBitmap;
	
	//defines the edges of the field
	LatLngBounds fieldBounds;
	LatLng sw;
	LatLng ne;
	
	//minimum elevation corresponds to black in the bitmap, in meters above sea level
	double minElevation;
	
	//maximum elevation, corresponds to white in the bitmap, in meters above sea level
	double maxElevation;
	
	//constructor method
	public Field(Bitmap bitmap, LatLng southwest, LatLng northeast, double minHeight, double maxHeight) {
		elevationBitmap = bitmap;
		sw = southwest;
		ne = northeast;
		fieldBounds = new LatLngBounds(sw, ne);
		minElevation = minHeight;
		maxElevation = maxHeight;
	}
	
	public void setBitmap(Bitmap bits) {
		elevationBitmap = bits;
	}
	
	public void setBounds(LatLngBounds bounds) {
		fieldBounds = bounds;
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
		GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
	     .image(BitmapDescriptorFactory.fromBitmap(elevationBitmap))
	     .positionFromBounds(fieldBounds)
	     .transparency(0));
		groundOverlay.setVisible(true);
		return groundOverlay;
	}
	
	//returns elevation of given point
	public double elevationFromLatLng(LatLng point) {
		if (fieldBounds.contains(point)) {
			//use linear interpolation to figure out which pixel to get data from
			//should be accurate since fields <= ~1 mile wide
			double north = ne.longitude;
			double east = ne.latitude;
			double south = sw.longitude;
			double west = sw.latitude;
			
			int width = elevationBitmap.getWidth();
			int height = elevationBitmap.getHeight();
			
			double x = (double)width*(point.latitude-west)/(east-west);
			double y = (double)height*(point.longitude-south)/(north-south);
			
			//retrieve packed int
			int waterLevel = elevationBitmap.getPixel((int)x, (int)y);
			
			//pixels are represented as packed ARGB, so discard all but blue channel
			//this gives range of 0-255
			waterLevel &= 0x000000FF;
			
			//convert 0-255 pixel data to elevation float
			double waterLevelMeters = minElevation + ((double)waterLevel*(maxElevation-minElevation)/255.0);
			return waterLevelMeters;
			
		}
		else {
			//point isn't in the field
			return 0.0;
		}
	}
	
}
