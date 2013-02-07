package com.precisionag.waterplane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MainActivity extends Activity {
GroundOverlay prevoverlay;

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
		map.getUiSettings().setMyLocationButtonEnabled(true);
		LatLngBounds bounds = readBounds();
		prevoverlay = createOverlay(bitmap, bounds);
		configSeekbar(bitmap, bounds, prevoverlay);
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
	    switch (item.getItemId()) {
	        case R.id.item_legal:
	        	//this bit randomly stopped working
	        	
	            //DialogFragment newFragment = new LegalNoticeDialogFragment();
	        	//newFragment.show(getFragmentManager(), "legal");
	            //return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
//takes a bitmap, latitude/longitude bounds, and a map to create a map overlay
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
public void updateColors(Bitmap bitmap, LatLngBounds bounds) {
	//get level from seekbar
	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	int waterLevel = seekBar.getProgress();
	seekBar.setMax(255);
	
	int width = bitmap.getWidth();
	int height = bitmap.getHeight();
	int[] pixels = new int[width * height];
	bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
	bitmap = bitmap.copy(bitmap.getConfig(), true);
	
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
	prevoverlay = createOverlay(bitmap, bounds);

}

private void configSeekbar(final Bitmap bitmap, final LatLngBounds bounds, final GroundOverlay overlay) {
	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	seekBar.setMax(255);
	seekBar.setProgress(seekBar.getMax()/2);
	seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
		@Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        	updateColors(bitmap, bounds);
        }
	});
}

private LatLngBounds readBounds() {
	try {
		
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
	    dataIO.close();
	    
	    return new LatLngBounds(northEast, southWest);
	
	}
	catch  (IOException e) {
		return new LatLngBounds(new LatLng(0.0, 0.0), new LatLng(0.0, 0.0));
	}


}

}

