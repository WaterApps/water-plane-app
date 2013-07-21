package com.waterapps.waterplane;

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
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.*;
import com.waterapps.lib.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.graphics.Color.HSVToColor;

public class MainActivity extends Activity implements OnMapClickListener {
private static final int ADD_MODE = 1;
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
public static Resources resources;
static ArrayList<DemFile> demFiles;
DemFile currentlyLoaded;
private boolean firstStart;
public static LatLngBounds demBounds;
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
        CustomMarker.setDensity(getResources().getDisplayMetrics().density);
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
        drag_mode = prefs.getBoolean("drag_mode", false);

	    getActionBar().setCustomView(R.layout.custom_ab);
        actionBar.setDisplayShowCustomEnabled(true);
		MyMapFragment mapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map);
		map = mapFragment.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);
		map.setOnMapClickListener(this);
		UiSettings uiSettings = map.getUiSettings();
        waterElevationTextView = (TextView) findViewById(R.id.text);
		ElevationTextView = (TextView) findViewById(R.id.text2);

        //set ups markers
		userMarker = map.addMarker(new MarkerOptions()
        .position(map.getCameraPosition().target)
        .title("You are here"));
		MarkerHandler markerListener = new MarkerHandler();
		map.setOnMarkerDragListener(markerListener);
        map.setOnMarkerClickListener(markerListener);

        //set up map ui
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setTiltGesturesEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		
		mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		Field.setMapFragment(mapFrag);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(255);
        seekBar.setProgress(128);
		Field.setSeekBar(seekBar);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        field = new Field(bitmap, new LatLng(0.0, 0.0), new LatLng(0.0, 0.0), 0.0, 0.0);
		userLocation = new LatLng(0.0, 0.0);
		markers = new ArrayList<CustomMarker>();
		mode = 0;

		CustomMarker.setField(field);
		CustomMarker.setMap(map);

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

        //increments elevation
		final Button buttonPlus = (Button) findViewById(R.id.buttonPlus);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Increase elevation
            	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            	seekBar.setProgress(seekBar.getProgress()+2);
            	updateColors(field);
            }
        });

        //decrements elevation
        final Button buttonMinus = (Button) findViewById(R.id.buttonMinus);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// Decrease elevation
            	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            	seekBar.setProgress(seekBar.getProgress()-2);
            	updateColors(field);
            }
        });


        appName = (TextView)findViewById(R.id.appName);

        //hides currently selected marker text
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
        hideButton.setVisibility(View.GONE);

        //shows currently selected marker text
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
        showButton.setVisibility(View.GONE);

        //deletes currently selected marker
        buttonDelete = (Button) findViewById(R.id.buttonDeleteMarker);
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
        
		updateColors(field);

        //if GPS isn't enabled, ask user to enable it
        if(hasGPS) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                if (!drag_mode) {
                    drag_mode = true;
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                        //set title
                        alertDialogBuilder.setTitle("GPS is not enabled");

                        //set dialog message
                        alertDialogBuilder
                            .setMessage("Please enable GPS if you want to use the location tracking feature")
                            .setCancelable(false)
                            .setPositiveButton("Continue anyway",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // if this button is clicked, close
                                    // current activity
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

        //display DEM outlines
        scanDEMs();

        //show help on first app start
        firstStart = prefs.getBoolean("first_start", true);
        if (firstStart) {
            //show help
            Intent intent = new Intent(this, IntroActivity.class);
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
        menu.findItem(R.id.menu_coloring).setChecked(coloring);
        menu.findItem(R.id.menu_transparency).setChecked(transparency);
        menu.findItem(R.id.menu_drag).setChecked(!drag_mode);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    if (item.getItemId() == R.id.menu_add) {
        	mode = ADD_MODE;
            Toast toast = Toast.makeText(this, "Tap on the map to place the marker.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return true;
	    }

	    else if (item.getItemId() == R.id.menu_drag) {
            if (drag_mode) {
                //if gps is enabled, use it
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {

                    drag_mode = false;
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean("drag_mode", drag_mode);
                    edit.commit();
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
                    //if gps is not enabled, inform the user
                    Toast toast = Toast.makeText(this, "GPS must be enabled to use this feature.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 100);
                    toast.show();
                }
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
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("drag_mode", drag_mode);
            edit.commit();
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

            Intent intent = new Intent("com.filebrowser.DataFileChooserWaterplane");
            intent.putExtra("path", demDirectory);
            startActivityForResult(intent, 1);
        }

        else if(item.getItemId() == R.id.menu_follow) {
            following = !following;
            item.setChecked(following);
            if (following) {
                field.setWaterLevel(field.elevationFromLatLng(userLocation));
            }
        }

        else if(item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
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
        DemFile demFile;
        for(int i = 0; i< demFiles.size(); i++) {
            demFile = demFiles.get(i);
            if ( (currentlyLoaded == null) || !demFile.getFilename().equals(currentlyLoaded.getFilename())) {
                if(demFile.getBounds().contains(point)) {
                    currentlyLoaded = demFile;
                    ElevationRaster raster = new ElevationRaster();
                    new ReadElevationRasterTask(this, raster, demFile.getFilename()).execute(demFile.getFileUri());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("last_dem", demFile.getFileUri().getPath());
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

    //updates colors when elevation is changed
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
            if (!coloring) {
                //test each pixel, if below water level set blue, else set transparent
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
                //elevation shading is being used
                for (int i = 0; i < (width * height); i++) {
                    if ((pixels[i] & 0x000000FF) < waterLevel) {
                        //water is visible, set pixel to appropriate color
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
            //this unfortunately creates annoying flickering
            //currently not aware of any way to avoid this
            GroundOverlay ppo = field.prevoverlay;
            field.prevoverlay = createOverlay(bitmap, field.getFieldBounds());
            if (transparency) {
                field.prevoverlay.setTransparency(alpha);
            }
            ppo.remove();
            currentlyDrawing = false;
        }
    }

    //sets up the seekbar object and text above it
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

    LocationListener locationListener = new LocationHandler();

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        //handle data from file manager

        if (requestCode == FIRST_START) {
            loadInitialDEM();
            return;
        }

        System.out.print("Intent Handler");
        System.out.print(data);

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
                setCurrentlyLoaded(prefs.getString("last_dem", "foo"));
                return;
            }
            else {
                Toast toast = Toast.makeText(this, "File selected was not a GEOTiff file.", Toast.LENGTH_LONG);
                toast.show();
            }
        }

        if (requestCode == INITIAL_LOAD && data == null) {
                ElevationRaster raster = new ElevationRaster();
                DemFile demFileToLoad = demFiles.get(0);
                String filename = demFileToLoad.getFilename();
                new ReadElevationRasterTask(this, raster, filename).execute(demFileToLoad.getFileUri());
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
        field.updatePolyLine();

        //if user is outside of field, turn off location tracking
        if(!raster.getBounds().contains(userLocation)) {
            drag_mode = true;
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("drag_mode", drag_mode);
            edit.commit();
            userLocation = raster.getCenter();
            userMarker.setPosition(userLocation);
            userMarker.setDraggable(true);
            MarkerHandler.setText();
            Toast toast = Toast.makeText(context, "Press and hold the location marker to drag.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.show();
        }

        if (following) {
            field.setWaterLevel(field.elevationFromLatLng(raster.getCenter()));
            Log.d("following", "water level is set");
        }

        updateColors(field);
        updateSlider();

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(raster.getBounds(), 50));

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

    //calculates distance between two latlng objects
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

    //sets elevation slider min/max back to default values
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

    //remove the marker from visibility and the list of markers
    public static void deleteMarker(CustomMarker marker) {
        markers.remove(marker);
        marker.getMarker().remove();
    }

    //converts Android Uri to Java URI
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

    //looks through contents of DEM directory and displays outlines of all DEMs there
    public static void scanDEMs() {
        //scan DEM directory
        String path = demDirectory;
        DemFile demFile;
        demFiles = new ArrayList<DemFile>();
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
                demFile = ReadGeoTiffMetadata.readMetadata(file[i]);
                if(i==0) {
                    demBounds = new LatLngBounds(new LatLng(demFile.getSw_lat(), demFile.getSw_long()),
                            new LatLng(demFile.getNe_lat(), demFile.getNe_long()));
                }
                demFiles.add(demFile);
                demOutlines.add(map.addPolyline(new PolylineOptions().add(new LatLng(demFile.getSw_lat(), demFile.getSw_long()))
                        .add(new LatLng(demFile.getSw_lat(), demFile.getNe_long()))
                        .add(new LatLng(demFile.getNe_lat(), demFile.getNe_long()))
                        .add(new LatLng(demFile.getNe_lat(), demFile.getSw_long()))
                        .add(new LatLng(demFile.getSw_lat(), demFile.getSw_long()))
                        .color(Color.RED)));
                demBounds = demBounds.including(new LatLng(demFile.getSw_lat(), demFile.getSw_long()));
                demBounds = demBounds.including(new LatLng(demFile.getNe_lat(), demFile.getNe_long()));
            }
        }
    }

    //picks which DEM to load upon app start
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
                Intent intent = new Intent("com.filebrowser.DataFileChooserWaterplane");
                intent.putExtra("path", demDirectory);
                startActivityForResult(intent, INITIAL_LOAD);
            }
        }
    }

    //copies a file from assets to SD
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

    //tell app which DEM is currently loaded, so it isn't reloaded if clicked on
    public void setCurrentlyLoaded(String filename) {
        DemFile demFile;
        for(int i = 0; i< demFiles.size(); i++) {
            demFile = demFiles.get(i);
            Log.d("filename", filename);
            Log.d("demFile filename", demFile.getFilename());
            if (filename.equals(demFile.getFilename())) {
                currentlyLoaded = demFile;
            }
        }
    }

    public static void updateSlider() {
        if (userLocation != null ) {
            //get level from seekbar
            seekBar.setMax(255);
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

    //remove polylines showing DEM outlines, for use when DEM folder is changed
    public static void removeDemOutlines() {
        Iterator<Polyline> outlines = demOutlines.iterator();
        while(outlines.hasNext()) {
            outlines.next().remove();
        }
    }

}