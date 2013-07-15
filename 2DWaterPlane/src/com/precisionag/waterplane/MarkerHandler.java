package com.precisionag.waterplane;

import java.text.DecimalFormat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.precisionag.lib.CustomMarker;

public class MarkerHandler implements OnMarkerDragListener, GoogleMap.OnMarkerClickListener {
	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub
		MainActivity.userLocation = marker.getPosition();
        if (MainActivity.following) {
            MainActivity.field.setWaterLevel(MainActivity.field.elevationFromLatLng(MainActivity.userLocation));
            MainActivity.field.updateColors();
        }
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTitle().equals("true") | marker.getTitle().equals("false")) {
            CustomMarker.setSelected(marker);
            MainActivity.showMarkerAB();
        }
        if (marker.getTitle().equals("true")) {
            if (marker.equals(CustomMarker.getSelected())) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrow_selected));
            }
            else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrow));
            }
            marker.setTitle("false");
        }
        else if (marker.getTitle().equals("false")) {
            marker.setTitle("true");
        }
        MainActivity.updateMarkers();
        return true;
    }
}
