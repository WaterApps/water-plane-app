package com.precisionag.waterplane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity implements OnMapClickListener, OnCameraChangeListener, OnMarkerDragListener, OnTouchListener {
private static final int ADD_MODE = 1;
private static final int DRAG_MODE = 2;

GroundOverlay prevoverlay;
Field field;
List<CustomMarker> markers;
LatLng userLocation;
int mode;
double waterLevelMeters;
double density = 0.0;
LocationManager locationManager;
Context context = this;
Marker userMarker;

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
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.custom_ab);

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.field);
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		GoogleMap map = mapFragment.getMap();
		map.setOnCameraChangeListener(this);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);
		map.setOnMapClickListener(this);
		UiSettings uiSettings = map.getUiSettings();
		
		userMarker = map.addMarker(new MarkerOptions()
        .position(new LatLng(0, 0))
        .title("You are here"));
		
		map.setOnMarkerDragListener(this);

		
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setTiltGesturesEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		
		field = new Field(bitmap, new LatLng(0.0, 0.0), new LatLng(0.0, 0.0), 0.0, 0.0);
		userLocation = new LatLng(0.0, 0.0);
		markers = new ArrayList<CustomMarker>();
		mode = 0;
		density = (getResources().getDisplayMetrics().xdpi)/160.0;
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		
		CustomMarker.setDisplayWidth(width);
		CustomMarker.setField(field);
		CustomMarker.setActivity(this);
		CustomMarker.setMap(map);
		CustomMarker.setContext(context);
		CustomMarker.setLayout((RelativeLayout)findViewById(R.id.TopLevelView));
		RelativeLayout lay = (RelativeLayout) findViewById(R.id.TopLevelView);
		lay.setOnTouchListener(this);
				
		readDataFile(field);
		prevoverlay = field.createOverlay(map);
		configSeekbar(field, prevoverlay);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		//set button listeners
		final Button buttonPlus = (Button) findViewById(R.id.buttonPlus);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Increase elevation
            	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            	seekBar.setProgress(seekBar.getProgress()+2);
            	updateColors(field);
            }
        });
        
        final Button buttonMinus = (Button) findViewById(R.id.buttonMinus);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// Decrease elevation
            	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            	seekBar.setProgress(seekBar.getProgress()-2);
            	updateColors(field);
            }
        });
        
        final Button buttonDelete = (Button) findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Iterator<CustomMarker> i = markers.iterator();
            	CustomMarker marker;

            	while (i.hasNext()) {
            		 marker = i.next();
            		 if (CustomMarker.selected == marker.button) {
            			 marker.removeMarker();
            			 markers.remove(marker);
            			 break;
            		 }
            	}
            	
                CustomMarker.layout.removeView(CustomMarker.selected);
            }
        });
		
		updateColors(field);
		
		
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			if (mode != DRAG_MODE) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						context);
		 
					// set title
					alertDialogBuilder.setTitle("GPS is not enabled");
		 
					// set dialog message
					alertDialogBuilder
						.setMessage("Please enable GPS!")
						.setCancelable(false)
						.setPositiveButton("Exit",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								// if this button is clicked, close
								// current activity
								MainActivity.this.finish();
							}
						  })
						.setNegativeButton("GPS Settings",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						});
		 
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();
		 
						// show it
						alertDialog.show();
			}
		}
		
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
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
	    else if (item.getItemId() == R.id.menu_drag) {
	    	if (mode == DRAG_MODE) {
	    		mode = 0;
	    		userLocation = new LatLng(
	    				locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(), 
	    				locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
	    		
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
	  		  userMarker.setPosition(userLocation);
	    	} 
	    	else {
	    		mode = DRAG_MODE;
	    		
	    	}
	    	userMarker.setDraggable(mode == DRAG_MODE);
            return true;
	    }
	    else {
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onMapClick (LatLng point) {
		CustomMarker.selected = null;
		updateMarkers();
		switch(mode) {
		case ADD_MODE:
			CustomMarker.setWaterElevation(waterLevelMeters);
			CustomMarker newMarker = new CustomMarker(point);
			newMarker.updateMarker(density);
			markers.add(newMarker);
			mode = 0;
			break;
		case DRAG_MODE:/*
			userLocation = point;
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
			  */
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
			if (userLocation != null ) {
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
				  updateColors(field);
			}
			
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
		 marker.updateMarker(density);
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
    	if (mode != DRAG_MODE) {

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
		  userMarker.setPosition(userLocation);
    	}
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}
  };

@Override
public void onCameraChange(CameraPosition position) {
	updateMarkers();
}

@Override
public void onMarkerDrag(Marker marker) {
	// TODO Auto-generated method stub
	userLocation = marker.getPosition();
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
	  updateMarkers();
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
public boolean onTouch(View arg0, MotionEvent arg1) {
	// TODO Auto-generated method stub
	updateMarkers();
	return true;
}

}

