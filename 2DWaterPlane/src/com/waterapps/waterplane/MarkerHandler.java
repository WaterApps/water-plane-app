package com.waterapps.waterplane;

import java.text.DecimalFormat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.Marker;
import com.waterapps.lib.CustomMarker;

public class MarkerHandler implements OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    public static void setText() {
        double elevationDouble = MainActivity.demData.elevationFromLatLng(MainActivity.userLocation);
        double elevationDelta =  elevationDouble - MainActivity.waterLevelMeters;
        String ElevationText;

        if (elevationDouble == 0.0) {
            ElevationText = "You are not in the demData.";
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
        //MainActivity.updateMarkers();
    }

	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub
        if(!marker.getTitle().equals("true") && !marker.getTitle().equals("false")) {
            MainActivity.userLocation = marker.getPosition();
            MainActivity.updateMarkers();

        }

        if (MainActivity.following) {
            MainActivity.demData.setWaterLevel(MainActivity.demData.elevationFromLatLng(MainActivity.userLocation));
        }
		double elevationDouble = MainActivity.demData.elevationFromLatLng(MainActivity.userLocation);
		  double elevationDelta =  elevationDouble - MainActivity.waterLevelMeters;
		  String ElevationText;
		  
		  if (elevationDouble == 0.0) {
			  ElevationText = "You are not in the demData.";
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
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		MainActivity.updateMarkers();
		
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTitle().equals("true") | marker.getTitle().equals("false")) {
            CustomMarker.setSelected(marker);
        }

        /*
        if (marker.getTitle().equals("true")) {
            MainActivity.showHiddenMarkerAB();
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
            MainActivity.showMarkerAB();
        }
        */

        if (marker.getTitle().equals("true")) {
            MainActivity.showMarkerAB();
        }
        if (marker.getTitle().equals("false")) {
            MainActivity.showHiddenMarkerAB();
        }

        MainActivity.updateMarkers();
        return true;
    }
}
