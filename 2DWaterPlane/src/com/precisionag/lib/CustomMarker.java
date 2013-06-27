package com.precisionag.lib;

import java.text.DecimalFormat;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.precisionag.waterplane.MainActivity;
import com.precisionag.waterplane.R;
import com.precisionag.waterplane.R.drawable;

public class CustomMarker {
	private static GoogleMap map;
	private static Field field;
	private static double userElevation;
	private static double waterElevation;
	private LatLng location;
	private Button button;
	CheckBox checkBox;
	static Context context;
	private static RelativeLayout layout;
	static int displayWidth;
	private static Button selected;
	static final int blue = 0xFF33B5E5;
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
			String temp = new DecimalFormat("000.0").format(Math.abs(userElevation-elevationDouble));
			title = "";
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

		setButton(new Button(context));
		//button.setBackgroundColor(Color.WHITE);
		//button.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.box));
		getButton().setText(title+userDelta+"\n"+waterDelta);

		MarginLayoutParams marginParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//left top right bottom
		marginParams.setMargins(screenLocation.x-(getButton().getWidth()/2), screenLocation.y-getButton().getHeight(), 0, 0);
		
	    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
	    getButton().setLayoutParams(layoutParams);
		getLayout().addView(getButton());
		getButton().setBackgroundColor(Color.WHITE);
		getButton().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (getSelected() != null) {
            		getSelected().setBackgroundColor(Color.WHITE);
            	}
                setSelected(getButton());
                getButton().setBackgroundColor(blue);
                MainActivity.hideElevationControls();
                MainActivity.showMarkerBottomText();
                MainActivity.showMarkerAB();
                MainActivity.updateMarkers();
            }
        });
		
		checkBox = new CheckBox(context);
		checkBox.setButtonDrawable(context.getResources().getDrawable(R.drawable.arrow));
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	getLayout().removeView(getButton());
            	if (isChecked) {
            		getLayout().addView(getButton());
            	}
            }
          });
		MarginLayoutParams checkBoxParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		checkBoxParams.setMargins(screenLocation.x-checkBox.getWidth()/2, screenLocation.y, 0, 0);
		layoutParams = new RelativeLayout.LayoutParams(checkBoxParams);
		checkBox.setLayoutParams(layoutParams);
		getLayout().addView(checkBox);
		checkBox.setChecked(true);
	}
	
	public void updateMarker() {	
		double elevationDouble = field.elevationFromLatLng(location);
		String title;
		String userDelta;
		String waterDelta;
		
		getButton().setBackgroundColor(getButton() == getSelected() ? blue : Color.WHITE);
		
		if (elevationDouble == 0.0) {
			title = "Not in field!";
			userDelta = "";
			waterDelta = "";
		}
		else {
			String temp = new DecimalFormat("000.0").format(elevationDouble-userElevation);
			title = "";
			userDelta = temp+"m from you";
			
			temp = new DecimalFormat("000.0").format(elevationDouble-waterElevation);
			waterDelta = temp+"m from water";
		}
		
		getButton().setText(title+userDelta+"\n"+waterDelta);
		
		Projection projection = map.getProjection();
		Point screenLocation = projection.toScreenLocation(location); 

		MarginLayoutParams marginParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//left top right bottom
		if (getButton().getWidth() == 0) {
			marginParams.setMargins(screenLocation.x-(int)(getButton().getTextSize()*waterDelta.length()/4.0), screenLocation.y-(int)(getButton().getTextSize()*3.0), 0, 0);
		} else {
            if (screenLocation.x+(getButton().getWidth()/2) < displayWidth) {
			    marginParams.setMargins(screenLocation.x-(getButton().getWidth()/2), screenLocation.y-getButton().getHeight(), 0, 0);
            }
            else {
                marginParams.setMargins(screenLocation.x+(getButton().getWidth()/2), screenLocation.y-getButton().getHeight(), 0, 0);
            }
		}
		
	    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
	    //layoutParams.setWidth();
	    // LayoutParams.WRAP_CONTENT;
	    getButton().setLayoutParams(layoutParams);
	    getLayout().removeView(getButton());
	    if (checkBox.isChecked()) {
	    	getLayout().addView(getButton());
	    }
		MarginLayoutParams checkBoxParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		checkBoxParams.setMargins(screenLocation.x-checkBox.getWidth()/2, screenLocation.y, 0, 0);
		layoutParams = new RelativeLayout.LayoutParams(checkBoxParams);
		checkBox.setLayoutParams(layoutParams);
		getLayout().removeView(checkBox);
		getLayout().addView(checkBox);
	}
	
	public static void setDisplayWidth(int newWidth) {
		displayWidth = newWidth;
	}
	public void removeMarker() {
		getButton().setVisibility(View.GONE);
		setButton(null);
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
	
	public boolean inBounds(LatLng point) {
		//return overlay.getBounds().contains(point);
		return true;
	}

	public static Button getSelected() {
		return selected;
	}

	public static void setSelected(Button selected) {
		CustomMarker.selected = selected;
	}

	public static RelativeLayout getLayout() {
		return layout;
	}

	public Button getButton() {
		return button;
	}

	public void setButton(Button button) {
		this.button = button;
	}
}
