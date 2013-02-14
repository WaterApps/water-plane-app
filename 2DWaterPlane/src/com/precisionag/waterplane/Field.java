package com.precisionag.waterplane;

import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;

public class Field {
	//bitmap represents rasterized elevation data
	Bitmap elevationBitmap;
	
	//defines the edges of the field
	LatLngBounds fieldBounds;
	
	//minimum elevation corresponds to black in the bitmap, in meters above sea level
	double minElevation;
	
	//maximum elevation, corresponds to white in the bitmap, in meters above sea level
	double maxElevation;
	
	//constructor method
	public Field(Bitmap bitmap, LatLngBounds bounds, double minHeight, double maxHeight) {
		elevationBitmap = bitmap;
		fieldBounds = bounds;
		minElevation = minHeight;
		maxElevation = maxHeight;
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
	
	//creates an overlay view of the field on the specified map object
	public GroundOverlay createOverlay(GoogleMap map) {
		GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
	     .image(BitmapDescriptorFactory.fromBitmap(elevationBitmap))
	     .positionFromBounds(fieldBounds)
	     .transparency(0));
		groundOverlay.setVisible(true);
		return groundOverlay;
	}
	
	
}
