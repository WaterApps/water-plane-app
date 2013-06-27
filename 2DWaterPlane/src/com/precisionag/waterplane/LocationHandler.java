package com.precisionag.waterplane;

import java.text.DecimalFormat;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.precisionag.lib.CustomMarker;

public class LocationHandler implements LocationListener {
	    public void onLocationChanged(Location location) {
	      // Called when a new location is found by the network location provider.
	    	if (!MainActivity.drag_mode) {

	    	  MainActivity.userLocation = new LatLng(location.getLatitude(), location.getLongitude());
		      
			  double elevationDouble = MainActivity.field.elevationFromLatLng(MainActivity.userLocation);
			  double elevationDelta =  elevationDouble - MainActivity.waterLevelMeters;
			  String ElevationText;
			  
			  if (elevationDouble == 0.0) {
				  ElevationText = "You are not in the field.";
			  }
			  else {
			  	  String elevationString = new DecimalFormat("#.#").format(Math.abs(elevationDouble));
			  	  String elevationDeltaString = new DecimalFormat("#.#").format(Math.abs(elevationDelta));
			  	  if (elevationDelta >= 0.0) {
			  		  ElevationText = "You: " + elevationDeltaString + "m above water (" + elevationString + "m)";
			  	  }
			  	  else {
			  		ElevationText = "You: " + elevationDeltaString + "m below water (" + elevationString + "m)";
			  	  }
			  }
			  MainActivity.ElevationTextView.setText(ElevationText);
			  
			  CustomMarker.setUserElevation(elevationDouble);
			  MainActivity.userMarker.setPosition(MainActivity.userLocation);
	    	}
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}

}
