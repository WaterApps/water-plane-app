package com.waterapps.waterplane;

import java.text.DecimalFormat;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.waterapps.lib.CustomMarker;

/**
 * An implementation of LocationListener.
 */
public class LocationHandler implements LocationListener {
	    public void onLocationChanged(Location location) {
	      // Called when a new location is found by the network location provider.

            //only use GPS data if drag mode is disabled
            if (!MainActivity.drag_mode) {
                //if 'follow user elevation' is set, set water level to match user location
              if (MainActivity.following) {
                  MainActivity.demData.setWaterLevel(MainActivity.demData.elevationFromLatLng(MainActivity.userLocation));
                  MainActivity.updateColors(MainActivity.demData);
              }

                //update text
              MainActivity.userLocation = new LatLng(location.getLatitude(), location.getLongitude());

              double elevationDouble = MainActivity.demData.elevationFromLatLng(MainActivity.userLocation);
              double elevationDelta =  elevationDouble - MainActivity.waterLevelMeters;
              String ElevationText;

              if (elevationDouble == 0.0) {
                  ElevationText = "You are not in the DEM.";
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
                MainActivity.updateMarkers();
	    	}

	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}

}
