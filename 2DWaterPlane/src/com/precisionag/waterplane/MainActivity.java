package com.precisionag.waterplane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MainActivity extends Activity implements OnMapClickListener {
private static final int ADD_MODE = 1;
private static final int REMOVE_MODE = 2;

GroundOverlay prevoverlay;

Field field;
List<CustomMarker> markers;
LatLng userLocation;
int mode;
double waterLevelMeters;

	public class LegalNoticeDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext()))
	               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.field);
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		GoogleMap map = mapFragment.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);
		map.setOnMapClickListener(this);
		UiSettings uiSettings = map.getUiSettings();
		
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setTiltGesturesEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		
		field = new Field(bitmap, new LatLng(0.0, 0.0), new LatLng(0.0, 0.0), 0.0, 0.0);
		markers = new ArrayList<CustomMarker>();
		mode = 0;
		
		CustomMarker.setField(field);
		CustomMarker.setActivity(this);
		CustomMarker.setMap(map);
		
		readDataFile(field);
		prevoverlay = field.createOverlay(map);
		configSeekbar(field, prevoverlay);
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		updateColors(field);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    if (item.getItemId() == R.id.item_legal) {
	        	//this part randomly stopped working
	        	//and needed rewritten anyway
	    	
	            //DialogFragment newFragment = new LegalNoticeDialogFragment();
	        	//newFragment.show(getFragmentManager(), "legal");
	            return true;
	    }
	    else if (item.getItemId() == R.id.menu_add) {
        	mode = ADD_MODE;
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_remove) {
        	mode = REMOVE_MODE;
            return true;
	    }
	    else {
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onMapClick (LatLng point) {
		switch(mode) {
		case ADD_MODE:
			CustomMarker.setWaterElevation(waterLevelMeters);
			markers.add(new CustomMarker(point));
			mode = 0;
			break;
		case REMOVE_MODE:
			Iterator<CustomMarker> i = markers.iterator();
			CustomMarker marker;

			while (i.hasNext()) {
				 marker = i.next();
				 if (marker.inBounds(point)) {
					 marker.removeMarker();
					 i.remove();
				 }
			}
			break;
		default:
			break;
		}
		
	}
	
//takes a bitmap, latitude/longitude bounds, and a map to create a map overlay
//this has been duplicated in the Field class
private GroundOverlay createOverlay(Bitmap overlayBitmap, LatLngBounds bounds) {
	MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
	BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(overlayBitmap);
	GoogleMap map = mapFragment.getMap();
	GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
     .image(image)
     .positionFromBounds(bounds)
     .transparency(0));
	groundOverlay.setVisible(true);
	return groundOverlay;
}
	
//public void updateColors(Bitmap bitmap, LatLngBounds bounds) {
public void updateColors(Field field) {
	//get level from seekbar
	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	seekBar.setMax(255);
	int waterLevel = seekBar.getProgress();
	
	int width = field.elevationBitmap.getWidth();
	int height = field.elevationBitmap.getHeight();
	int[] pixels = new int[width * height];
	field.elevationBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
	Bitmap bitmap = field.elevationBitmap.copy(field.elevationBitmap.getConfig(), true);
	
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
	prevoverlay.remove();

	prevoverlay = createOverlay(bitmap, field.fieldBounds);

}

private void configSeekbar(final Field field, final GroundOverlay overlay) {
	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	seekBar.setMax(255);
	seekBar.setProgress(seekBar.getMax()/2);
	seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
		@Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			//get level from seekbar
			int waterLevel = seekBar.getProgress();
			
			//update text block
			waterLevelMeters = field.minElevation + ((double)waterLevel*(field.maxElevation-field.minElevation)/255.0);
			TextView waterElevationTextView = (TextView) findViewById(R.id.text);
			String elevation = new DecimalFormat("#.#").format(waterLevelMeters);
			String waterElevationText = "Elevation: " + elevation + "m";
			waterElevationTextView.setText(waterElevationText);
			
			//update other text block
			double elevationDouble = field.elevationFromLatLng(userLocation);
			  double elevationDelta =  elevationDouble - waterLevelMeters;
			  String ElevationText;
			  TextView ElevationTextView = (TextView) findViewById(R.id.text2);
			  
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
			  ElevationTextView.setText(ElevationText);
			  
			  //update marker text
			  CustomMarker.setWaterElevation(waterLevelMeters);
			  
			  //these are too slow to do realtime
			  
			  //updateMarkers();
			 //updateColors(field);
		}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        	updateColors(field);
        	updateMarkers();
        }
	});
}

private void updateMarkers() {
	Iterator<CustomMarker> i = markers.iterator();
	CustomMarker marker;

	while (i.hasNext()) {
		 marker = i.next();
		 marker.updateMarker();
	}
}

private void readDataFile(Field field) {
	try {
		
		//read data from string
		AssetManager am = getApplicationContext().getAssets();
		BufferedReader dataIO = new BufferedReader(new InputStreamReader(am.open("field.latlng")));
	    String dataString = null;

	    dataString = dataIO.readLine();
	    double north = Double.parseDouble(dataString);
	    dataString = dataIO.readLine();
	    double east = Double.parseDouble(dataString);
	    dataString = dataIO.readLine();
	    double south = Double.parseDouble(dataString);
	    dataString = dataIO.readLine();
	    double west = Double.parseDouble(dataString);
	    
	    LatLng northEast = new LatLng(north, east);
	    LatLng southWest = new LatLng(south, west);
	    
	    dataString = dataIO.readLine();
	    double minElevation = Double.parseDouble(dataString);
	    dataString = dataIO.readLine();
	    double maxElevation = Double.parseDouble(dataString);
	    
	    //set corresponding parameters in field
	    field.setBounds(new LatLngBounds(northEast, southWest));
	    field.setNortheast(northEast);
	    field.setSouthwest(southWest);
	    field.setMinElevation(minElevation);
	    field.setMaxElevation(maxElevation);
	    
	    dataIO.close();
	    
	
	}
	catch  (IOException e) {
	}


}

LocationListener locationListener = new LocationListener() {
    public void onLocationChanged(Location location) {
      // Called when a new location is found by the network location provider.
      userLocation = new LatLng(location.getLatitude(), location.getLongitude());
      
	  double elevationDouble = field.elevationFromLatLng(userLocation);
	  double elevationDelta =  elevationDouble - waterLevelMeters;
	  String ElevationText;
	  TextView ElevationTextView = (TextView) findViewById(R.id.text2);
	  
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
	  ElevationTextView.setText(ElevationText);
	  
	  CustomMarker.setUserElevation(elevationDouble);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}
  };

}

