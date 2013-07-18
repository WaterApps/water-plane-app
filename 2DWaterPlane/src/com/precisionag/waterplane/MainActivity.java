package com.precisionag.waterplane;

import com.ibm.util.CoordinateConversion;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.*;
import com.precisionag.lib.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.graphics.Color.HSVToColor;

public class MainActivity extends Activity implements OnMapClickListener, OnCameraChangeListener, OnTouchListener {
private static final int ADD_MODE = 1;
//static final int DRAG_MODE = 2;

GroundOverlay prevoverlay;
static Field field;
static List<CustomMarker> markers;
static LatLng userLocation;
static int mode;
public static double waterLevelMeters;
LocationManager locationManager;
static Marker userMarker;
private Uri fileUri;
static TextView ElevationTextView;
static GoogleMap map;
public static String demDirectory = "/dem";
static boolean drag_mode = false;
static LinearLayout elevationControls;
static LinearLayout markerBottomText;
static ActionBar actionBar;
static boolean currentlyDrawing;
public static boolean transparency;
public static boolean coloring;
public static boolean hasGPS;
public static Button buttonDelete;
LatLng[] linePoints;
public static float sliderMin;
public static float sliderMax;
public static float defaultSliderMin;
public static float defaultSliderMax;
static TextView editMin;
static TextView editMax;
public static int hsvColors[];
public static int hsvTransparentColors[];
static float alpha;
public static SharedPreferences prefs;
public static Context context;
static boolean following;
public static float scale;
public static Resources resources;
static ArrayList<Dem> dems;
Dem currentlyLoaded;
private boolean firstStart;
public static LatLngBounds demBounds;
public static boolean showMultipleDEM;
static SeekBar seekBar;
static MapFragment mapFrag;
private static final int FIRST_START = 42;
private static final int INITIAL_LOAD = 6502;
static TextView waterElevationTextView;
static boolean markerAB;
static TextView appName;
static Button showButton;
static Button hideButton;
static ArrayList<Polyline> demOutlines;
public static boolean mapReady;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        mapReady = false;
        demOutlines = new ArrayList<Polyline>();
        markerAB = false;
        resources = getResources();
        scale = getResources().getDisplayMetrics().density;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        demDirectory = prefs.getString("dem_dir", Environment.getExternalStorageDirectory().toString() + "/dem");
        context = this;
        alpha = 0.5f;
        hsvColors = new int[256];
        hsvTransparentColors = new int[256];
        float hsvComponents[] = {1.0f, 0.75f, 0.75f};
        for(int i = 0; i<255; i++) {
            hsvComponents[0] = 360.0f*i/255.0f;
            hsvColors[i] = HSVToColor(hsvComponents);
            hsvTransparentColors[i] = HSVToColor(128, hsvComponents);
        }
		super.onCreate(savedInstanceState);
        showMultipleDEM = false;
        sliderMin = 241.94f;
        sliderMax = 250.925f;
        defaultSliderMin = 241.94f;
        defaultSliderMax = 250.925f;
        currentlyDrawing = false;
		setContentView(R.layout.activity_main);
        editMin = (TextView) findViewById(R.id.editMin);
        editMax = (TextView) findViewById(R.id.editMax);


        elevationControls = (LinearLayout) findViewById(R.id.elevationControls);
        markerBottomText = (LinearLayout) findViewById(R.id.markerControls);
        actionBar = getActionBar();

        //set display mode
        transparency = prefs.getBoolean("transparency_bool", true);
        coloring = prefs.getBoolean("coloring", false);

        linePoints = new LatLng[2];
	    getActionBar().setCustomView(R.layout.custom_ab);
        actionBar.setDisplayShowCustomEnabled(true);
		MyMapFragment mapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map);
		map = mapFragment.getMap();
		map.setOnCameraChangeListener(this);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);
		map.setOnMapClickListener(this);
		UiSettings uiSettings = map.getUiSettings();
        waterElevationTextView = (TextView) findViewById(R.id.text);
		ElevationTextView = (TextView) findViewById(R.id.text2);
		
		userMarker = map.addMarker(new MarkerOptions()
        .position(map.getCameraPosition().target)
        .title("You are here"));
				
		MarkerHandler markerListener = new MarkerHandler();
		map.setOnMarkerDragListener(markerListener);
        map.setOnMarkerClickListener(markerListener);
		
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setTiltGesturesEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		
		mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		Field.setMapFragment(mapFrag);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(255);
        seekBar.setProgress(128);
		Field.setSeekBar(seekBar);
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.field);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        field = new Field(bitmap, new LatLng(0.0, 0.0), new LatLng(0.0, 0.0), 0.0, 0.0);
		userLocation = new LatLng(0.0, 0.0);
		markers = new ArrayList<CustomMarker>();
		mode = 0;
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		
		CustomMarker.setDisplayWidth(width);
		CustomMarker.setField(field);
		CustomMarker.setMap(map);
		CustomMarker.setContext(context);
		CustomMarker.setLayout((RelativeLayout)findViewById(R.id.TopLevelView));
		RelativeLayout lay = (RelativeLayout) findViewById(R.id.TopLevelView);
		lay.setOnTouchListener(this);
		
		field.prevoverlay = field.createOverlay(map);
		configSeekbar(field, prevoverlay);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //figure out if device has GPS
        List <String> providersList = locationManager.getAllProviders();
        Iterator<String> iterator = providersList.iterator();
        hasGPS = false;
        while (iterator.hasNext()) {
            if(iterator.next().contains("gps")) {
                hasGPS = true;
            }
        }

        //if it does, enable location updates
        if(hasGPS) {
		    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

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

        buttonDelete = (Button) findViewById(R.id.buttonDeleteMarker);

        appName = (TextView)findViewById(R.id.appName);

        hideButton = (Button)findViewById(R.id.buttonHideMarker);
        hideButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Iterator<CustomMarker> i = markers.iterator();
                CustomMarker marker;

                while (i.hasNext()) {
                    marker = i.next();
                    if (CustomMarker.getSelected().equals(marker.getMarker())) {
                        marker.getMarker().setTitle("false");
                        marker.updateMarker();
                        break;
                    }
                }

                showHiddenMarkerAB();
            }
        });

        showButton = (Button)findViewById(R.id.buttonShowMarker);
        showButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Iterator<CustomMarker> i = markers.iterator();
                CustomMarker marker;

                while (i.hasNext()) {
                    marker = i.next();
                    if (CustomMarker.getSelected().equals(marker.getMarker())) {
                        marker.getMarker().setTitle("true");
                        marker.updateMarker();
                        break;
                    }
                }

                showMarkerAB();
            }
        });

        hideButton.setVisibility(View.GONE);
        showButton.setVisibility(View.GONE);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Iterator<CustomMarker> i = markers.iterator();
            	CustomMarker marker;

            	while (i.hasNext()) {
            		 marker = i.next();
            		 if (CustomMarker.getSelected().equals(marker.getMarker())) {
            			 marker.removeMarker();
            			 break;
            		 }
            	}
                showNormalAB();
            }
        });
        buttonDelete.setVisibility(View.GONE);
        /*
        final Button buttonOpenDem = (Button) findViewById(R.id.buttonOpenDem);
        buttonOpenDem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//opens file manager
            	Intent intent = new Intent("org.openintents.action.PICK_FILE");
            	intent.setData(Uri.parse("file:///sdcard/dem"));
            	startActivityForResult(intent, 1);
            }
        });
		*/
        
		updateColors(field);

        if(hasGPS) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                if (!drag_mode) {
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

        scanDEMs();

        //show help on first app start
        firstStart = prefs.getBoolean("first_start", true);
        if (firstStart) {
            //show help
            Intent intent = new Intent(this, HelpActivity.class);
            startActivityForResult(intent, FIRST_START);

            //keep it from happening again
            SharedPreferences.Editor edit = prefs.edit();
            edit = prefs.edit();
            edit.putBoolean("first_start", false);
            edit.commit();
        }

        //load initial DEM if help menu isn't being shown
        if(!firstStart) {
            loadInitialDEM();
        }

        //ElevationRaster raster = new ElevationRaster();
        //new ReadElevationRasterTask(this, raster).execute(UritoURI(Uri.fromFile(new File("/sdcard/dem/geotiff.tif"))));

    }

	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}

	@Override
	public void onResume() {
		super.onResume();
        updateColors(field);
        if (hasGPS)
		    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
        if (markerAB) {
            getMenuInflater().inflate(R.menu.marker_menu, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            menu.findItem(R.id.menu_coloring).setChecked(coloring);
            menu.findItem(R.id.menu_transparency).setChecked(transparency);
            menu.findItem(R.id.menu_drag).setChecked(!drag_mode);
        }



		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection

        /*
	    if (item.getItemId() == R.id.item_legal) {
	        	  String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(
	              getApplicationContext());
	              AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(MainActivity.this);
	              LicenseDialog.setTitle("Legal Notices");
	              LicenseDialog.setMessage(LicenseInfo);
	              LicenseDialog.show();
	        	  return true;
	    }
	    */

	    if (item.getItemId() == R.id.menu_add) {
        	mode = ADD_MODE;
            Toast toast = Toast.makeText(this, "Tap on the map to place the marker.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return true;
	    }

	    else if (item.getItemId() == R.id.menu_drag) {
            if (drag_mode) {
                drag_mode = false;
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
	  		  		  ElevationText = "You: " + elevationDeltaString + "m above water (" + elevationString + "m)";
	  		  	  }
	  		  	  else {
	  		  		ElevationText = "You: " + elevationDeltaString + "m below water (" + elevationString + "m)";
	  		  	  }
	  		  }
	  		  ElevationTextView.setText(ElevationText);
	  		  
	  		  CustomMarker.setUserElevation(elevationDouble);
	  		  userMarker.setPosition(userLocation);
	    	}

	    	else {
	    		drag_mode = true;
	    		userLocation = map.getCameraPosition().target;
	    		userMarker.setPosition(userLocation);
                Toast toast = Toast.makeText(this, "Press and hold the location marker to drag.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 100);
                toast.show();
	    	}
	    	userMarker.setDraggable(drag_mode);
            item.setChecked(!drag_mode);
	    }

        else if (item.getItemId() == R.id.menu_center) {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(field.getFieldBounds(), 50));
        }

        else if(item.getItemId() == R.id.menu_coloring) {
            SharedPreferences.Editor edit = prefs.edit();
            if (coloring) {
                coloring = false;
            }
            else {
                coloring = true;
            }
            edit.putBoolean("coloring", coloring);
            edit.commit();
            item.setChecked(coloring);
            updateColors(field);
        }

        else if(item.getItemId() == R.id.menu_transparency) {
            SharedPreferences.Editor edit = prefs.edit();
            if (transparency) {
                transparency = false;
            }
            else {
                transparency = true;
            }
            edit.putBoolean("transparency_bool", transparency);
            edit.commit();
            item.setChecked(transparency);
            updateColors(field);
        }

        else if (item.getItemId() == R.id.menu_choose_dem) {
            //opens file manager

            Intent intent = new Intent("com.filebrowser.DataFileChooser");
            intent.putExtra("path", demDirectory);
            startActivityForResult(intent, 1);
        }

        else if(item.getItemId() == R.id.menu_follow) {
            following = !following;
            item.setChecked(following);
        }

        /*
        else if (item.getItemId() == R.id.menu_set_folder) {
            Bundle data = new Bundle();
            data.putString(Environment.getExternalStorageDirectory().getPath(), ""); //Starting path
            data.putString("returnIntent", "back"); //After choose return back to this
            Intent i = new Intent("com.filebrowser.DataPathChooser");
            i.putExtras(data);
            startActivity(i);
            prefs.registerOnSharedPreferenceChangeListener(this);
            return true;
        }
        */

        else if(item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            //prefs.registerOnSharedPreferenceChangeListener(this);
            return true;
        }

        else if(item.getItemId() == R.id.menu_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        }

	    else {
	            return super.onOptionsItemSelected(item);
	    }
        return true;
	}
	
	@Override
	public void onMapClick (LatLng point) {
        //handle markers
		CustomMarker.setSelected(null);
        showNormalAB();
		switch(mode) {
			case ADD_MODE:
				CustomMarker.setWaterElevation(waterLevelMeters);
				CustomMarker newMarker = new CustomMarker(point);
				newMarker.updateMarker();
				markers.add(newMarker);
				//updateMarkers();
				mode = 0;
                Toast toast = Toast.makeText(this, "Press and hold on the marker to move it.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
				break;
				
			default:
                hideMarkerBottomText();
                showElevationControls();
                showNormalAB();
				break;
		}

        //load DEM if clicked on
        Dem dem;
        for(int i = 0; i<dems.size(); i++) {
            dem = dems.get(i);
            if ( (currentlyLoaded == null) || !dem.getFilename().equals(currentlyLoaded.getFilename())) {
                if(dem.getBounds().contains(point)) {
                    currentlyLoaded = dem;
                    ElevationRaster raster = new ElevationRaster();
                    new ReadElevationRasterTask(this, raster, dem.getFilename()).execute(dem.getFileUri());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("last_dem", dem.getFileUri().getPath());
                    editor.commit();
                }
            }
        }
        updateMarkers();
    }
	
//takes a bitmap, latitude/longitude bounds, and a map to create a map overlay
private static GroundOverlay createOverlay(Bitmap overlayBitmap, LatLngBounds bounds) {
	//MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
	BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(overlayBitmap);
	GoogleMap map = mapFrag.getMap();
	GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
     .image(image)
     .positionFromBounds(bounds)
     .transparency(0));
	groundOverlay.setVisible(true);
	return groundOverlay;
}
	
public static void updateColors(Field field) {
    if (!currentlyDrawing) {
        currentlyDrawing = true;
        //get level from seekbar
        double distanceFromBottom = waterLevelMeters - field.getMinElevation();
        double fieldRange = field.getMaxElevation() - field.getMinElevation();

        double level = 255.0*distanceFromBottom/fieldRange;

        int waterLevel = (int)level;
        int width = field.getElevationBitmap().getWidth();
        int height = field.getElevationBitmap().getHeight();
        int[] pixels = new int[width * height];
        field.getElevationBitmap().getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bitmap = field.getElevationBitmap().copy(field.getElevationBitmap().getConfig(), true);
        int c;
        //test each pixel, if below water level set blue, else set transparent
        if (!coloring) {
            for (int i = 0; i < (width * height); i++) {
                if ((pixels[i] & 0x000000FF) < waterLevel) {
                    //water is visible, set pixel to blue
                    //pixels[i] = transparency ? 0x880000FF : 0xFF0000FF;
                    pixels[i] = 0xFF0000FF;
                } else {
                    //no water, set pixel transparent
                    pixels[i] = 0x00000000;
                }
            }
        }
        else {
            for (int i = 0; i < (width * height); i++) {
                if ((pixels[i] & 0x000000FF) < waterLevel) {
                    //water is visible, set pixel to color
                    c=pixels[i] & 0xFF;
                    pixels[i] =  hsvColors[c];
                } else {
                    //no water, set pixel transparent
                    pixels[i] = 0x00000000;
                }
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        //remove old map overlay and create new one
        GroundOverlay ppo = field.prevoverlay;
        field.prevoverlay = createOverlay(bitmap, field.getFieldBounds());
        if (transparency) {
            field.prevoverlay.setTransparency(alpha);
        }
        ppo.remove();
        currentlyDrawing = false;
    }
}

private void configSeekbar(final Field field, final GroundOverlay overlay) {
	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	seekBar.setMax(255);
    seekBar.setProgress(128);
	seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
		@Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (userLocation != null ) {
				//get level from seekbar
				int waterLevel = seekBar.getProgress();
				
				//update text block
				waterLevelMeters = sliderMin + ((double)waterLevel*(sliderMax-sliderMin)/255.0);
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
					  ElevationText = "No data for your location";
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
				  ElevationTextView.setText(ElevationText);
				  
				  //update marker text
				  CustomMarker.setWaterElevation(waterLevelMeters);
				  				  
				  //visual updates
				  updateMarkers();
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
	seekBar.setProgress(seekBar.getMax()/2);
}

public static void updateMarkers() {
	Iterator<CustomMarker> i = markers.iterator();
	CustomMarker marker;

    System.out.print("markers");
    System.out.print(markers);

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

LocationListener locationListener = new LocationHandler();

@Override
public void onCameraChange(CameraPosition position) {
	updateMarkers();
}

@Override
public boolean onTouch(View arg0, MotionEvent arg1) {
	updateMarkers();
	return false;
}

protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	//handle data from file manager

    if (requestCode == FIRST_START) {
        loadInitialDEM();
        return;
    }

    if (data != null) {
        if (data.getData().toString().contains(".tif")) {
            fileUri = data.getData();
            java.net.URI juri = null;
            try {
                juri = new java.net.URI(fileUri.getScheme(),
                        fileUri.getSchemeSpecificPart(),
                        fileUri.getFragment());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ElevationRaster raster = new ElevationRaster();
            String filename = fileUri.getPath().split("/")[fileUri.getPath().split("/").length-1];
            new ReadElevationRasterTask(this, raster, filename).execute(juri);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_dem", fileUri.getPath());
            editor.commit();
            return;
        }
        else {
            Toast toast = Toast.makeText(this, "File selected was not a GEOTiff file.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    if (requestCode == INITIAL_LOAD) {
        ElevationRaster raster = new ElevationRaster();
        Dem demToLoad = dems.get(0);
        String filename = demToLoad.getFilename();
        new ReadElevationRasterTask(this, raster, filename).execute(demToLoad.getFileUri());
    }
}

public static void onFileRead(ElevationRaster raster) {

	field.setBounds(raster.getBounds());
	System.out.println(raster.getBounds());
    raster.calculateTenths();
    field.setMinElevation(raster.getMinElevation());
    field.setMaxElevation(raster.getMaxElevation());
    field.setBitmap(raster.getBitmap());
    defaultSliderMin = sliderMin;
    defaultSliderMax = sliderMax;
    defaultSlider();
	map.animateCamera(CameraUpdateFactory.newLatLngBounds(raster.getBounds(), 50));
    field.updatePolyLine();
    updateColors(field);
    updateSlider();

    //if user is outside of field, turn off location tracking
    if(!raster.getBounds().contains(userLocation)) {
        drag_mode = true;
        userLocation = raster.getCenter();
        userMarker.setPosition(userLocation);
        userMarker.setDraggable(true);
        MarkerHandler.setText();
        Toast toast = Toast.makeText(context, "Press and hold the location marker to drag.", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
}

public static void hideElevationControls() {
    //LinearLayout elevationControls = (LinearLayout) findViewById(R.id.elevationControls);
    elevationControls.setVisibility(View.GONE);
}

public static void showElevationControls() {
    elevationControls.setVisibility(View.VISIBLE);
}

public static void hideMarkerBottomText() {
    markerBottomText.setVisibility(View.GONE);
}

public static void showMarkerBottomText() {
    markerBottomText.setVisibility(View.VISIBLE);
}

public static void showMarkerAB() {
    //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    //actionBar.setCustomView(R.layout.custom_ab);
    buttonDelete.setVisibility(View.VISIBLE);
    appName.setVisibility(View.GONE);
    hideButton.setVisibility(View.VISIBLE);
    showButton.setVisibility(View.GONE);
    //markerAB = true;
    //invalidateOptionsMenu();
}

public static void showHiddenMarkerAB() {
    //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    //actionBar.setCustomView(R.layout.custom_ab);
    buttonDelete.setVisibility(View.VISIBLE);
    appName.setVisibility(View.GONE);
    hideButton.setVisibility(View.GONE);
    showButton.setVisibility(View.VISIBLE);
    //markerAB = true;
    //invalidateOptionsMenu();
}

public static void showNormalAB() {
    //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    buttonDelete.setVisibility(View.GONE);
    appName.setVisibility(View.VISIBLE);
    hideButton.setVisibility(View.GONE);
    showButton.setVisibility(View.GONE);
    //markerAB = false;
    //invalidateOptionsMenu();
}

public static void updateEditText(float min, float max) {
    DecimalFormat df = new DecimalFormat("#.#");
    editMin.setText(df.format(min));
    editMax.setText(df.format(max));
}

public float distanceBetween(LatLng p1, LatLng p2) {
    double metersPerDegree = 111222.0;
    double longDistance = (p1.longitude-p2.longitude)*metersPerDegree*Math.cos((p1.latitude+p2.latitude)/2);
    double latDistance = (p1.latitude-p2.latitude)*metersPerDegree;
    return (float)Math.sqrt((latDistance*latDistance) + (longDistance*longDistance));
}

public static void setAlpha(float a) {
    alpha = a;
}

public static float getAlpha() {
    return alpha;
}

    public static void defaultSlider() {
        DecimalFormat df = new DecimalFormat("#.#");
        sliderMin = defaultSliderMin;
        sliderMax = defaultSliderMax;
        updateEditText(sliderMin, sliderMax);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("pref_min_elevation", df.format(sliderMin));
        edit.putString("pref_max_elevation", df.format(sliderMax));
        edit.apply();
    }

    public static void deleteMarker(CustomMarker marker) {
        markers.remove(marker);
        marker.getMarker().remove();
    }

    private URI UritoURI(Uri fileUri) {
        URI juri = null;
        try {
        juri = new java.net.URI(fileUri.getScheme(),
                fileUri.getSchemeSpecificPart(),
                fileUri.getFragment());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return juri;
    }

    public static void scanDEMs() {
        //scan DEM directory
        String path = demDirectory;
        Dem dem;
        dems = new ArrayList<Dem>();
        Log.i("Files", "Path: " + path);
        File f = new File(path);
        Polyline outline;
        demOutlines = new ArrayList<Polyline>();

        if (f.isDirectory()) {
            File file[] = f.listFiles();
            Log.i("File", file.toString());

            for (int i=0; i < file.length; i++)
            {
                Log.d("Files", "FileName:" + file[i].getName());
                dem = ReadGeoTiffMetadata.readMetadata(file[i]);
                if(i==0) {
                    demBounds = new LatLngBounds(new LatLng(dem.getSw_lat(), dem.getSw_long()),
                            new LatLng(dem.getNe_lat(), dem.getNe_long()));
                }
                dems.add(dem);
                demOutlines.add(map.addPolyline(new PolylineOptions().add(new LatLng(dem.getSw_lat(), dem.getSw_long()))
                        .add(new LatLng(dem.getSw_lat(), dem.getNe_long()))
                        .add(new LatLng(dem.getNe_lat(), dem.getNe_long()))
                        .add(new LatLng(dem.getNe_lat(), dem.getSw_long()))
                        .add(new LatLng(dem.getSw_lat(), dem.getSw_long()))
                        .color(Color.RED)));
                demBounds = demBounds.including(new LatLng(dem.getSw_lat(), dem.getSw_long()));
                demBounds = demBounds.including(new LatLng(dem.getNe_lat(), dem.getNe_long()));
            }
        }
    }

    public void loadInitialDEM() {
        //attempt to load last used DEM, if it still exists
        Log.d("demfilename", prefs.getString("last_dem", "foo"));
        File demFile = new File(prefs.getString("last_dem", "foo"));
        if(demFile.isFile()) {
            ElevationRaster raster = new ElevationRaster();
            new ReadElevationRasterTask(this, raster, demFile.getName()).execute(UritoURI(Uri.fromFile(demFile)));
            setCurrentlyLoaded(prefs.getString("last_dem", "foo"));
            return;
        }
        String path = demDirectory;
        File f = new File(path);

        //if DEM dir doesn't exist, create it and copy sample TIFF in, then open it
        if (!f.isDirectory()) {
            f.mkdir();
            copyAssets();
            ElevationRaster raster = new ElevationRaster();
            new ReadElevationRasterTask(this, raster).execute(UritoURI(Uri.fromFile(new File(demDirectory+"Feldun.tif"))));
            setCurrentlyLoaded(demDirectory+"Feldun.tif");
            return;
        }
        //selected directory exists
        else {
            //list files in DEM dir
            File file[] = f.listFiles();
            ArrayList<File> tiffs = new ArrayList<File>();

            //count number of TIFFs in dir
            int count = 0;
            for(int i = 0; i<file.length; i++) {
                if(file[i].getName().contains(".tif")) {
                    count++;
                    tiffs.add(file[i]);
                }
            }

            //if no TIFFs, copy sample into dir and open
            if (count == 0) {
                copyAssets();
                ElevationRaster raster = new ElevationRaster();
                new ReadElevationRasterTask(this, raster).execute(UritoURI(Uri.fromFile(new File(demDirectory+"Feldun.tif"))));
                setCurrentlyLoaded(demDirectory+"Feldun.tif");
            }
            //if one TIFF, open it
            else if(count == 1) {
                ElevationRaster raster = new ElevationRaster();
                new ReadElevationRasterTask(this, raster, tiffs.get(0).getName()).execute(UritoURI(Uri.fromFile(tiffs.get(0))));
                setCurrentlyLoaded(tiffs.get(0).getPath());
            }
            //if multiple TIFFs, let user choose
            else {
                Intent intent = new Intent("com.filebrowser.DataFileChooser");
                intent.putExtra("path", demDirectory);
                startActivityForResult(intent, INITIAL_LOAD);
            }
        }
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        String filename = "Feldun.tif";
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(demDirectory, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public void setCurrentlyLoaded(String filename) {
        Dem dem;
        for(int i = 0; i<dems.size(); i++) {
            dem = dems.get(i);
            Log.d("filename", filename);
            Log.d("dem filename", dem.getFilename());
            if (filename.equals(dem.getFilename())) {
                currentlyLoaded = dem;
            }
        }
    }

    public static void updateSlider() {
        if (userLocation != null ) {
            //get level from seekbar
            seekBar.setMax(255);
            seekBar.setProgress(128);
            int waterLevel = seekBar.getProgress();

            //update text block
            waterLevelMeters = sliderMin + ((double)waterLevel*(sliderMax-sliderMin)/255.0);
            String elevation = new DecimalFormat("#.#").format(waterLevelMeters);
            String waterElevationText = "Elevation: " + elevation + "m";
            waterElevationTextView.setText(waterElevationText);

            //update other text block
            double elevationDouble = field.elevationFromLatLng(userLocation);
            double elevationDelta =  elevationDouble - waterLevelMeters;
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
            ElevationTextView.setText(ElevationText);

            //update marker text
            CustomMarker.setWaterElevation(waterLevelMeters);
        }
        updateColors(field);
    }

    public static void removeDemOutlines() {
        Iterator<Polyline> outlines = demOutlines.iterator();
        while(outlines.hasNext()) {
            outlines.next().remove();
        }
    }

}