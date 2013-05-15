package com.precisionag.waterplane;

import java.text.DecimalFormat;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.Marker;
import com.precisionag.lib.CustomMarker;

public class MarkerHandler implements OnMarkerDragListener {
	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub
		MainActivity.userLocation = marker.getPosition();
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
		  		  ElevationText = "Your Elevation: " + elevationDeltaString + "m above water (" + elevationString + "m)";
		  	  }
		  	  else {
		  		ElevationText = "Your Elevation: " + elevationDeltaString + "m below water (" + elevationString + "m)";
		  	  }
		  }
		  MainActivity.ElevationTextView.setText(ElevationText);
		  
		  CustomMarker.setUserElevation(elevationDouble);
		  MainActivity.updateMarkers();
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub
		
	}
}
