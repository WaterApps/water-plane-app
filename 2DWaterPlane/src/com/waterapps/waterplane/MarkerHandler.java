package com.waterapps.waterplane;

import android.view.View;

import java.text.DecimalFormat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.Marker;
import com.waterapps.lib.CustomMarker;
import com.waterapps.lib.MapLine;

/**
 * An implementation of the Google Maps marker callbacks.
 */
public class MarkerHandler implements OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    /**
     * Updates text display in ManinActivity
     */
    public static void setText() {
        //create string to use
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

        //update things in MainActivity
        MainActivity.ElevationTextView.setText(ElevationText);
        CustomMarker.setUserElevation(elevationDouble);
    }

    /**
     * When a marker is dragged, continually update text
     * @param marker The marker being dragged
     */
	@Override
	public void onMarkerDrag(Marker marker) {
        //this is for the user location maarker
        if(!marker.getTitle().equals("true") && !marker.getTitle().equals("false")) {
            MainActivity.userLocation = marker.getPosition();
            MainActivity.updateMarkers();
        }
        //for when 'location following' mode is set
        if (MainActivity.following) {
            MainActivity.demData.setWaterLevel(MainActivity.demData.elevationFromLatLng(MainActivity.userLocation));
        }

        //updates text
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

    /**
     * Update marker graphics when a drag is completed
     * @param marker
     */
	@Override
	public void onMarkerDragEnd(Marker marker) {
		MainActivity.updateMarkers();
	}

    /**
     * Do nothing when a marker drag begins
     * @param marker
     */
	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub
		
	}

    /**
     * When a marker is clicked, set it as selected
     * @param marker The marker being clicked
     * @return Whether or not click was handled
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker == null || marker.getTitle() == null) {
            return true;
        }

        if(marker.getTitle().equals("Tap")) {
            MainActivity.loadDEM(marker.getPosition());
        }

        //if this is a marker showing the min/max of a line, use default behavior
        //which is to toggle visibility of info window

        if(marker.getTitle().contains("Min") | marker.getTitle().contains("Max")) {
            return false;
        }

        //if this is a marker that identifies a line, set up appropriate UI
        if(marker.getTitle().equals("Line")) {
            MainActivity.buttonDeleteLine.setVisibility(View.VISIBLE);
            MainActivity.hideElevationControls();
            MapLine.setSelected(marker);
            MainActivity.buttonShowProfile.setVisibility(View.VISIBLE);
        }
        //if this isn't the user location marker, set it as selected.
        if(marker.getTitle().equals("true") | marker.getTitle().equals("false")) {
            CustomMarker.setSelected(marker);
        }

        //if the marker text is visible
        if (marker.getTitle().equals("true")) {
            MainActivity.showMarkerAB();
        }
        //if the marker text is hidden
        if (marker.getTitle().equals("false")) {
            MainActivity.showHiddenMarkerAB();
        }

        MainActivity.updateMarkers();
        return true;
    }
}
