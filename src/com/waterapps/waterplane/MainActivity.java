package com.waterapps.waterplane;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.waterapps.lib.DemData;
import org.waterapps.lib.DemLoadUtils;
import org.waterapps.lib.WmacListener;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.filebrowser.DataFileChooser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.openatk.openatklib.atkmap.ATKMap;
import com.openatk.openatklib.atkmap.ATKSupportMapFragment;
import com.openatk.openatklib.atkmap.listeners.ATKMapClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointDragListener;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonClickListener;
import com.openatk.openatklib.atkmap.models.ATKPoint;
import com.openatk.openatklib.atkmap.models.ATKPolygon;
import com.openatk.openatklib.atkmap.views.ATKPointView;
import com.openatk.openatklib.atkmap.views.ATKPolygonView;
import org.waterapps.lib.DataDownload.DemInProgress;
import org.waterapps.lib.DataDownload.DownloadDem;
import org.waterapps.lib.DataDownload.GeoUtils;

/**
 * The app's main activity.
 */
public class MainActivity extends FragmentActivity implements ATKMapClickListener, WmacListener, ATKPointDragListener, ATKPointClickListener, ATKPolygonClickListener {
    private static final int ADD_MODE = 1;
    private static final int LINE_MODE = 57832;
    private static final int MAX_PIXELS_PER_MB = 1200;
    
	private static final float START_LAT = 40.428712f;
	private static final float START_LNG = -86.913819f;
	private static final float START_ZOOM = 17.0f;
	
    private static final int FIRST_START = 42;
    private static final int FILE_CHOOSER = 6503;
	private static final int FILE_PATH_CHOOSER = 6504;
	
    private DemDataWrapper demDataWrapper;
    private static List<CustomMarker> markers;
    private static int mode;
    public static double waterLevel;
    private LocationManager locationManager;
    private static ATKPointView userMarker;
    
    static TextView ElevationTextView;
    static boolean drag_mode = false;
    public static boolean downloading;
    static LinearLayout elevationControls;
    static LinearLayout markerBottomText;
    static LinearLayout dlControls;
    static ActionBar actionBar;
    static boolean currentlyDrawing;
    public static boolean transparency;
    public static boolean coloring;
    static float alpha;
    public static boolean hasGPS;
    public static Button buttonDelete;
    static Button buttonDeleteLine;
    public static float sliderMin;
    public static float sliderMax;
    static TextView editMin;
    static TextView editMax;
    public static SharedPreferences prefs;
    private static Context context;
    static boolean following;
    private boolean firstStart;
    static SeekBar seekBar;
    static TextView waterElevationTextView;
    private View focusThief;
    static boolean markerAB;
    static TextView appName;
    static Button showButton;
    static Button hideButton;
    static Button DlButton;
    static Button DlCancelButton;
    static Resources resources;
    ArrayList<MapLine> mapLines;
    static Button buttonShowProfile;
    public static ImageView iv;
    public static boolean profile;
    private ATKSupportMapFragment atkMapFragment;
    public static ATKMap map;
	private DemLoadUtils demLoadUtils;
	private UiSettings mapSettings;
	ATKPointView selectedLine;
	OnSharedPreferenceChangeListener onSharedPrefChangeListener;


    int demDownloadCount = 1;
    int demFinishedCount = 1;
    static int currentDemDownloads = 0;
    float dlWidth;
    float aspect = 1;
    float s;
    LatLng dlCenter;
    ATKPolygonView gdlArea;
    static BroadcastReceiver receiver;
    static Queue<DemInProgress> progress = new LinkedList<DemInProgress>();
    static float density;
    public static Context getContext() {
        return context;
    }
//    static MainActivity that;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
		atkMapFragment = (ATKSupportMapFragment) fm.findFragmentById(R.id.map);
		
		if (savedInstanceState == null) {
			// First incarnation of this activity.
			atkMapFragment.setRetainInstance(true);
		} else {
			// Reincarnated activity. The obtained map is the same map instance in the previous
			// activity life cycle. There is no need to reinitialize it.
			map = atkMapFragment.getAtkMap();
		}
		setUpMapIfNeeded();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        onSharedPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        	  public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
        		  if (key.equals("pref_max_elevation")) {
        			  updateSliderMax(Float.parseFloat(sharedPrefs.getString("pref_max_elevation", "300.0")));
        		  } else if (key.equals("pref_min_elevation")) {
        			  updateSliderMin(Float.parseFloat(sharedPrefs.getString("pref_min_elevation", "100.0")));
        	      }
        	  }
        	};
        prefs.registerOnSharedPreferenceChangeListener(onSharedPrefChangeListener);
        
        context = this;
        
        demLoadUtils = new DemLoadUtils(this, map, prefs);
		demLoadUtils.registerWmacListener(this);
		
        downloading = false;
//        that = this;
        profile = false;
        mapLines = new ArrayList<MapLine>();
        resources = getResources();

		markerAB = false;
        CustomMarker.setDensity(getResources().getDisplayMetrics().density);
        density = getResources().getDisplayMetrics().density;

		sliderMin = 241.94f;
        sliderMax = 250.925f;
        sliderMin = Float.parseFloat(prefs.getString("pref_min_elevation", "100.0"));
        sliderMax = Float.parseFloat(prefs.getString("pref_max_elevation", "300.0"));
        
        currentlyDrawing = false;
        
        focusThief = findViewById(R.id.focus_thief);
        
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
                atkMapFragment.getView().setVisibility(View.VISIBLE);
                showElevationControls();
                profile = false;
            }
        });

        //set display mode
        drag_mode = prefs.getBoolean("drag_mode", false);

	    getActionBar().setCustomView(R.layout.custom_ab);
        actionBar.setDisplayShowCustomEnabled(true);
        
        
        map.getUiSettings();
        waterElevationTextView = (TextView) findViewById(R.id.text);
		ElevationTextView = (TextView) findViewById(R.id.text2);

        //set up markers
		LatLng where = map.getCameraPosition().target;
		ATKPoint userPoint = new ATKPoint("User", where);
		userMarker = map.addPoint(userPoint);
		userMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.person), 100, 200);
//		ATKPointDragListener pointDragListener = null;
//		map.setOnPointDragListener(pointDragListener);
//		ATKPointClickListener pointClickListener = null;
//		map.setOnPointClickListener(pointClickListener);
		
//		userMarker = map.addMarker(new MarkerOptions()
//        .position(map.getCameraPosition().target)
//        .title("You are here")
//        .icon(BitmapDescriptorFactory.fromResource(R.drawable.person)));


        //set up map ui
//		uiSettings.setRotateGesturesEnabled(false);
//		uiSettings.setTiltGesturesEnabled(false);
//		uiSettings.setZoomControlsEnabled(false);
		
//		mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
//		demData.setMapFragment(mapFrag);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(255);
        seekBar.setProgress(128);
//		userLocation = new LatLng(0.0, 0.0);
		markers = new ArrayList<CustomMarker>();
		mode = 0;
//		CustomMarker.setMap(map);

		//demData.prevoverlay = demData.createOverlay(map);

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
//        if(hasGPS) {
//		    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//        }

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
            	demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
            }
        });

        //decrements elevation
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// Decrease elevation
            	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            	seekBar.setProgress(seekBar.getProgress()-2);
            	demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
            }
        });

        DlButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideDlControls();
                showElevationControls();
                onDownloadAreaSelected();
//                gdlArea.remove();
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
               for (int i = mapLines.size()-1; i > -1; i--){
            	   if(mapLines.get(i).getMarker().equals(getSelectedLine())) {
                       //remove from map
                       mapLines.get(i).remove();
                     //remove from list
                       mapLines.remove(mapLines.get(i));
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
                Toast toast = Toast.makeText(getContext(), "Tap to dismiss.", Toast.LENGTH_SHORT);
                toast.show();
                profile = true;
                Iterator<MapLine> iter = mapLines.iterator();
                MapLine l;
                while (iter.hasNext()) {
                    l = iter.next();
                    if(l.getMarker().equals(getSelectedLine())) {
                        l.drawProfile(userMarker.getAtkPoint().position);
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
                gdlArea.getAtkPolygon().boundary.clear();
                gdlArea.getAtkPolygon().boundary.add(a.northeast);
                gdlArea.getAtkPolygon().boundary.add(new LatLng(a.northeast.latitude, a.southwest.longitude));
                gdlArea.getAtkPolygon().boundary.add(a.southwest);
                gdlArea.getAtkPolygon().boundary.add(new LatLng(a.southwest.latitude, a.northeast.longitude));
                gdlArea.update();
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
                gdlArea.getAtkPolygon().boundary.clear();
                gdlArea.getAtkPolygon().boundary.add(a.northeast);
                gdlArea.getAtkPolygon().boundary.add(new LatLng(a.northeast.latitude, a.southwest.longitude));
                gdlArea.getAtkPolygon().boundary.add(a.southwest);
                gdlArea.getAtkPolygon().boundary.add(new LatLng(a.southwest.latitude, a.northeast.longitude));
                gdlArea.update();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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

        context.getSystemService(Context.ACTIVITY_SERVICE);

        //load initial DEM if help menu isn't being shown
        if(!firstStart) {
            demLoadUtils.loadInitialDem(this);
        }
    }
    
//    LocationListener locationListener = new LocationHandler(demDataWrapper, userMarker);

  //Group: ATK
  	private void setUpMapIfNeeded() {
  		if (map == null) {
  			//Map is null try to find it
  			map = ((ATKSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getAtkMap();
  		}

  		if (atkMapFragment.getRetained() == false) {
  			//New map, we need to set it up
  			setUpMap();
  			
  			//Move to where we were last time
  			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
  			Float startLat = prefs.getFloat("StartupLat", START_LAT);
  			Float startLng = prefs.getFloat("StartupLng", START_LNG);
  			Float startZoom = prefs.getFloat("StartupZoom", START_ZOOM);
  			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLat, startLng), startZoom));
  		}
  		
  		//Setup atkmap listeners, note: these are the atkmap listeners, per object listeners can override these ie. (ATKPoint.setOnClickListener())
  		map.setOnPointClickListener(this);
  		map.setOnPointDragListener(this);
  		map.setOnPolygonClickListener(this);
  		map.setOnMapClickListener(this);
  	}
  	
  	private void setUpMap() {
  		//Set map settings
  		mapSettings = map.getUiSettings();
  		mapSettings.setZoomControlsEnabled(false);
  		mapSettings.setMyLocationButtonEnabled(false);
  		mapSettings.setTiltGesturesEnabled(false);
  		map.setMyLocationEnabled(true);
  		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
  	}
    
    @Override
	public void onPause() {
		super.onPause();
//		getPreferenceScreen().getSharedPreferences()
//        	.unregisterOnSharedPreferenceChangeListener(this);
//		locationManager.removeUpdates(locationListener);
    }

	@Override
	public void onResume() {
		super.onResume();
//		getPreferenceScreen().getSharedPreferences()
//        	.registerOnSharedPreferenceChangeListener(this);
		Log.w("onresume", "ya");
		if (demDataWrapper != null) {
			demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
		}
//        if (hasGPS)
//		    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

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
                    LatLng userLocation = new LatLng(
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
                    updateSliderUserText();
                    CustomMarker.setUserElevation(demDataWrapper.getDemData().getElevationFromLatLng(userLocation));
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
	    		LatLng userLocation = map.getCameraPosition().target;
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
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(demDataWrapper.getDemData().getBounds(), 50));
        }

        else if(item.getItemId() == R.id.menu_coloring) {
            SharedPreferences.Editor edit = prefs.edit();
            if (coloring) {
                setColoring(false);
            }
            else {
            	setColoring(true);
            }
            edit.putBoolean("coloring", getColoring());
            edit.commit();
            item.setChecked(getColoring());
            demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
        }

        else if(item.getItemId() == R.id.menu_transparency) {
            SharedPreferences.Editor edit = prefs.edit();
            if (getTransparency()) {
            	setTransparency(false);
            }
            else {
            	setTransparency(true);
            }
            edit.putBoolean("transparency_bool", getTransparency());
            edit.commit();
            item.setChecked(getTransparency());
            demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
        }

        else if (item.getItemId() == R.id.menu_choose_dem) {
            //opens file manager
            Intent intent = new Intent(this, DataFileChooser.class);
            intent.putExtra("path", getDemLoadUtils().getDemDirectory());
//            intent.putExtra("path", demDirectory);
            startActivityForResult(intent, FILE_CHOOSER);
        }

        else if(item.getItemId() == R.id.menu_follow) {
            following = !following;
            item.setChecked(following);
            if (following) {
               setWaterLevel(demDataWrapper.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position), demDataWrapper);
            }
        }

        else if(item.getItemId() == R.id.menu_settings) {
        	Bundle bundle = new Bundle();
        	bundle.putString("min_elev", Float.toString((float)getDemData().getMinElevation()));
        	bundle.putString("max_elev", Float.toString((float)getDemData().getMaxElevation()));
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            return true;
        }

        else if(item.getItemId() == R.id.menu_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        }

        else if(item.getItemId() == R.id.menu_line) {
//            if(mode == LINE_MODE) {
//                mode = 0;
//                currentLineOptions = new PolylineOptions().color(Color.WHITE).width(5.0f);
//            }
//            else {
                mode = LINE_MODE;
//                currentLineOptions = new PolylineOptions().color(Color.WHITE).width(5.0f);
                hideElevationControls();
                Toast toast = Toast.makeText(getContext(), "Tap to add points to the line.", Toast.LENGTH_SHORT);
                toast.show();
//            }
        }

        //Download DEM of currently visible area
        else if(item.getItemId() == R.id.menu_download) {
            if(!downloading) {
                showDlControls();
                hideElevationControls();
                //download DEM of currently visible area
                LatLngBounds demArea = selectArea(map.getProjection().getVisibleRegion().latLngBounds);
                dlWidth = s;
            } else {
                Toast toast = Toast.makeText(this, "Only one DEM download supported at once", Toast.LENGTH_LONG);
                toast.show();
            }
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
	
  /**
  * Takes a new water elevation and updates the bitmap to reflect the change
  * @param level New water level
  */
 public static void setWaterLevel(double level, DemDataWrapper demDataWrapper) {
     seekBar.setProgress((int)(255.0*(level-MainActivity.sliderMin)/(MainActivity.sliderMax-MainActivity.sliderMin)));
     demDataWrapper.updateColors(level, coloring, transparency, alpha);
 }
 
    /**
     * sets up the seekbar object and text above it
     * @param demData dem object
     * @param overlay map overlay
     */
    private void configSeekbar() {
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(255);
        seekBar.setProgress(128);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (userMarker.getAtkPoint().position != null ) {
                    //get level from seekbar
                	updateSliderWaterPlaneText();
                    //update other text block
                	updateSliderUserText();
                    //update marker text
                	CustomMarker.setWaterElevation(waterLevel);

                	//update the user line on markerline cross section profiles
                    if(profile) {
                    	Iterator<MapLine> iter = mapLines.iterator();
                        MapLine l;
                        while (iter.hasNext()) {
                            l = iter.next();
                            if(l.getMarker().equals(getSelectedLine())) {
                                l.drawProfile(userMarker.getAtkPoint().position);
                            }
                        }
                    }
                    else {
                      //visual updates
                      updateMarkers();
                      demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
                updateMarkers();
            }
        });
        seekBar.setProgress(seekBar.getMax()/2);
    }

    /**
     * update the text on all markers currently shown
     */
    public static void updateMarkers() {
    	Log.w("updating", "heregoes");
        Iterator<CustomMarker> i = markers.iterator();
        CustomMarker marker;

        while (i.hasNext()) {
             marker = i.next();
             marker.updateMarker();
        }
    }

    /**
     * Handles data returned from other activities
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		    	// Handle first ever startup: show help screen
    			if (requestCode == FIRST_START) {
    				demLoadUtils.loadInitialDem(this);
    				return;
    			}
    			
    			//Handle data from file path chooser (occurs when changing DEM folder in settings activity)
    			if (requestCode == FILE_PATH_CHOOSER) {
    				SettingsActivity.updateDemFolder();
    				demLoadUtils.setNewDemDirectory(data.getStringExtra("directory"));  //edits the path pref within this function
    			}
    			
    			//Handle data from file chooser
    			if (requestCode == FILE_CHOOSER) {
    				//TODO Disallow choosing the current demfile!!!
    				if (data == null) {
    				}
    				//TODO the "if contains .tif" means we can still only load tifs
    				else if (data.getData().toString().contains(".tif")) {
    					File f = new File(data.getData().getPath());
    					if (demLoadUtils.getDemDirectory() != f.getParent()) {
    						demLoadUtils.setNewDemDirectory(f.getParent());
    					}
    					demLoadUtils.loadDem(this, data.getData().getPath());
    				}
    			}
    		}

//    public void onFileRead(DemDataWrapper demDataWrapper) {
//        //if user is outside of demData, turn off location tracking
//        if(!demDataWrapper.getBounds().contains(userLocation)) {
//            drag_mode = true;
//            SharedPreferences.Editor edit = prefs.edit();
//            edit.putBoolean("drag_mode", drag_mode);
//            edit.commit();
//            userLocation = demDataWrapper.getCenter();
//            userMarker.setPosition(userLocation);
//            userMarker.setDraggable(true);
//            MarkerHandler.setText();
//            Toast toast = Toast.makeText(getContext(), "Press and hold the location marker to drag.", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 100);
//            toast.show();
//        }
//        demDataWrapper.updateColors(waterLevelMeters, coloring, transparency, alpha);
//        updateSlider();
//        map.animateCamera(CameraUpdateFactory.newLatLngBounds(getDemData().getBounds(), 50));
//        removeDemOutline(new GeoRectangle(raster.getBounds()).center());
//        removeMarker(raster.getBounds());
//    }

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
     * Remove the marker from visibility and the list of markers
     * @param marker Marker to be removed
     */
    public static void deleteMarker(CustomMarker marker) {
        markers.remove(marker);
        marker.getAtkPointView().remove();
        marker = null;
    }

    //getters and setters
    public static Resources getResource() {
        return resources;
    }

    public void DownloadDEM(LatLngBounds extent) {
        new DownloadDem(extent, getDemLoadUtils().getDemDirectory(), map, this, gdlArea);
    }

    private LatLngBounds selectArea(LatLngBounds screen) {
        LatLng center = GeoUtils.getCenter(screen);
        dlCenter = center;

        //make a square with its side length min(width, height) of screen area
        s = GeoUtils.getWidth(screen) > GeoUtils.getHeight(screen) ? GeoUtils.getHeight(screen) : GeoUtils.getWidth(screen);
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        if ( (s*s)/3 > MAX_PIXELS_PER_MB*manager.getLargeMemoryClass()) {
            s = 3*(float)Math.sqrt(MAX_PIXELS_PER_MB*manager.getLargeMemoryClass());
        }
        LatLngBounds dlArea = GeoUtils.makeRectangle(center, s, aspect);
        List<LatLng> list = new ArrayList<LatLng>();
		list.add(dlArea.northeast);
		list.add(new LatLng(dlArea.northeast.latitude, dlArea.southwest.longitude));
        list.add(dlArea.southwest);
        list.add(new LatLng(dlArea.southwest.latitude, dlArea.northeast.longitude));
        ATKPolygon rectangle = new ATKPolygon("rect", list);
        rectangle.viewOptions.setStrokeColor(Color.BLUE);
//        rectangle.viewOptions.setFillColor(Color.TRANSPARENT);
        gdlArea = map.addPolygon(rectangle);
        gdlArea.setFillColor(Color.TRANSPARENT);

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
    
    /**
     * Updates the textbox which show slider max value
     * @param min Slider min value
     */
    public void updateSliderMin(float min) {
    	if (min < getDemData().getMinElevation()) {
    		min = (float) getDemData().getMinElevation();
    	}
        DecimalFormat df = new DecimalFormat("#.#");
        editMin.setText(df.format(min));   
        sliderMin = min;
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("pref_min_elevation", df.format(sliderMin));
        edit.commit();
    }
    
    /**
     * Updates the textbox which show slider max value
     * @param max Slider max value
     */
    public void updateSliderMax(float max) {
    	if (max > getDemData().getMaxElevation()) {
    		max = (float) getDemData().getMaxElevation();
    	}
        DecimalFormat df = new DecimalFormat("#.#");
        editMax.setText(df.format(max));     
        sliderMax = max;
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("pref_max_elevation", df.format(sliderMax));
        edit.commit();
    }
    
    public void updateSliderWaterPlaneText() {
    	int newWaterLevel = seekBar.getProgress();
    	waterLevel = sliderMin + ((double)newWaterLevel*(sliderMax-sliderMin)/255.0);
        String elevation = new DecimalFormat("#.#").format(waterLevel);
        String waterElevationText = "Water Plane Elevation: " + elevation + "m";
        waterElevationTextView.setText(waterElevationText);
    }
    
    public void updateSliderUserText() {
    	double elevationDouble = demDataWrapper.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position);
//        double elevationDelta =  elevationDouble - waterLevel;
        String ElevationText;
        TextView ElevationTextView = (TextView) findViewById(R.id.text2);

        if (elevationDouble == 0.0) {
          ElevationText = "Current location isn't inside the data area.";
        }
        else {
          String elevationString = new DecimalFormat("#.#").format(Math.abs(elevationDouble));
//          String elevationDeltaString = new DecimalFormat("#.#").format(Math.abs(elevationDelta));
//          if (elevationDelta >= 0.0) {
//              ElevationText = "Your Elevation: " + elevationDeltaString + "m above water (" + elevationString + "m)";
              ElevationText = "Your Elevation: " + elevationString + "m";
//          }
//          else {
////        	  ElevationText = "Your Elevation: " + elevationDeltaString + "m above water (" + elevationString + "m)";
//        	  ElevationText = "Your Elevation: " + elevationString + "m)";
//          }
        }
        ElevationTextView.setText(ElevationText);
    }
        
    public void setSelectedLine(ATKPointView marker) {
        selectedLine = marker;
    }

    public ATKPointView getSelectedLine() {
        return selectedLine;
    }
    
	public void setColoring(boolean coloring) {
		MainActivity.coloring = coloring;
	}
	
	public boolean getColoring() {
		return MainActivity.coloring;
	}

	public void setTransparency(boolean transparency) {
		MainActivity.transparency = transparency;
	}
	
	public boolean getTransparency() {
		return MainActivity.transparency;
	}
	
    /**
     * Set alpha/transparency of map DEM overlay
     * @param a Alpha to set
     */
    public void setAlpha(float alpha) {
        MainActivity.alpha = alpha;
    }

    /**
     * Gets the alpha/transparency value of map DEM overlay
     * @return Alpha of DEM overlay
     */
    public float getAlpha() {
        return alpha;
    }
    
	@Override
	public void onDemDataLoad() {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		demDataWrapper = new DemDataWrapper(this, bitmap, new LatLng(0.0, 0.0), new LatLng(0.0, 0.0), 0.0, 0.0);
        setAlpha(0.5f);
        setTransparency(prefs.getBoolean("transparency_bool", true));
        setColoring(prefs.getBoolean("coloring", false));
		configSeekbar();
		demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
		CustomMarker.setDemData(demDataWrapper);
        float[] minMax = demDataWrapper.findSliderMinMax(this);
        updateSliderMin(minMax[0]);
        updateSliderMax(minMax[1]);

        //if user is outside of the DEM area, turn off location tracking
        if(!demDataWrapper.getDemData().getBounds().contains(userMarker.getAtkPoint().position)) {
            drag_mode = true;
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("drag_mode", drag_mode);
            edit.commit();
            LatLng userLocation = demDataWrapper.getDemData().getCenter();
            userMarker.setPosition(userLocation);
            userMarker.setDraggable(true);
            userMarker.setSuperDraggable(true);
        }

        if (following) {
        	setWaterLevel(demDataWrapper.getDemData().getElevationFromLatLng(demDataWrapper.getDemData().getCenter()), demDataWrapper);
        }
        demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
        updateSliderUserText();
        updateSliderWaterPlaneText();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(getDemData().getBounds(), 50));
	}
    
    /**
     * Handles user clicks on the map.
     * Adds markers and lines.
     * @param point Location where click occurred
     */
	@Override
	public void onMapClick (LatLng point) {
        iv.setVisibility(View.GONE);
        atkMapFragment.getView().setVisibility(View.VISIBLE);
        profile = false;
        //handle adding a line
        if(mode == LINE_MODE) {
            if(demDataWrapper.getDemData().getBounds().contains(point)) {
        		// Currently, MapLines may have only 2 vertices!
            	if (mapLines.isEmpty() || mapLines.get(mapLines.size()-1).getVertices().size() == 2) {
            		mapLines.add(new MapLine(map, demDataWrapper, point, atkMapFragment.getView().getWidth(), atkMapFragment.getView().getHeight(), this, this));
//            		mapLines.add(new MapLine(map, demDataWrapper, point this, this));
            	} else {
                	mapLines.get(mapLines.size()-1).addPoint(point);
                    showElevationControls();
                    mode = 0;
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
				CustomMarker.setWaterElevation(waterLevel);
				
				ATKPoint atkPoint = new ATKPoint("custom", point);
				ATKPointView pointView = map.addPoint(atkPoint);
				pointView.setAnchor(0.5f, 1.0f);
				pointView.setDraggable(true);
				pointView.setTitle("true");
				pointView.update();
				CustomMarker marker = new CustomMarker(pointView);
				marker.updateMarker();
				markers.add(marker);
				//updateMarkers();
				mode = 0;
                Toast toast = Toast.makeText(this, "Press and hold on the marker to move it.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
				break;
				
			default:
                showNormalAB();
                buttonDeleteLine.setVisibility(View.GONE);
                buttonShowProfile.setVisibility(View.GONE);
                if(mode != LINE_MODE) {
                    showElevationControls();
                }
                setSelectedLine(null);
				break;
		}
    }
	
	@Override
	public boolean onPointClick(ATKPointView pointView) {
		if(pointView == null || pointView.getTitle() == null) {
            return true;
        }
        //if this is a marker showing the min/max of a line, use default behavior
        //which is to toggle visibility of info window
        if(pointView.getTitle().contains("Min") | pointView.getTitle().contains("Max")) {
            return false;
        }

        //if this is a marker that identifies a line, set up appropriate UI        
        if(pointView.getTitle().equals("Line")) {
            MainActivity.buttonDeleteLine.setVisibility(View.VISIBLE);
            MainActivity.hideElevationControls();
            setSelectedLine(pointView);
            MainActivity.buttonShowProfile.setVisibility(View.VISIBLE);
        }
        
        if (pointView.getTitle().equals("true") | (pointView.getTitle().equals("false"))) {
        	Iterator<CustomMarker> i = markers.iterator();
            CustomMarker marker;

            while (i.hasNext()) {
                 marker = i.next();
                 if (marker.getAtkPointView().equals(pointView)) {
                	 marker.getAtkPointView().hideTitle(); 
                 }
            }
            CustomMarker.setSelected(pointView);
            
            //if the marker text is visible
            if (pointView.getTitle().equals("true")) {
                MainActivity.showMarkerAB();    
            }
            //if the marker text is hidden
            if (pointView.getTitle().equals("false")) {
                MainActivity.showHiddenMarkerAB();
            }
        }

        MainActivity.updateMarkers();
        return true;
	}

	@Override
	public boolean onPointDrag(ATKPointView pointView) {
		//this is for the user location marker
        if(pointView.getAtkPoint().id.equals("User")) {
            userMarker.getAtkPoint().position = pointView.getAtkPoint().position;
            updateSliderUserText();
            MainActivity.updateMarkers();
        }
        //for when 'location following' mode is set
        if (MainActivity.following) {
            MainActivity.setWaterLevel(demDataWrapper.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position), demDataWrapper);
        }
    	double elevationDouble = demDataWrapper.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position);
        CustomMarker.setUserElevation(elevationDouble);
        return false;
	}

	@Override
	public boolean onPointDragEnd(ATKPointView pointView) {
		MainActivity.updateMarkers();
		return false;
	}

	@Override
	public boolean onPointDragStart(ATKPointView pointView) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onPolygonClick(ATKPolygonView polygonView) {
		if (((String)polygonView.getAtkPolygon().id).equals(demLoadUtils.getLoadedDemData().getFilePath())) {
			return false;
		} else {
			if (demLoadUtils.getLoadedDemData().getGroundOverlay() != null) {
				demLoadUtils.getLoadedDemData().getGroundOverlay().remove();
			}
			demLoadUtils.loadClickedDem(this, (String) polygonView.getAtkPolygon().id);
		}
		return false;
	}

	@Override
	public DemLoadUtils getDemLoadUtils() {
		return demLoadUtils;
	}

	@Override
	public void setDemLoadUtils(DemLoadUtils demLoadUtils) {
		this.demLoadUtils = demLoadUtils;
	}

	@Override
	public DemData getDemData() {
		return this.demLoadUtils.getLoadedDemData();
	}
}