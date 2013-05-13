package com.precisionag.waterplane;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

public class CustomMarker {
	private static GoogleMap map;
	private static Field field;
	private static Activity activity;
	private static double userElevation;
	private static double waterElevation;
	private LatLng location;
	Button button;
	CheckBox checkBox;
	static Context context;
	static RelativeLayout layout;
	static int displayWidth;
	static Button selected;
	
	public CustomMarker(LatLng point) {
		location = point;
		double elevationDouble = field.elevationFromLatLng(point);
		String title;
		String userDelta;
		String waterDelta;
		
		if (elevationDouble == 0.0) {
			title = "Not in field!";
			userDelta = "";
			waterDelta = "";
		}
		else {
			String elevation = new DecimalFormat("000.0").format(elevationDouble);
			String temp = new DecimalFormat("000.0").format(Math.abs(userElevation-elevationDouble));
			title = "Elevation: " + elevation + "m";
			if (userElevation-elevationDouble < 0.0) {
				userDelta = temp+"m above you";
			} else {
				userDelta = temp+"m below you";
			}
			
			temp = new DecimalFormat("000.0").format(Math.abs(waterElevation-elevationDouble));
			if (waterElevation-elevationDouble < 0.0) {
				waterDelta = temp+"m above water";
			} else {
				waterDelta = temp+"m below water";
			}
		}
		
		Projection projection = map.getProjection();
		
		Point screenLocation = projection.toScreenLocation(point);

		button = new Button(context);
		//button.setBackgroundColor(Color.WHITE);
		//button.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.box));
		button.setText(userDelta+"\n"+waterDelta);
		
		float textSize = button.getTextSize();
		int buttonWidth = (int)(textSize * userDelta.length() / 1.5);
		int buttonHeight = (int)textSize * 4;
			

		MarginLayoutParams marginParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//left top right bottom
		marginParams.setMargins(screenLocation.x-(button.getWidth()/2), screenLocation.y-button.getHeight(), 0, 0);
		
	    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
	    button.setLayoutParams(layoutParams);
		layout.addView(button);
		button.setBackgroundColor(Color.WHITE);
		button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (selected != null) {
            		selected.setBackgroundColor(Color.WHITE);
            	}
                selected = button;
                button.setBackgroundColor(Color.YELLOW);
            }
        });
		
		checkBox = new CheckBox(context);
		checkBox.setButtonDrawable(context.getResources().getDrawable(R.drawable.location_place));
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	layout.removeView(button);
            	if (isChecked) {
            		layout.addView(button);
            	}
            }
          });
		MarginLayoutParams checkBoxParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		checkBoxParams.setMargins(screenLocation.x, screenLocation.y, 0, 0);
		layoutParams = new RelativeLayout.LayoutParams(checkBoxParams);
		checkBox.setLayoutParams(layoutParams);
		layout.addView(checkBox);
		checkBox.setChecked(true);
	}
	
	public void updateMarker(double density) {	
		double elevationDouble = field.elevationFromLatLng(location);
		String title;
		String userDelta;
		String waterDelta;
		
		button.setBackgroundColor(button == selected ? Color.YELLOW : Color.WHITE);
		
		if (elevationDouble == 0.0) {
			title = "Not in field!";
			userDelta = "";
			waterDelta = "";
		}
		else {
			String elevation = new DecimalFormat("000.0").format(elevationDouble);
			String temp = new DecimalFormat("000.0").format(elevationDouble-userElevation);
			title = "Elevation: " + elevation + "m";
			userDelta = temp+"m from you";
			
			temp = new DecimalFormat("000.0").format(elevationDouble-waterElevation);
			waterDelta = temp+"m from water";
		}
		
		button.setText(userDelta+"\n"+waterDelta);
		
		Projection projection = map.getProjection();
		Point screenLocation = projection.toScreenLocation(location); 

		MarginLayoutParams marginParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//left top right bottom
		marginParams.setMargins(screenLocation.x-(button.getWidth()/2), screenLocation.y-button.getHeight(), 0, 0);
		
	    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
	    //layoutParams.setWidth();
	    // LayoutParams.WRAP_CONTENT;
	    button.setLayoutParams(layoutParams);
	    layout.removeView(button);
	    if (checkBox.isChecked()) {
	    	layout.addView(button);
	    }
		MarginLayoutParams checkBoxParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		checkBoxParams.setMargins(screenLocation.x, screenLocation.y, 0, 0);
		layoutParams = new RelativeLayout.LayoutParams(checkBoxParams);
		checkBox.setLayoutParams(layoutParams);
		layout.removeView(checkBox);
		layout.addView(checkBox);
	}
	
	public static void setDisplayWidth(int newWidth) {
		displayWidth = newWidth;
	}
	public void removeMarker() {
		button.setVisibility(View.GONE);
		button = null;
		checkBox.setVisibility(View.GONE);
		checkBox = null;
	}
	
	public static void setMap(GoogleMap newMap) {
		map = newMap;
	}
	
	public static void setLayout(RelativeLayout newLayout) {
		layout = newLayout;
	}
	
	public static void setContext(Context newContext) {
		context = newContext;
	}
	
	public static void setField(Field newField) {
		field = newField;
	}
	
	public static void setUserElevation(double elevation) {
		userElevation = elevation;
	}
	
	public static void setWaterElevation(double elevation) {
		waterElevation = elevation;
	}
	
	public static void setActivity(Activity mActivity) {
		activity = mActivity;
	}
	
	public boolean inBounds(LatLng point) {
		//return overlay.getBounds().contains(point);
		return true;
	}
}
