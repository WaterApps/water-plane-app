package com.precisionag.waterplane;

import java.text.DecimalFormat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

public class CustomMarker {
	private static GoogleMap map;
	private static Field field;
	private static Activity activity;
	private static double userElevation;
	private static double waterElevation;
	private LatLng location;
	private Bitmap image;
	private GroundOverlay overlay;
	
	public CustomMarker(LatLng point) {
		location = point;
		double elevationDouble = field.elevationFromLatLng(point);
		String title;
		String userDelta;
		String waterDelta;
		
		if (elevationDouble == 0.0) {
			title = "Not in field!";
			userDelta = "";
			waterDelta = "";
		}
		else {
			String elevation = new DecimalFormat("#.#").format(elevationDouble);
			String temp = new DecimalFormat("#.#").format(Math.abs(userElevation-elevationDouble));
			title = "Elevation: " + elevation + "m";
			if (userElevation-elevationDouble < 0.0) {
				userDelta = temp+"m above you";
			} else {
				userDelta = temp+"m below you";
			}
			
			temp = new DecimalFormat("#.#").format(Math.abs(waterElevation-elevationDouble));
			if (waterElevation-elevationDouble < 0.0) {
				waterDelta = temp+"m above water";
			} else {
				waterDelta = temp+"m below water";
			}
		}
		Bitmap markerBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.box);			
		markerBitmap = markerBitmap.copy(markerBitmap.getConfig(), true);
		Canvas c = new Canvas(markerBitmap);
		Paint paint = new Paint();
		paint.setTextSize(75);
		c.drawText(title, (float)5, (float)80, paint);
		c.drawText(userDelta, (float)5, (float)160, paint);
		c.drawText(waterDelta, (float)5, (float)240, paint);
		BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(markerBitmap);
		GoogleMap map;
		map = ((MapFragment) activity.getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
	     .image(image)
	     .anchor((float)1.0, (float)1.0)
	     .position(point, (float)300.0)
	     .transparency(0)
	     .zIndex((float)1));
		groundOverlay.setVisible(true);
		overlay = groundOverlay;
	}
	
	public void updateMarker(double density) {
		//delete old overlay and recreate with updated text
		overlay.remove();
		
		double elevationDouble = field.elevationFromLatLng(location);
		String title;
		String userDelta;
		String waterDelta;
		
		if (elevationDouble == 0.0) {
			title = "Not in field!";
			userDelta = "";
			waterDelta = "";
		}
		else {
			String elevation = new DecimalFormat("#.#").format(elevationDouble);
			String temp = new DecimalFormat("#.#").format(elevationDouble-userElevation);
			title = "Elevation: " + elevation + "m";
			userDelta = temp+"m from you";
			
			temp = new DecimalFormat("#.#").format(elevationDouble-waterElevation);
			waterDelta = temp+"m from water";
		}
		Bitmap markerBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.box);			
		markerBitmap = markerBitmap.copy(markerBitmap.getConfig(), true);
		Canvas c = new Canvas(markerBitmap);
		Paint paint = new Paint();
		
		paint.setTextSize((int)(25.0*density));
		c.drawText(title, (float)5, (float)80, paint);
		c.drawText(userDelta, (float)5, (float)160, paint);
		c.drawText(waterDelta, (float)5, (float)240, paint);
		BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(markerBitmap);
		GoogleMap map;
		map = ((MapFragment) activity.getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
	     .image(image)
	     .anchor((float)1.0, (float)1.0)
	     .position(location, (float)300.0)
	     .transparency(0)
	     .zIndex((float)1));
		groundOverlay.setVisible(true);
		overlay = groundOverlay;
	}
	
	public void removeMarker() {
		overlay.remove();
	}
	
	public static void setMap(GoogleMap newMap) {
		map = newMap;
	}
	
	public static void setField(Field newField) {
		field = newField;
	}
	
	public static void setUserElevation(double elevation) {
		userElevation = elevation;
	}
	
	public static void setWaterElevation(double elevation) {
		waterElevation = elevation;
	}
	
	public static void setActivity(Activity mActivity) {
		activity = mActivity;
	}
	
	public boolean inBounds(LatLng point) {
		return overlay.getBounds().contains(point);
	}
}
