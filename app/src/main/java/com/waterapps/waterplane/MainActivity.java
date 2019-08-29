package com.waterapps.waterplane;

import android.Manifest;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.filebrowser.DataFileChooser;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.waterapps.lib.DataDownload.DemInProgress;
import org.waterapps.lib.DataDownload.DownloadDem;
import org.waterapps.lib.DataDownload.GeoUtils;
import org.waterapps.lib.DemData;
import org.waterapps.lib.DemLoadUtils;
import org.waterapps.lib.WmacListener;

public class MainActivity extends FragmentActivity implements ATKSupportMapFragment.onMapReadyListener, ATKMapClickListener, WmacListener, ATKPointDragListener, ATKPointClickListener, ATKPolygonClickListener {

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;
    private static final int MY_PERMISSION_READ_EXTERNAL_STORAGE = 11;
    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE = 11;

    private static final int ADD_MODE = 1;
    static Button DlButton = null;
    static Button DlCancelButton = null;
    static TextView ElevationTextView = null;
    private static final int FILE_CHOOSER = 6503;
    private static final int FILE_PATH_CHOOSER = 6504;
    private static final int FIRST_START = 42;
    private static final int LINE_MODE = 57832;
    private static final int MAX_PIXELS_PER_MB = 1200;
    private static final float START_LAT = 40.42871f;
    private static final float START_LNG = -86.91382f;
    private static final float START_ZOOM = 17.0f;
    static ActionBar actionBar;
    static float alpha;
    static TextView appName;
    static Button buttonDeleteLine;
    static Button buttonShowProfile;
    public static boolean coloring;
    private static Context context;
    static int currentDemDownloads = 0;
    static boolean currentlyDrawing;
    static LinearLayout dlControls;
    public static boolean downloading;
    static boolean drag_mode = false;
    static TextView editMax;
    static TextView editMin;
    static LinearLayout elevationControls;
    static boolean following;
    public static boolean hasGPS;
    static Button hideButton;

    /* renamed from: iv */
    public static ImageView f64iv;
    public static ATKMap map;
    static boolean markerAB;
    static LinearLayout markerBottomText;
    /* access modifiers changed from: private */
    public static List<ATKPointView> markers;
    private static int mode;
    public static SharedPreferences prefs;
    public static boolean profile;
    static Queue<DemInProgress> progress = new LinkedList();
    static BroadcastReceiver receiver;
    static Resources resources;
    static SeekBar seekBar;
    static Button showButton;
    public static float sliderMax;
    public static float sliderMin;
    public static boolean transparency;
    private static double userElevation;
    /* access modifiers changed from: private */
    public static ATKPointView userMarker;
    private static double waterElevation;
    static TextView waterElevationTextView;
    public static double waterLevel;
    float aspect = 1.0f;
    /* access modifiers changed from: private */
    public ATKSupportMapFragment atkMapFragment;
    public Button buttonDelete;
    /* access modifiers changed from: private */
    public DemDataWrapper demDataWrapper;
    int demDownloadCount = 1;
    int demFinishedCount = 1;
    private DemLoadUtils demLoadUtils;
    private float density;
    LatLng dlCenter;
    float dlWidth;
    private boolean firstStart;
    ATKPolygonView gdlArea;
    private LocationManager locationManager;
    ArrayList<MapLine> mapLines;
    private UiSettings mapSettings;
    OnSharedPreferenceChangeListener onSharedPrefChangeListener;
    private int pointIdCounter = 0;

    /* renamed from: s */
    float f65s;
    ATKPointView selectedLine;
    /* access modifiers changed from: private */
    public ATKPointView selectedMarker;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context2, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context2, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context2, AttributeSet attributeSet) {
        return super.onCreateView(str, context2, attributeSet);
    }

    public static Context getContext() {
        return context;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.atkMapFragment = (ATKSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (savedInstanceState == null) {
            Log.w("AAA", "aaaa");
            this.atkMapFragment.setRetainInstance(true);
        } else {
            Log.w("AAA", "bbbb");
            map = this.atkMapFragment.getAtkMap();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ATKSupportMapFragment) {
            ATKSupportMapFragment atkMapFrag = (ATKSupportMapFragment) fragment;
            atkMapFrag.setOnMapReadyListener(this);
        }
    }

    @Override
    public void onMapReadyNow(ATKSupportMapFragment atkSmp) {
        if (atkSmp instanceof ATKSupportMapFragment) {
            Log.e("setup thingy?", "onmapreay");
            setUpMapIfNeeded();
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            OnSharedPreferenceChangeListener r0 = new OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
                    if (key.equals("pref_max_elevation")) {
                        MainActivity.this.updateSliderMax(Float.parseFloat(sharedPrefs.getString("pref_max_elevation", "300.0")));
                    } else if (key.equals("pref_min_elevation")) {
                        MainActivity.this.updateSliderMin(Float.parseFloat(sharedPrefs.getString("pref_min_elevation", "100.0")));
                    }
                }
            };
            this.onSharedPrefChangeListener = r0;
            prefs.registerOnSharedPreferenceChangeListener(this.onSharedPrefChangeListener);
            context = this;

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSION_READ_EXTERNAL_STORAGE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // Permission has already been granted
                DemLoadUtils demLoadUtils2 = new DemLoadUtils(this, map, prefs, MY_PERMISSION_WRITE_EXTERNAL_STORAGE);
                this.demLoadUtils = demLoadUtils2;
                this.demLoadUtils.registerWmacListener(this);
            }

            downloading = false;
            profile = false;
            this.mapLines = new ArrayList();
            resources = getResources();
            markerAB = false;
            this.density = getResources().getDisplayMetrics().density;
            sliderMin = 241.94f;
            sliderMax = 250.925f;
            sliderMin = Float.parseFloat(prefs.getString("pref_min_elevation", "100.0"));
            sliderMax = Float.parseFloat(prefs.getString("pref_max_elevation", "300.0"));
            currentlyDrawing = false;
            editMin = (TextView) findViewById(R.id.editMin);
            editMax = (TextView) findViewById(R.id.editMax);
            elevationControls = (LinearLayout) findViewById(R.id.elevationControls);
            markerBottomText = (LinearLayout) findViewById(R.id.markerControls);
            dlControls = (LinearLayout) findViewById(R.id.dlControls);
            actionBar = getActionBar();
            f64iv = (ImageView) findViewById(R.id.graphView);
            ImageView imageView = f64iv;
            OnClickListener r02 = new OnClickListener() {
                public void onClick(View v) {
                    MainActivity.f64iv.setVisibility(8);
                    MainActivity.this.atkMapFragment.getView().setVisibility(0);
                    MainActivity.showElevationControls();
                    MainActivity.profile = false;
                }
            };
            imageView.setOnClickListener(r02);
            drag_mode = prefs.getBoolean("drag_mode", false);
            getActionBar().setCustomView(R.layout.custom_ab);
            actionBar.setDisplayShowCustomEnabled(true);
            map.getUiSettings();
            waterElevationTextView = (TextView) findViewById(R.id.text);
            ElevationTextView = (TextView) findViewById(R.id.text2);
            userMarker = map.addPoint(new ATKPoint("User", map.getCameraPosition().target));
            userMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.person), 100, 200);
            seekBar = (SeekBar) findViewById(R.id.seekBar);
            seekBar.setMax(255);
            seekBar.setProgress(128);
            markers = new ArrayList();
            mode = 0;
            this.locationManager = (LocationManager) getSystemService("location");
            hasGPS = false;
            for (String contains : this.locationManager.getAllProviders()) {
                if (contains.contains("gps")) {
                    hasGPS = true;
                }
            }
            hideButton = (Button) findViewById(R.id.buttonHideMarker);
            showButton = (Button) findViewById(R.id.buttonShowMarker);
            this.buttonDelete = (Button) findViewById(R.id.buttonDeleteMarker);
            buttonDeleteLine = (Button) findViewById(R.id.buttonDeleteLine);
            buttonShowProfile = (Button) findViewById(R.id.buttonShowProfile);
            DlButton = (Button) findViewById(R.id.dlButton);
            DlCancelButton = (Button) findViewById(R.id.dlCancelButton);
            Button buttonPlus = (Button) findViewById(R.id.buttonPlus);
            Button buttonMinus = (Button) findViewById(R.id.buttonMinus);
            OnClickListener r03 = new OnClickListener() {
                public void onClick(View v) {
                    SeekBar seekBar = (SeekBar) MainActivity.this.findViewById(R.id.seekBar);
                    seekBar.setProgress(seekBar.getProgress() + 2);
                    MainActivity.this.demDataWrapper.updateColors(MainActivity.waterLevel, MainActivity.coloring, MainActivity.transparency, MainActivity.alpha);
                }
            };
            buttonPlus.setOnClickListener(r03);
            OnClickListener r04 = new OnClickListener() {
                public void onClick(View v) {
                    SeekBar seekBar = (SeekBar) MainActivity.this.findViewById(R.id.seekBar);
                    seekBar.setProgress(seekBar.getProgress() - 2);
                    MainActivity.this.demDataWrapper.updateColors(MainActivity.waterLevel, MainActivity.coloring, MainActivity.transparency, MainActivity.alpha);
                }
            };
            buttonMinus.setOnClickListener(r04);
            Button button = DlButton;
            OnClickListener r05 = new OnClickListener() {
                public void onClick(View v) {
                    MainActivity.hideDlControls();
                    MainActivity.showElevationControls();
                    MainActivity.this.onDownloadAreaSelected();
                }
            };
            button.setOnClickListener(r05);
            Button button2 = DlCancelButton;
            OnClickListener r06 = new OnClickListener() {
                public void onClick(View v) {
                    MainActivity.hideDlControls();
                    MainActivity.showElevationControls();
                    MainActivity.this.gdlArea.remove();
                }
            };
            button2.setOnClickListener(r06);
            appName = (TextView) findViewById(R.id.appName);
            Button button3 = hideButton;
            OnClickListener r07 = new OnClickListener() {
                public void onClick(View v) {
                    Iterator<ATKPointView> i = MainActivity.markers.iterator();
                    while (true) {
                        if (i.hasNext()) {
                            ATKPointView marker = (ATKPointView) i.next();
                            if (MainActivity.this.selectedMarker.equals(marker)) {
                                Log.w("Set", "111");
                                marker.setTitle("hidden");
                                marker.setIcon(MainActivity.this.updateMarkerTextIcon(marker, Boolean.valueOf(marker.equals(MainActivity.this.selectedMarker))), 100, 200);
                                marker.update();
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    MainActivity.this.showHiddenMarkerAB();
                }
            };
            button3.setOnClickListener(r07);
            hideButton.setVisibility(8);
            Button button4 = showButton;
            OnClickListener r08 = new OnClickListener() {
                public void onClick(View v) {
                    Iterator<ATKPointView> i = MainActivity.markers.iterator();
                    while (true) {
                        if (i.hasNext()) {
                            ATKPointView marker = (ATKPointView) i.next();
                            if (MainActivity.this.selectedMarker.equals(marker)) {
                                marker.setTitle("visible");
                                marker.setIcon(MainActivity.this.updateMarkerTextIcon(marker, Boolean.valueOf(marker.equals(MainActivity.this.selectedMarker))), 100, 200);
                                marker.update();
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    MainActivity.this.showMarkerAB();
                }
            };
            button4.setOnClickListener(r08);
            showButton.setVisibility(8);
            Button button5 = this.buttonDelete;
            OnClickListener r09 = new OnClickListener() {
                public void onClick(View v) {
                    Iterator<ATKPointView> i = MainActivity.markers.iterator();
                    while (true) {
                        if (i.hasNext()) {
                            ATKPointView marker = (ATKPointView) i.next();
                            if (MainActivity.this.selectedMarker.equals(marker)) {
                                MainActivity.this.deleteMarker(marker);
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    MainActivity.this.showNormalAB();
                }
            };
            button5.setOnClickListener(r09);
            this.buttonDelete.setVisibility(8);
            Button button6 = buttonDeleteLine;
            OnClickListener r010 = new OnClickListener() {
                public void onClick(View v) {
                    MainActivity.buttonDeleteLine.setVisibility(8);
                    MainActivity.buttonShowProfile.setVisibility(8);
                    MainActivity.showElevationControls();
                    for (int i = MainActivity.this.mapLines.size() - 1; i > -1; i--) {
                        if (((MapLine) MainActivity.this.mapLines.get(i)).getMarker().equals(MainActivity.this.getSelectedLine())) {
                            ((MapLine) MainActivity.this.mapLines.get(i)).remove();
                            MainActivity.this.mapLines.remove(MainActivity.this.mapLines.get(i));
                        }
                    }
                }
            };
            button6.setOnClickListener(r010);
            Button button7 = buttonShowProfile;
            OnClickListener r011 = new OnClickListener() {
                public void onClick(View v) {
                    MainActivity.buttonShowProfile.setVisibility(8);
                    MainActivity.buttonDeleteLine.setVisibility(8);
                    MainActivity.this.hideElevationControls();
                    MainActivity.f64iv.setVisibility(0);
                    Toast.makeText(MainActivity.getContext(), "Tap to dismiss.", 0).show();
                    MainActivity.profile = true;
                    Iterator<MapLine> iter = MainActivity.this.mapLines.iterator();
                    while (iter.hasNext()) {
                        MapLine l = (MapLine) iter.next();
                        if (l.getMarker().equals(MainActivity.this.getSelectedLine())) {
                            l.drawProfile(MainActivity.userMarker.getAtkPoint().position);
                        }
                    }
                }
            };
            button7.setOnClickListener(r011);
            SeekBar seekBar2 = (SeekBar) findViewById(R.id.sizeBar);
            seekBar2.setMax(100);
            seekBar2.setProgress(100);
            OnSeekBarChangeListener r012 = new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    MainActivity.this.dlWidth = (MainActivity.this.f65s * ((float) progress)) / 100.0f;
                    LatLngBounds a = GeoUtils.makeRectangle(MainActivity.this.dlCenter, MainActivity.this.dlWidth, MainActivity.this.aspect);
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.clear();
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.add(a.northeast);
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.add(new LatLng(a.northeast.latitude, a.southwest.longitude));
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.add(a.southwest);
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.add(new LatLng(a.southwest.latitude, a.northeast.longitude));
                    MainActivity.this.gdlArea.update();
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };
            seekBar2.setOnSeekBarChangeListener(r012);
            SeekBar aspectBar = (SeekBar) findViewById(R.id.aspectBar);
            aspectBar.setMax(100);
            aspectBar.setProgress(49);
            OnSeekBarChangeListener r013 = new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    MainActivity.this.aspect = ((float) (progress + 1)) / 50.0f;
                    LatLngBounds a = GeoUtils.makeRectangle(MainActivity.this.dlCenter, MainActivity.this.dlWidth, MainActivity.this.aspect);
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.clear();
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.add(a.northeast);
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.add(new LatLng(a.northeast.latitude, a.southwest.longitude));
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.add(a.southwest);
                    MainActivity.this.gdlArea.getAtkPolygon().boundary.add(new LatLng(a.southwest.latitude, a.northeast.longitude));
                    MainActivity.this.gdlArea.update();
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };
            aspectBar.setOnSeekBarChangeListener(r013);
            if (hasGPS && !this.locationManager.isProviderEnabled("gps") && !drag_mode) {
                drag_mode = true;
                Builder alertDialogBuilder = new Builder(getContext());
                alertDialogBuilder.setTitle("GPS is not enabled");
                DialogInterface.OnClickListener r014 = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                };
                DialogInterface.OnClickListener r015 = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                    }
                };
                alertDialogBuilder.setMessage("Please enable GPS if you want to use the location tracking feature").setCancelable(false).setPositiveButton("Continue anyway", r014).setNegativeButton("GPS Settings", r015);
                alertDialogBuilder.create().show();
            }
            this.firstStart = prefs.getBoolean("first_start", true);
            if (this.firstStart) {
                startActivityForResult(new Intent(this, IntroActivity.class), 42);
                Editor edit = prefs.edit();
                Editor edit2 = prefs.edit();
                edit2.putBoolean("first_start", false);
                edit2.commit();
            }
            context.getSystemService("activity");
            if (!this.firstStart) {
                this.demLoadUtils.loadInitialDem(this);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w("TEST","123");

    }

    private void setUpMapIfNeeded() {
        Log.e("setupmapif", "11111");
        if (map == null) {
            map = ((ATKSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getAtkMap();
            Log.w("bob", Boolean.toString(map == null));
        }
        if (!this.atkMapFragment.getRetained()) {
            setUpMap();
            SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng((double) Float.valueOf(prefs2.getFloat("StartupLat", START_LAT)).floatValue(), (double) Float.valueOf(prefs2.getFloat("StartupLng", START_LNG)).floatValue()), Float.valueOf(prefs2.getFloat("StartupZoom", START_ZOOM)).floatValue()));
        }
        Log.e("setupmapif", "222222");
        map.setOnPointClickListener(this);
        map.setOnPointDragListener(this);
        map.setOnPolygonClickListener(this);
        map.setOnMapClickListener(this);
    }

    private void setUpMap() {
        this.mapSettings = map.getUiSettings();
        this.mapSettings.setZoomControlsEnabled(false);
        this.mapSettings.setMyLocationButtonEnabled(false);
        this.mapSettings.setTiltGesturesEnabled(false);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            map.setMyLocationEnabled(true);
        }

        map.setMapType(4);
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        if (this.demDataWrapper != null) {
            this.demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (markerAB) {
            getMenuInflater().inflate(R.menu.marker_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            menu.findItem(R.id.menu_coloring).setChecked(coloring);
            menu.findItem(R.id.menu_transparency).setChecked(transparency);
            menu.findItem(R.id.menu_drag).setChecked(!drag_mode);
        }
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean z;
        menu.findItem(R.id.menu_coloring).setChecked(coloring);
        menu.findItem(R.id.menu_transparency).setChecked(transparency);
        MenuItem findItem = menu.findItem(R.id.menu_drag);
        if (drag_mode) {
            z = false;
        } else {
            z = true;
        }
        findItem.setChecked(z);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean z = false;
        if (item.getItemId() == R.id.menu_add) {
            mode = 1;
            Toast toast = Toast.makeText(this, "Tap on the map to place the marker.", 1);
            toast.setGravity(17, 0, 0);
            toast.show();
            return true;
        } else if (item.getItemId() == R.id.menu_drag) {
            if (!drag_mode) {
                drag_mode = true;
                userMarker.setPosition(map.getCameraPosition().target);
                userMarker.update();
                Toast toast2 = Toast.makeText(this, "Press and hold the location marker to drag.", 1);
                toast2.setGravity(17, 0, 100);
                toast2.show();
            } else if (this.locationManager.isProviderEnabled("gps")) {
                drag_mode = false;
                Editor edit = prefs.edit();
                edit.putBoolean("drag_mode", drag_mode);
                edit.commit();

                if (ContextCompat.checkSelfPermission( this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
                {
                    ActivityCompat.requestPermissions(
                            this,
                            new String [] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                            MY_PERMISSION_ACCESS_COARSE_LOCATION
                    );
                    final DemDataWrapper bob = this.demDataWrapper;
                    FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    updateSliderUserText();
                                    userElevation = bob.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position);
                                    userMarker.setPosition(userLocation);
                                    userMarker.update();
                                    if (location != null) {
                                        // Logic to handle location object
                                    }
                                }
                            });


                }
            } else {
                Toast toast3 = Toast.makeText(this, "GPS must be enabled to use this feature.", 1);
                toast3.setGravity(17, 0, 100);
                toast3.show();
            }
            userMarker.setDraggable(Boolean.valueOf(drag_mode));
            if (!drag_mode) {
                z = true;
            }
            item.setChecked(z);
            Editor edit2 = prefs.edit();
            edit2.putBoolean("drag_mode", drag_mode);
            edit2.commit();
            return true;
        } else if (item.getItemId() == R.id.menu_center) {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(this.demDataWrapper.getDemData().getBounds(), 50));
            return true;
        } else if (item.getItemId() == R.id.menu_coloring) {
            Editor edit3 = prefs.edit();
            if (coloring) {
                setColoring(false);
            } else {
                setColoring(true);
            }
            edit3.putBoolean("coloring", getColoring());
            edit3.commit();
            item.setChecked(getColoring());
            this.demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
            return true;
        } else if (item.getItemId() == R.id.menu_transparency) {
            Editor edit4 = prefs.edit();
            if (getTransparency()) {
                setTransparency(false);
            } else {
                setTransparency(true);
            }
            edit4.putBoolean("transparency_bool", getTransparency());
            edit4.commit();
            item.setChecked(getTransparency());
            this.demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
            return true;
        } else if (item.getItemId() == R.id.menu_choose_dem) {
            Intent intent = new Intent(this, DataFileChooser.class);
            intent.putExtra("path", getDemLoadUtils().getDemDirectory());
            startActivityForResult(intent, 6503);
            return true;
        } else if (item.getItemId() == R.id.menu_follow) {
            if (!following) {
                z = true;
            }
            following = z;
            item.setChecked(following);
            if (!following) {
                return true;
            }
            setWaterLevel(this.demDataWrapper.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position), this.demDataWrapper);
            return true;
        } else if (item.getItemId() == R.id.menu_settings) {
            Bundle bundle = new Bundle();
            bundle.putString("min_elev", Float.toString((float) getDemData().getMinElevation()));
            bundle.putString("max_elev", Float.toString((float) getDemData().getMaxElevation()));
            Intent intent2 = new Intent(this, SettingsActivity.class);
            intent2.putExtras(bundle);
            startActivity(intent2);
            return true;
        } else if (item.getItemId() == R.id.menu_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        } else if (item.getItemId() == R.id.menu_line) {
            mode = LINE_MODE;
            hideElevationControls();
            Toast.makeText(getContext(), "Tap to add points to the line.", 0).show();
            return true;
        } else if (item.getItemId() != R.id.menu_download) {
            return super.onOptionsItemSelected(item);
        } else {
            if (!downloading) {
                showDlControls();
                hideElevationControls();
                LatLngBounds selectArea = selectArea(map.getProjection().getVisibleRegion().latLngBounds);
                this.dlWidth = this.f65s;
                return true;
            }
            Toast.makeText(this, "Only one DEM download supported at once", 1).show();
            return true;
        }
    }

    static boolean inside(Polygon poly, LatLng l) {
        Iterator<LatLng> iter = poly.getPoints().iterator();
        LatLngBounds bounds = new LatLngBounds((LatLng) iter.next(), (LatLng) iter.next());
        while (iter.hasNext()) {
            bounds = bounds.including((LatLng) iter.next());
        }
        return bounds.contains(l);
    }

    public void setWaterLevel(double level, DemDataWrapper demDataWrapper2) {
        seekBar.setProgress((int) ((255.0d * (level - ((double) sliderMin))) / ((double) (sliderMax - sliderMin))));
        demDataWrapper2.updateColors(level, coloring, transparency, alpha);
    }

    private void configSeekbar() {
        SeekBar seekBar2 = (SeekBar) findViewById(R.id.seekBar);
        seekBar2.setMax(255);
        seekBar2.setProgress(128);
        seekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (MainActivity.userMarker.getAtkPoint().position != null) {
                    MainActivity.this.updateSliderWaterPlaneText();
                    MainActivity.this.updateSliderUserText();
                    if (MainActivity.profile) {
                        Iterator<MapLine> iter = MainActivity.this.mapLines.iterator();
                        while (iter.hasNext()) {
                            MapLine l = (MapLine) iter.next();
                            if (l.getMarker().equals(MainActivity.this.getSelectedLine())) {
                                l.drawProfile(MainActivity.userMarker.getAtkPoint().position);
                            }
                        }
                        return;
                    }
                    MainActivity.this.updateMarkers();
                    MainActivity.this.demDataWrapper.updateColors(MainActivity.waterLevel, MainActivity.coloring, MainActivity.transparency, MainActivity.alpha);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                MainActivity.this.demDataWrapper.updateColors(MainActivity.waterLevel, MainActivity.coloring, MainActivity.transparency, MainActivity.alpha);
                MainActivity.this.updateMarkers();
            }
        });
        seekBar2.setProgress(seekBar2.getMax() / 2);
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42) {
            this.demLoadUtils.loadInitialDem(this);
            return;
        }
        if (requestCode == 6504) {
            SettingsActivity.updateDemFolder();
            this.demLoadUtils.setNewDemDirectory(data.getStringExtra("directory"));
        }
        if (requestCode == 6503 && data != null && data.getData().toString().contains(".tif")) {
            File f = new File(data.getData().getPath());
            if (this.demLoadUtils.getDemDirectory() != f.getParent()) {
                this.demLoadUtils.setNewDemDirectory(f.getParent());
            }
            this.demLoadUtils.loadDem(this, data.getData().getPath());
        }
    }

    public void hideElevationControls() {
        elevationControls.setVisibility(8);
    }

    public static void showElevationControls() {
        elevationControls.setVisibility(0);
    }

    public static void hideMarkerBottomText() {
        markerBottomText.setVisibility(8);
    }

    public static void showMarkerBottomText() {
        markerBottomText.setVisibility(0);
    }

    public static void hideDlControls() {
        dlControls.setVisibility(8);
    }

    public static void showDlControls() {
        dlControls.setVisibility(0);
    }

    public void showMarkerAB() {
        this.buttonDelete.setVisibility(0);
        appName.setVisibility(8);
        hideButton.setVisibility(0);
        showButton.setVisibility(8);
    }

    public void showHiddenMarkerAB() {
        this.buttonDelete.setVisibility(0);
        appName.setVisibility(8);
        hideButton.setVisibility(8);
        showButton.setVisibility(0);
    }

    public void showNormalAB() {
        this.buttonDelete.setVisibility(8);
        appName.setVisibility(0);
        hideButton.setVisibility(8);
        showButton.setVisibility(8);
    }

    public static Resources getResource() {
        return resources;
    }

    public void DownloadDEM(LatLngBounds extent) {
        new DownloadDem(extent, getDemLoadUtils().getDemDirectory(), map, this, this.gdlArea);
    }

    private LatLngBounds selectArea(LatLngBounds screen) {
        LatLng center = GeoUtils.getCenter(screen);
        this.dlCenter = center;
        this.f65s = GeoUtils.getWidth(screen) > GeoUtils.getHeight(screen) ? GeoUtils.getHeight(screen) : GeoUtils.getWidth(screen);
        ActivityManager manager = (ActivityManager) context.getSystemService("activity");
        if ((this.f65s * this.f65s) / 3.0f > ((float) (manager.getLargeMemoryClass() * MAX_PIXELS_PER_MB))) {
            this.f65s = ((float) Math.sqrt((double) (manager.getLargeMemoryClass() * MAX_PIXELS_PER_MB))) * 3.0f;
        }
        LatLngBounds dlArea = GeoUtils.makeRectangle(center, this.f65s, this.aspect);
        List<LatLng> list = new ArrayList<>();
        list.add(dlArea.northeast);
        list.add(new LatLng(dlArea.northeast.latitude, dlArea.southwest.longitude));
        list.add(dlArea.southwest);
        list.add(new LatLng(dlArea.southwest.latitude, dlArea.northeast.longitude));
        ATKPolygon rectangle = new ATKPolygon((Object) "rect", list);
        rectangle.viewOptions.setStrokeColor(-16776961);
        this.gdlArea = map.addPolygon(rectangle);
        this.gdlArea.setFillColor(0);
        return dlArea;
    }

    /* access modifiers changed from: private */
    public void onDownloadAreaSelected() {
        DownloadDEM(GeoUtils.makeRectangle(this.dlCenter, this.dlWidth, this.aspect));
    }

    public int dpToPx(int dp) {
        return (int) ((((float) dp) * this.density) + 0.5f);
    }

    public void updateSliderMin(float min) {
        if (((double) min) < getDemData().getMinElevation()) {
            min = (float) getDemData().getMinElevation();
        }
        DecimalFormat df = new DecimalFormat("#.#");
        editMin.setText(df.format((double) min));
        sliderMin = min;
        Editor edit = prefs.edit();
        edit.putString("pref_min_elevation", df.format((double) sliderMin));
        edit.commit();
    }

    public void updateSliderMax(float max) {
        if (((double) max) > getDemData().getMaxElevation()) {
            max = (float) getDemData().getMaxElevation();
        }
        DecimalFormat df = new DecimalFormat("#.#");
        editMax.setText(df.format((double) max));
        sliderMax = max;
        Editor edit = prefs.edit();
        edit.putString("pref_max_elevation", df.format((double) sliderMax));
        edit.commit();
    }

    public void updateSliderWaterPlaneText() {
        waterLevel = ((double) sliderMin) + ((((double) seekBar.getProgress()) * ((double) (sliderMax - sliderMin))) / 255.0d);
        waterElevationTextView.setText("Water Plane Elevation: " + new DecimalFormat("#.#").format(waterLevel) + " ft");
    }

    public void updateSliderUserText() {
        String ElevationText;
        double elevationDouble = this.demDataWrapper.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position);
        Log.w("Elev", Double.toString(elevationDouble));
        TextView ElevationTextView2 = (TextView) findViewById(R.id.text2);
        if (elevationDouble == 0.0d) {
            ElevationText = "Current location isn't inside the data area.";
        } else {
            ElevationText = "Your Elevation: " + new DecimalFormat("#.#").format(Math.abs(elevationDouble)) + " ft";
        }
        ElevationTextView2.setText(ElevationText);
    }

    public void updateMarkers() {
        Log.w("marker length", Integer.toString(markers.size()));
        int ii = 0;
        for (ATKPointView marker : markers) {
            ii++;
            Log.w("i", Integer.toString(ii));
            marker.setIcon(updateMarkerTextIcon(marker, Boolean.valueOf(marker.equals(this.selectedMarker))), 100, 200);
            marker.update();
        }
    }

    public void deleteMarker(ATKPointView marker) {
        this.selectedMarker = null;
        marker.remove();
        map.removePoint(marker.getAtkPoint());
        markers.remove(marker);
    }

    public BitmapDescriptor updateMarkerTextIcon(ATKPointView pointView, Boolean isSelectedMarker) {
        String userDelta;
        String waterDelta;
        BitmapDescriptor icon;
        try {
            MapsInitializer.initialize(this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (!pointView.isVisible()) {
            return null;
        }
        double elevationDouble = getDemData().getElevationFromLatLng(pointView.getAtkPoint().position);
        if (elevationDouble == 0.0d) {
            userDelta = "available";
            waterDelta = "No elevation data";
        } else {
            String temp = new DecimalFormat("#.#").format(Math.abs(userElevation - elevationDouble));
            if (userElevation - elevationDouble < 0.0d) {
                userDelta = new StringBuilder(String.valueOf(temp)).append("m above you").toString();
            } else {
                userDelta = new StringBuilder(String.valueOf(temp)).append("m below you").toString();
            }
            String temp2 = new DecimalFormat("#.#").format(Math.abs(waterLevel - elevationDouble));
            if (waterLevel - elevationDouble < 0.0d) {
                waterDelta = new StringBuilder(String.valueOf(temp2)).append("m above water").toString();
            } else {
                waterDelta = new StringBuilder(String.valueOf(temp2)).append("m below water").toString();
            }
        }
        Bitmap bitmap = textToBitmap(new StringBuilder(String.valueOf(waterDelta)).append("\n").append(userDelta).toString(), isSelectedMarker.booleanValue());
        if (pointView.getTitle().equals("visible")) {
            icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        } else if (isSelectedMarker.booleanValue()) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.arrow_selected);
        } else {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.arrow);
        }
        try {
            pointView.setIcon(icon, 100, 200);
            pointView.update();
            return icon;
        } catch (IllegalArgumentException e2) {
            System.out.println(e2);
            return icon;
        }
    }

    /* access modifiers changed from: 0000 */
    public Bitmap textToBitmap(String text, boolean isSelected) {
        Bitmap arrow;
        int width = dpToPx(180);
        Bitmap bitmap = Bitmap.createBitmap(width, dpToPx(80), Config.ARGB_8888);
        if (isSelected) {
            arrow = BitmapFactory.decodeResource(getResource(), R.drawable.arrow_selected);
        } else {
            arrow = BitmapFactory.decodeResource(getResource(), R.drawable.arrow);
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
        canvas.drawBitmap(arrow, null, new RectF((float) dpToPx(80), (float) dpToPx(60), (float) dpToPx(100), (float) dpToPx(80)), null);
        canvas.clipRect(0, 0, width, dpToPx(60));
        if (isSelected) {
            canvas.drawARGB(255, 51, 181, 229);
        } else {
            canvas.drawARGB(255, 255, 255, 255);
        }
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(16.0f * this.density);
        StaticLayout sl = new StaticLayout(text, textPaint, bitmap.getWidth() - 8, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.translate((float) ((int) (3.0f * this.density)), (float) ((int) (10.0f * this.density)));
        sl.draw(canvas);
        return bitmap;
    }

    public void setSelectedLine(ATKPointView marker) {
        this.selectedLine = marker;
    }

    public ATKPointView getSelectedLine() {
        return this.selectedLine;
    }

    public void setColoring(boolean coloring2) {
        coloring = coloring2;
    }

    public boolean getColoring() {
        return coloring;
    }

    public void setTransparency(boolean transparency2) {
        transparency = transparency2;
    }

    public boolean getTransparency() {
        return transparency;
    }

    public void setAlpha(float alpha2) {
        alpha = alpha2;
    }

    public float getAlpha() {
        return alpha;
    }

    public void onDemDataLoad() {
        this.demDataWrapper = new DemDataWrapper(this, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), new LatLng(0.0d, 0.0d), new LatLng(0.0d, 0.0d), 0.0d, 0.0d);
        setAlpha(0.5f);
        setTransparency(prefs.getBoolean("transparency_bool", true));
        setColoring(prefs.getBoolean("coloring", false));
        configSeekbar();
        this.demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
        float[] minMax = this.demDataWrapper.findSliderMinMax(this);
        updateSliderMin(minMax[0]);
        updateSliderMax(minMax[1]);
        if (!this.demDataWrapper.getDemData().getBounds().contains(userMarker.getAtkPoint().position)) {
            drag_mode = true;
            Editor edit = prefs.edit();
            edit.putBoolean("drag_mode", drag_mode);
            edit.commit();
            userMarker.setPosition(this.demDataWrapper.getDemData().getCenter());
            userMarker.setDraggable(Boolean.valueOf(true));
            userMarker.setSuperDraggable(true);
            userMarker.update();
            userElevation = this.demDataWrapper.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position);
        }
        if (following) {
            setWaterLevel(this.demDataWrapper.getDemData().getElevationFromLatLng(this.demDataWrapper.getDemData().getCenter()), this.demDataWrapper);
        }
        this.demDataWrapper.updateColors(waterLevel, coloring, transparency, alpha);
        updateSliderUserText();
        updateSliderWaterPlaneText();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(getDemData().getBounds(), 50));
    }

    public void onMapClick(LatLng point) {
        Log.w("MAP CLICKY", "CLICK");
        f64iv.setVisibility(8);
        this.atkMapFragment.getView().setVisibility(0);
        profile = false;
        if (mode == LINE_MODE) {
            if (!this.demDataWrapper.getDemData().getBounds().contains(point)) {
                Toast.makeText(this, "Please place all points within DEM boundaries.", 0).show();
            } else if (this.mapLines.isEmpty() || ((MapLine) this.mapLines.get(this.mapLines.size() - 1)).getVertices().size() == 2) {
                this.mapLines.add(new MapLine(map, this.demDataWrapper, point, this.atkMapFragment.getView().getWidth(), this.atkMapFragment.getView().getHeight(), this, this));
            } else {
                ((MapLine) this.mapLines.get(this.mapLines.size() - 1)).addPoint(point);
                showElevationControls();
                mode = 0;
            }
        }
        switch (mode) {
            case 1:
                Log.e("added to map", "already");
                this.pointIdCounter++;
                ATKPointView pointView = map.addPoint(new ATKPoint("Custom" + String.valueOf(this.pointIdCounter), point));
                Log.e("PointView", Boolean.toString(pointView == null));
                pointView.setAnchor(0.5f, 1.0f);
                pointView.setTitle("visible");
                pointView.hideTitle();
                pointView.setIcon(updateMarkerTextIcon(pointView, Boolean.valueOf(true)), 100, 200);
                pointView.setOnDragListener(this);
                pointView.setOnClickListener(this);
                pointView.setSuperDraggable(true);
                pointView.update();
                markers.add(pointView);
                selectMarker(pointView);
                mode = 0;
                return;
            default:
                selectMarker(null);
                showNormalAB();
                buttonDeleteLine.setVisibility(8);
                buttonShowProfile.setVisibility(8);
                if (mode != LINE_MODE) {
                    showElevationControls();
                }
                setSelectedLine(null);
                return;
        }
    }

    public boolean onPointClick(ATKPointView clickedPointView) {
        if (clickedPointView == null || clickedPointView.getTitle() == null) {
            return true;
        }
        if (clickedPointView.getTitle().contains("Min") || clickedPointView.getTitle().contains("Max")) {
            return false;
        }
        if (clickedPointView.getTitle().equals("Line")) {
            buttonDeleteLine.setVisibility(0);
            hideElevationControls();
            setSelectedLine(clickedPointView);
            buttonShowProfile.setVisibility(0);
        }
        if (clickedPointView.getTitle().equals("visible") || clickedPointView.getTitle().equals("hidden")) {
            selectMarker(clickedPointView);
            if (clickedPointView.getTitle().equals("visible")) {
                showMarkerAB();
            }
            if (clickedPointView.getTitle().equals("hidden")) {
                showHiddenMarkerAB();
            }
        }
        return true;
    }

    public void selectMarker(ATKPointView pointView) {
        if (this.selectedMarker != null) {
            this.selectedMarker.setIcon(updateMarkerTextIcon(this.selectedMarker, Boolean.valueOf(false)), 100, 200);
            this.selectedMarker.update();
        }
        if (pointView != null) {
            pointView.setIcon(updateMarkerTextIcon(pointView, Boolean.valueOf(true)), 100, 200);
            pointView.update();
            this.selectedMarker = pointView;
            if (pointView.getTitle().equals("visible")) {
                showMarkerAB();
            }
            if (pointView.getTitle().equals("hidden")) {
                showHiddenMarkerAB();
            }
        }
    }

    public boolean onPointDrag(ATKPointView pointView) {
        if (pointView.getAtkPoint().id.equals("User")) {
            updateSliderUserText();
            updateMarkers();
            userElevation = this.demDataWrapper.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position);
        }
        if (following) {
            setWaterLevel(this.demDataWrapper.getDemData().getElevationFromLatLng(userMarker.getAtkPoint().position), this.demDataWrapper);
        }
        if (pointView.getAtkPoint().id.toString().contains("Custom")) {
            pointView.setIcon(updateMarkerTextIcon(pointView, Boolean.valueOf(true)), 100, 200);
            pointView.update();
        }
        return false;
    }

    public boolean onPointDragEnd(ATKPointView pointView) {
        return false;
    }

    public boolean onPointDragStart(ATKPointView pointView) {
        if (pointView.getAtkPoint().id.toString().contains("Custom")) {
            selectMarker(pointView);
        }
        return false;
    }

    public boolean onPolygonClick(ATKPolygonView polygonView) {
        if (!((String) polygonView.getAtkPolygon().id).equals(this.demLoadUtils.getLoadedDemData().getFilePath())) {
            if (this.demLoadUtils.getLoadedDemData().getGroundOverlay() != null) {
                this.demLoadUtils.getLoadedDemData().getGroundOverlay().remove();
            }
            this.demLoadUtils.loadClickedDem(this, (String) polygonView.getAtkPolygon().id);
        }
        return false;
    }

    public DemLoadUtils getDemLoadUtils() {
        return this.demLoadUtils;
    }

    public void setDemLoadUtils(DemLoadUtils demLoadUtils2) {
        this.demLoadUtils = demLoadUtils2;
    }

    public DemData getDemData() {
        return this.demLoadUtils.getLoadedDemData();
    }
}
