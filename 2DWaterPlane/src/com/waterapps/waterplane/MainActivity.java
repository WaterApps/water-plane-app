package com.waterapps.waterplane;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.waterapps.lib.DownloadDem;

import static android.graphics.Color.HSVToColor;
import static android.os.Environment.getExternalStorageDirectory;

/**
 * The app's main activity.
 */
public class MainActivity extends Activity implements OnMapClickListener {
    private static final int ADD_MODE = 1;
    private static final int LINE_MODE = 57832;
    private static final int MAX_PIXELS_PER_MB = 1200;
    GroundOverlay prevoverlay;
    static DemData demData;
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
    static LinearLayout dlControls;
    static ActionBar actionBar;
    static boolean currentlyDrawing;
    public static boolean transparency;
    public static boolean coloring;
    public static boolean hasGPS;
    public static Button buttonDelete;
    static Button buttonDeleteLine;
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
    private static Context context;
    static boolean following;
    static ArrayList<DemFile> demFiles;
    static DemFile currentlyLoaded;
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
    static Button DlButton;
    static Button DlCancelButton;
    static ArrayList<Polygon> demOutlines;
    static ArrayList<Marker> demMarkers = new ArrayList<Marker>();
    public static boolean mapReady;
    static Resources resources;
    PolylineOptions currentLineOptions;
    Polyline currentLine;
    ArrayList<MapLine> lines;
    ArrayList<Marker> lineJoints;
    static Button buttonShowProfile;
    public static ImageView iv;
    public static boolean profile;
    private static long enqueue;

    int demDownloadCount = 1;
    int demFinishedCount = 1;
    static int currentDemDownloads = 0;
    float dlWidth;
    float aspect = 1;
    float s;
    LatLng dlCenter;
    Polygon gdlArea;
    static BroadcastReceiver receiver;
    static Queue<DemInProgress> progress = new LinkedList<DemInProgress>();
    static float density;
    public static Context getContext() {
        return context;
    }
    static MainActivity that;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        that = this;
        GdalUtils.init();
        profile = false;
        lines = new ArrayList<MapLine>();
        lineJoints = new ArrayList<Marker>();
        currentLineOptions = new PolylineOptions().color(Color.WHITE).width(5.0f);
        currentLine = null;
        resources = getResources();
        mapReady = false;
        demOutlines = new ArrayList<Polygon>();
        markerAB = false;
        CustomMarker.setDensity(getResources().getDisplayMetrics().density);
        density = getResources().getDisplayMetrics().density;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        demDirectory = prefs.getString("dem_dir", getExternalStorageDirectory().toString() + "/dem");
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
        dlControls = (LinearLayout)findViewById(R.id.dlControls);
        actionBar = getActionBar();
        iv = (ImageView) findViewById(R.id.graphView);
        iv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iv.setVisibility(View.GONE);
                mapFrag.getView().setVisibility(View.VISIBLE);
                showElevationControls();
                profile = false;
            }
        });

        //set display mode
        transparency = prefs.getBoolean("transparency_bool", true);
        coloring = prefs.getBoolean("coloring", false);
        drag_mode = prefs.getBoolean("drag_mode", false);

	    getActionBar().setCustomView(R.layout.custom_ab);
        actionBar.setDisplayShowCustomEnabled(true);
		MyMapFragment mapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map);
		map = mapFragment.getMap();
        MapLine.setMap(map);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);
		map.setOnMapClickListener(this);
		UiSettings uiSettings = map.getUiSettings();
        waterElevationTextView = (TextView) findViewById(R.id.text);
		ElevationTextView = (TextView) findViewById(R.id.text2);

        //set ups markers
		userMarker = map.addMarker(new MarkerOptions()
        .position(map.getCameraPosition().target)
        .title("You are here")
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.person)));
		MarkerHandler markerListener = new MarkerHandler();
		map.setOnMarkerDragListener(markerListener);
        map.setOnMarkerClickListener(markerListener);

        //set up map ui
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setTiltGesturesEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		
		mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		demData.setMapFragment(mapFrag);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(255);
        seekBar.setProgress(128);
		demData.setSeekBar(seekBar);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        demData = new DemData(bitmap, new LatLng(0.0, 0.0), new LatLng(0.0, 0.0), 0.0, 0.0);
		userLocation = new LatLng(0.0, 0.0);
		markers = new ArrayList<CustomMarker>();
		mode = 0;

		CustomMarker.setDemData(demData);
		CustomMarker.setMap(map);

		demData.prevoverlay = demData.createOverlay(map);
		configSeekbar(demData, prevoverlay);
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

		//get the Buttons
        hideButton = (Button)findViewById(R.id.buttonHideMarker);
        showButton = (Button)findViewById(R.id.buttonShowMarker);
        buttonDelete = (Button) findViewById(R.id.buttonDeleteMarker);
        buttonDeleteLine = (Button) findViewById(R.id.buttonDeleteLine);
        buttonShowProfile = (Button) findViewById(R.id.buttonShowProfile);
        DlButton = (Button) findViewById(R.id.dlButton);
        DlCancelButton = (Button) findViewById(R.id.dlCancelButton);
        final Button buttonPlus = (Button) findViewById(R.id.buttonPlus);
        final Button buttonMinus = (Button) findViewById(R.id.buttonMinus);

        //increments elevation
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Increase elevation
            	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            	seekBar.setProgress(seekBar.getProgress()+2);
            	updateColors(demData);
            }
        });

        //decrements elevation
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// Decrease elevation
            	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            	seekBar.setProgress(seekBar.getProgress()-2);
            	updateColors(demData);
            }
        });

        DlButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideDlControls();
                showElevationControls();
                onDownloadAreaSelected();
                gdlArea.remove();
            }
        });

        DlCancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideDlControls();
                showElevationControls();
                gdlArea.remove();
            }
        });


        appName = (TextView)findViewById(R.id.appName);

        //hides currently selected marker text
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


        buttonDeleteLine.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonDeleteLine.setVisibility(View.GONE);
                buttonShowProfile.setVisibility(View.GONE);
                showElevationControls();
               Iterator<MapLine> iter = lines.iterator();
               while(iter.hasNext()) {
                   MapLine l = iter.next();
                   if(l.getMarker().equals(MapLine.getSelected())) {
                       //remove from list
                       lines.remove(l);
                       //remove from map
                       l.remove();
                   }
               }
            }
        });

        buttonShowProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonShowProfile.setVisibility(View.GONE);
                buttonDeleteLine.setVisibility(View.GONE);
                //mapFrag.getView().setVisibility(View.GONE);
                //showElevationControls();
                hideElevationControls();
                iv.setVisibility(View.VISIBLE);
                Toast toast = Toast.makeText(getContext(), "Tap to dismiss.", Toast.LENGTH_LONG);
                toast.show();
                profile = true;
                Iterator<MapLine> iter = lines.iterator();
                MapLine l;
                while (iter.hasNext()) {
                    l = iter.next();
                    if(l.getMarker().equals(MapLine.getSelected())) {
                        l.drawProfile();
                    }
                }

            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.sizeBar);
        seekBar.setMax(100);
        seekBar.setProgress(100);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dlWidth = s*(float)progress/100.0f;
                LatLngBounds a = GeoUtils.makeRectangle(dlCenter, dlWidth, aspect);
                ArrayList<LatLng> l = new ArrayList<LatLng>();
                l.add(a.northeast);
                l.add(new LatLng(a.northeast.latitude, a.southwest.longitude));
                l.add(a.southwest);
                l.add(new LatLng(a.southwest.latitude, a.northeast.longitude));
                gdlArea.setPoints(l);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar aspectBar = (SeekBar) findViewById(R.id.aspectBar);
        aspectBar.setMax(100);
        aspectBar.setProgress(49);
        aspectBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                aspect = (progress+1)/50.0f;
                LatLngBounds a = GeoUtils.makeRectangle(dlCenter, dlWidth, aspect);
                ArrayList<LatLng> l = new ArrayList<LatLng>();
                l.add(a.northeast);
                l.add(new LatLng(a.northeast.latitude, a.southwest.longitude));
                l.add(a.southwest);
                l.add(new LatLng(a.southwest.latitude, a.northeast.longitude));
                gdlArea.setPoints(l);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


		updateColors(demData);

        //if GPS isn't enabled, ask user to enable it
        if(hasGPS) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                if (!drag_mode) {
                    drag_mode = true;
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getContext());

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


        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        int mem = manager.getLargeMemoryClass();
        Log.d("large memory", Integer.toString(mem));
        int smallmem = manager.getLargeMemoryClass();
        Log.d("small memory", Integer.toString(smallmem));

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
        updateColors(demData);
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
            //set all checkboxes to the correct value
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

                    double elevationDouble = demData.elevationFromLatLng(userLocation);
                    double elevationDelta =  elevationDouble - waterLevelMeters;
                    String ElevationText;
                    TextView ElevationTextView = (TextView) findViewById(R.id.text2);

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
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(demData.getDemBounds(), 50));
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
            updateColors(demData);
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
            updateColors(demData);
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
                demData.setWaterLevel(demData.elevationFromLatLng(userLocation));
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

        else if(item.getItemId() == R.id.menu_line) {
            if(mode == LINE_MODE) {
                mode = 0;
                currentLineOptions = new PolylineOptions().color(Color.WHITE).width(5.0f);
            }
            else {
                mode = LINE_MODE;
                currentLineOptions = new PolylineOptions().color(Color.WHITE).width(5.0f);
                hideElevationControls();
                Toast toast = Toast.makeText(getContext(), "Tap to add points to the line.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        //Download DEM of currently visible area
        else if(item.getItemId() == R.id.menu_download) {
            showDlControls();
            hideElevationControls();
            //download DEM of currently visible area
            Log.d("screen", map.getProjection().getVisibleRegion().latLngBounds.toString());
            LatLngBounds demArea = selectArea(map.getProjection().getVisibleRegion().latLngBounds);
            dlWidth = s;
        }

	    else {
	            return super.onOptionsItemSelected(item);
	    }
        return true;
	}

    static boolean inside(Polygon poly, LatLng l) {
        Iterator<LatLng> iter = poly.getPoints().iterator();
        LatLngBounds bounds = new LatLngBounds(iter.next(), iter.next());
        while(iter.hasNext()) {
            bounds = bounds.including(iter.next());
        }
        return bounds.contains(l);
    }

    static void removeDemOutline(LatLng point) {
        Iterator<Polygon> outlines = demOutlines.iterator();
        while(outlines.hasNext()) {
            Polygon outline = outlines.next();
            if (inside(outline, point)) {
                outline.remove();
            }
        }
    }

    static void removeMarker(LatLngBounds b) {
        Iterator<Marker> iter = demMarkers.iterator();
        while(iter.hasNext()) {
            Marker marker = iter.next();
            if (b.contains(marker.getPosition()))
                marker.remove();
        }
    }

    public static void loadDEM(LatLng point) {
        //load DEM if clicked on
        DemFile demFile;
        removeDemOutlines();
        scanDEMs();

        for(int i = 0; i< demFiles.size(); i++) {
            demFile = demFiles.get(i);
            if ( (currentlyLoaded == null) || !demFile.getFilename().equals(currentlyLoaded.getFilename())) {
                if(demFile.getBounds().contains(point)) {
                    currentlyLoaded = demFile;
                    DemData raster = new DemData();
                    new ReadDemDataTask(that, raster, demFile.getFilename()).execute(demFile.getFileUri());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("last_dem", demFile.getFileUri().getPath());
                    editor.commit();
                }
            }
        }
        removeDemOutline(currentlyLoaded.getBounds().getCenter());
        removeMarker(currentlyLoaded.getBounds());
        updateMarkers();
    }

    /**
     * Handles user clicks on the map.
     * Adds markers when needed and loads DEMs if they are clicked on.
     * @param point Location where click occured
     */
	@Override
	public void onMapClick (LatLng point) {
        iv.setVisibility(View.GONE);
        mapFrag.getView().setVisibility(View.VISIBLE);
        profile = false;
        //handle adding a line
        if(mode == LINE_MODE) {
            if(demData.getBounds().contains(point)) {
                if(currentLine != null) {
                    currentLine.remove();
                }
                currentLineOptions.add(point);
                currentLine = map.addPolyline(currentLineOptions);
                lineJoints.add(map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot))
                        .anchor(0.5f, 0.5f)
                        .position(point)));
                if (lineJoints.size() == 2) {
                    mode = 0;
                    lines.add(new MapLine(currentLine, lineJoints));
                    lineJoints = new ArrayList<Marker>();
                    currentLineOptions = new PolylineOptions().color(Color.WHITE).width(5.0f);
                    currentLine = null;
                    showElevationControls();
                }
            }
            else {
                Toast toast = Toast.makeText(this, "Please place all points within DEM boundaries.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

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
                showNormalAB();
                buttonDeleteLine.setVisibility(View.GONE);
                if(mode != LINE_MODE) {
                    showElevationControls();
                }
                MapLine.setSelected(null);
				break;
		}

        loadDEM(point);
    }

    /**
     * Takes a bitmap, latitude/longitude bounds, and a map to create a map overlay
     * @param overlayBitmap Bitmap to display
     * @param bounds Area to display bitmap on
     * @return Google Maps GroundOverlay object
     */
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

    /**
     * updates colors when elevation is changed
     * @param field field to be updated
     */
    public static void updateColors(DemData field) {
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
                    pixels[i] = ((pixels[i] & 0x000000FF) < waterLevel) ? 0xFF0000FF : 0x00000000;
                }
            }
            else {
                //elevation shading is being used
                for (int i = 0; i < (width * height); i++) {
                    c = pixels[i] & 0x000000FF;
                    pixels[i] = (c < waterLevel) ? hsvColors[c] : 0x00000000;
                }
            }
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            //remove old map overlay and create new one
            //this unfortunately creates annoying flickering
            //currently not aware of any way to avoid this
            GroundOverlay ppo = field.prevoverlay;
            field.prevoverlay = createOverlay(bitmap, field.getDemBounds());
            if (transparency) {
                field.prevoverlay.setTransparency(alpha);
            }
            ppo.remove();
            currentlyDrawing = false;
        }
    }

    /**
     * sets up the seekbar object and text above it
     * @param demData dem object
     * @param overlay map overlay
     */
    private void configSeekbar(final DemData demData, final GroundOverlay overlay) {
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
                    String diff = new DecimalFormat("#.#").format(waterLevelMeters-sliderMin);
                    String waterElevationText = "Elevation: " + elevation + "m (" + diff + "m above min)";
                    waterElevationTextView.setText(waterElevationText);

                    //update other text block
                      double elevationDouble = demData.elevationFromLatLng(userLocation);
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

                    if(profile) {
                        Iterator<MapLine> iter = lines.iterator();
                        MapLine l;
                        while (iter.hasNext()) {
                            l = iter.next();
                            if(l.getMarker().equals(MapLine.getSelected())) {
                                l.drawProfile();
                            }
                        }
                    }
                    else {
                      //visual updates
                      updateMarkers();
                      updateColors(demData);
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateColors(demData);
                updateMarkers();
            }
        });
        seekBar.setProgress(seekBar.getMax()/2);
    }

    /**
     * update the text on all markers currently shown
     */
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

    /**
     * Handles data returned from other activities
     * @param requestCode
     * @param resultCode
     * @param data
     */
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
                DemData raster = new DemData();
                //String filename = fileUri.getPath().split("/")[fileUri.getPath().split("/").length-1];
                String filename = data.getDataString();
                new ReadDemDataTask(this, raster, filename).execute(juri);
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
                DemData raster = new DemData();
                DemFile demFileToLoad = demFiles.get(0);
                String filename = demFileToLoad.getFilename();
                new ReadDemDataTask(this, raster, filename).execute(demFileToLoad.getFileUri());
        }
    }

    public static void onFileRead(DemData raster) {

        demData.setNcols(raster.getNcols());
        demData.setNrows(raster.getNrows());
        demData.setBounds(raster.getBounds());
        raster.calculateTenths();
        demData.setMinElevation((float)raster.getMinElevation());
        demData.setMaxElevation((float)raster.getMaxElevation());
        demData.setBitmap(raster.getBitmap());
        defaultSliderMin = sliderMin;
        defaultSliderMax = sliderMax;
        defaultSlider();
        demData.updatePolyLine();

        //if user is outside of demData, turn off location tracking
        if(!raster.getBounds().contains(userLocation)) {
            drag_mode = true;
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("drag_mode", drag_mode);
            edit.commit();
            userLocation = raster.getCenter();
            userMarker.setPosition(userLocation);
            userMarker.setDraggable(true);
            MarkerHandler.setText();
            Toast toast = Toast.makeText(getContext(), "Press and hold the location marker to drag.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.show();
        }

        if (following) {
            demData.setWaterLevel(demData.elevationFromLatLng(raster.getCenter()));
            Log.d("following", "water level is set");
        }

        updateColors(demData);
        updateSlider();

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(raster.getBounds(), 50));
        removeDemOutline(new GeoRectangle(raster.getBounds()).center());
        removeMarker(raster.getBounds());

    }

    public static void hideElevationControls() {
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

    public static void hideDlControls() {
        dlControls.setVisibility(View.GONE);
    }

    public static void showDlControls() {
        dlControls.setVisibility(View.VISIBLE);
    }

    /**
     * Displays the action bar for when a marker is selected and its text is visible
     */
    public static void showMarkerAB() {
        buttonDelete.setVisibility(View.VISIBLE);
        appName.setVisibility(View.GONE);
        hideButton.setVisibility(View.VISIBLE);
        showButton.setVisibility(View.GONE);
    }

    /**
     * Displays the action bar for when a marker is selected but its text is hidden
     */
    public static void showHiddenMarkerAB() {
        buttonDelete.setVisibility(View.VISIBLE);
        appName.setVisibility(View.GONE);
        hideButton.setVisibility(View.GONE);
        showButton.setVisibility(View.VISIBLE);
    }

    /**
     * Displays the action bar for when no marker is selected
     */
    public static void showNormalAB() {
        buttonDelete.setVisibility(View.GONE);
        appName.setVisibility(View.VISIBLE);
        hideButton.setVisibility(View.GONE);
        showButton.setVisibility(View.GONE);
    }

    /**
     * Updates the textboxes which show slider min/max values
     * @param min Slider min value
     * @param max Slider max value
     */
    public static void updateEditText(float min, float max) {
        DecimalFormat df = new DecimalFormat("#.#");
        editMin.setText(df.format(min));
        editMax.setText(df.format(max));
    }

    /**
     * Set alpha/transparency of map DEM overlay
     * @param a Alpha to set
     */
    public static void setAlpha(float a) {
        alpha = a;
    }

    /**
     * Gets the alpha/transparency value of map DEM overlay
     * @return Alpha of DEM overlay
     */
    public static float getAlpha() {
        return alpha;
    }

    /**
     * Sets elevation slider min/max back to default values
     */
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

    /**
     * Remove the marker from visibility and the list of markers
     * @param marker Marker to be removed
     */
    public static void deleteMarker(CustomMarker marker) {
        markers.remove(marker);
        marker.getMarker().remove();
    }

    /**
     *Converts Android Uri to Java URI
     * @param fileUri Uri to be converted
     * @return Converted URI
     */
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

    /**
     *Looks through contents of DEM directory and displays outlines of all DEMs there
     */
    public static void scanDEMs() {
        //scan DEM directory
        String path = demDirectory;
        DemFile demFile;
        demFiles = new ArrayList<DemFile>();
        Log.i("Files", "Path: " + path);
        File f = new File(path);
        Polyline outline;
        demOutlines = new ArrayList<Polygon>();

        //don't look through it if it isn't a directory
        if (f.isDirectory()) {
            //loop through each file in dir
            File file[] = f.listFiles();
            for (int i=0; i < file.length; i++)
            {
                //get metadata
                demFile = ReadGeoTiffMetadata.readMetadata(file[i]);
                if(i==0) {
                    demBounds = new LatLngBounds(new LatLng(demFile.getSw_lat(), demFile.getSw_long()),
                            new LatLng(demFile.getNe_lat(), demFile.getNe_long()));
                }
                //add to list of dem objects
                demFiles.add(demFile);
                //draw outline on map
                demOutlines.add(map.addPolygon(new PolygonOptions().add(new LatLng(demFile.getSw_lat(), demFile.getSw_long()))
                        .add(new LatLng(demFile.getSw_lat(), demFile.getNe_long()))
                        .add(new LatLng(demFile.getNe_lat(), demFile.getNe_long()))
                        .add(new LatLng(demFile.getNe_lat(), demFile.getSw_long()))
                        .add(new LatLng(demFile.getSw_lat(), demFile.getSw_long()))
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(64, 255, 0, 0))));
                //expand bounds to include each dem
                demBounds = demBounds.including(new LatLng(demFile.getSw_lat(), demFile.getSw_long()));
                demBounds = demBounds.including(new LatLng(demFile.getNe_lat(), demFile.getNe_long()));
                demMarkers.add(map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(textToBitmap("Tap")))
                        .position(new GeoRectangle(demFile.getBounds()).center())
                        .title("Tap")));
            }
        }
    }

    /**
     * Picks which DEM to load upon app start
     */
    private void loadInitialDEM() {
        //attempt to load last used DEM, if it still exists
        Log.d("demfilename", prefs.getString("last_dem", "foo"));
        File demFile = new File(prefs.getString("last_dem", "foo"));
        if(demFile.exists() && demFile.isFile()) {
            DemData raster = new DemData();
            new ReadDemDataTask(this, raster, demFile.getPath()).execute();
            setCurrentlyLoaded(prefs.getString("last_dem", "foo"));
            return;
        }
        String path = demDirectory;
        File f = new File(path);

        //if DEM dir doesn't exist, create it and copy sample TIFF in, then open it
        if (!f.exists()) {
            f.mkdir();
            copyAssets(path);
            DemData raster = new DemData();
            new ReadDemDataTask(this, raster).execute(UritoURI(Uri.fromFile(new File(path+"Feldun.tif"))));
            setCurrentlyLoaded(path+"Feldun.tif");
            return;
        }
        //exists, but is a file
        else if(f.isFile()) {
            for(int i=0; i<10; i++) {
                path = demDirectory + Integer.toString(i);
                f = new File(path);
                if (!f.exists()) {
                    f.mkdir();
                    copyAssets(path);
                    DemData raster = new DemData();
                    Log.e("MainActivity", "dem dir was a file");
                    new ReadDemDataTask(this, raster).execute(UritoURI(Uri.fromFile(new File(path+"/Feldun.tif"))));
                    setCurrentlyLoaded(path+"Feldun.tif");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("last_dem", path+"/Feldun.tif");
                    editor.putString("dem_dir", path);
                    editor.commit();
                    return;
                }
            }
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
                copyAssets(path);
                DemData raster = new DemData();
                new ReadDemDataTask(this, raster).execute(UritoURI(Uri.fromFile(new File(demDirectory+"Feldun.tif"))));
                setCurrentlyLoaded(demDirectory+"Feldun.tif");
            }
            //if one TIFF, open it
            else if(count == 1) {
                DemData raster = new DemData();
                new ReadDemDataTask(this, raster, tiffs.get(0).getName()).execute(UritoURI(Uri.fromFile(tiffs.get(0))));
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

    /**
     * Copies a file from assets to SD
     * @param path Path to asset
     */
    private void copyAssets(String path) {
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
                File outFile = new File(path, filename);
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

    /**
     * copies a file
     * @param in input file stream
     * @param out output file stream
     * @throws IOException
     */
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    /**
     * Tell which DEM is currently loaded, so it isn't reloaded if clicked on
     * @param filename Filename of currently loaded DEM
     */
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

    /**
     * Updates the text near the slider bar
     */
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
            double elevationDouble = demData.elevationFromLatLng(userLocation);
            double elevationDelta =  elevationDouble - waterLevelMeters;
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
            ElevationTextView.setText(ElevationText);

            //update marker text
            CustomMarker.setWaterElevation(waterLevelMeters);
        }
        updateColors(demData);
    }

    /**
     *Removes polylines showing DEM outlines, for use when DEM folder is changed
     */
    public static void removeDemOutlines() {
        Iterator<Polygon> outlines = demOutlines.iterator();
        while(outlines.hasNext()) {
            outlines.next().remove();
        }
    }

    //getters and setters
    public static Resources getResource() {
        return resources;
    }

    public static DemData getDemData() {
        return demData;
    }

    public static int getMapWidth() {
        return mapFrag.getView().getWidth();
    }

    public static int getMapHeight() {
        return mapFrag.getView().getHeight();
    }

    public static float getUserElevation() {
        return (float)demData.elevationFromLatLng(userLocation);
    }

    void DownloadDEM(LatLngBounds extent) {
        new DownloadDem(extent, demDirectory, map, getContext());
    }

    private LatLngBounds selectArea(LatLngBounds screen) {
        LatLng center = GeoUtils.center(screen);
        dlCenter = center;

        //make a square with its side length min(width, height) of screen area
        s = GeoUtils.getWidth(screen) > GeoUtils.getHeight(screen) ? GeoUtils.getHeight(screen) : GeoUtils.getWidth(screen);
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        if ( (s*s)/3 > MAX_PIXELS_PER_MB*manager.getLargeMemoryClass()) {
            s = 3*(float)Math.sqrt(MAX_PIXELS_PER_MB*manager.getLargeMemoryClass());
        }
        LatLngBounds dlArea = GeoUtils.makeRectangle(center, s, aspect);

        PolygonOptions rectOptions = new PolygonOptions()
                .add(dlArea.northeast)
                .add(new LatLng(dlArea.northeast.latitude, dlArea.southwest.longitude))
                .add(dlArea.southwest)
                .add(new LatLng(dlArea.southwest.latitude, dlArea.northeast.longitude))
                .strokeColor(Color.BLUE);
        gdlArea = map.addPolygon(rectOptions);

        return dlArea;
    }

    private void onDownloadAreaSelected() {
        LatLngBounds demArea = GeoUtils.makeRectangle(dlCenter, dlWidth, aspect);
        DownloadDEM(demArea);
    }

    static Bitmap textToBitmap(String text) {
        int width = dpToPx(180);
        int height = dpToPx(20);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(16.0f * density);
        textPaint.setColor(Color.WHITE);
        StaticLayout sl= new StaticLayout(text, textPaint, bitmap.getWidth()-8, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.translate((int)(3.0f * density), (int)(4.0f * density));
        sl.draw(canvas);
        return bitmap;
    }

    static int dpToPx(int dp) {
        return (int) (dp * density + 0.5f);
    }
}