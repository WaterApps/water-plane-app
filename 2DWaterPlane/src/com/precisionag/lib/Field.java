package com.precisionag.lib;

import android.graphics.Bitmap;
import android.widget.SeekBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class Field {
	//bitmap represents rasterized elevation data
	private Bitmap elevationBitmap;
	
	//defines the edges of the field
	private LatLngBounds fieldBounds;
	LatLng sw;
	LatLng ne;
	
	//minimum elevation corresponds to black in the bitmap, in meters above sea level
	private double minElevation;
	
	//maximum elevation, corresponds to white in the bitmap, in meters above sea level
	double maxElevation;
	
	static MapFragment mapFragment;
	static SeekBar seekBar;
	GroundOverlay prevoverlay;
	
	//constructor method
	public Field(Bitmap bitmap, LatLng southwest, LatLng northeast, double minHeight, double maxHeight) {
		setElevationBitmap(bitmap);
		sw = southwest;
		ne = northeast;
		setFieldBounds(new LatLngBounds(sw, ne));
		setMinElevation(minHeight);
		maxElevation = maxHeight;
		prevoverlay = createOverlay(bitmap, getFieldBounds());
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
		GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
	     .image(BitmapDescriptorFactory.fromBitmap(getElevationBitmap()))
	     .positionFromBounds(getFieldBounds())
	     .transparency(0));
		groundOverlay.setVisible(true);
		return groundOverlay;
	}
	
	//returns elevation of given point
	public double elevationFromLatLng(LatLng point) {
		System.out.println(getFieldBounds());
		System.out.println(point);
		
		if (getFieldBounds().contains(point)) {
			//use linear interpolation to figure out which pixel to get data from
			//should be accurate since fields <= ~1 mile wide
			double north = ne.longitude;
			double east = ne.latitude;
			double south = sw.longitude;
			double west = sw.latitude;
			
			int width = getElevationBitmap().getWidth();
			int height = getElevationBitmap().getHeight();
			
			double x = (double)width*(point.latitude-west)/(east-west);
			double y = (double)height*(point.longitude-south)/(north-south);
			
			System.out.println(east);
			System.out.println(west);
			System.out.println(north);
			System.out.println(south);
			System.out.println(width);
			System.out.println(height);
			System.out.println(x);
			System.out.println(y);
			
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
	
	public void updateColors() {
		prevoverlay.remove();
		
		//get level from seekbar
		seekBar.setMax(255);
		int waterLevel = seekBar.getProgress();
		
		int width = elevationBitmap.getWidth();
		int height = elevationBitmap.getHeight();
		int[] pixels = new int[width * height];
		elevationBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		Bitmap bitmap = elevationBitmap.copy(elevationBitmap.getConfig(), true);
		
		//test each pixel, if below water level set blue, else set transparent
		for (int i = 0; i < (width * height); i++) {
			if ((pixels[i] & 0x000000FF) < waterLevel) {
				//water is visible, set pixel to blue
				pixels[i] = 0xFF0000FF;
			} else {
				//no water, set pixel transparent
				pixels[i] = 0x00000000;
			}
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		
		//remove old map overlay and create new one
		

		prevoverlay = createOverlay(bitmap, getFieldBounds());
		
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
}
