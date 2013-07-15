package com.precisionag.lib;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.precisionag.waterplane.MainActivity;
import com.precisionag.waterplane.R;
import com.precisionag.waterplane.R.drawable;

public class CustomMarker {
	private static GoogleMap map;
	private static Field field;
	private static double userElevation;
	private static double waterElevation;
    private Marker marker;
	private LatLng location;
	private Button button;
	CheckBox checkBox;
	static Context context;
	private static RelativeLayout layout;
	static int displayWidth;
	private static Marker selected;
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

        Bitmap bitmap = textToBitmap(waterDelta + "\n" + userDelta, false);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);

        marker = map.addMarker(new MarkerOptions()
                .title("true")
                .position(point)
                .icon(icon)
                .anchor(.5f, 1.0f));
	}
	
	public void updateMarker() {
        LatLng point = marker.getPosition();
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

        Bitmap bitmap = textToBitmap(waterDelta + "\n" + userDelta, marker.equals(selected));

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);

        if (marker.getTitle().equals("true")) {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
	}
	
	public static void setDisplayWidth(int newWidth) {
		displayWidth = newWidth;
	}
	public void removeMarker() {
		marker.remove();
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

	public static Marker getSelected() {
		return selected;
	}

	public static void setSelected(Marker selection) {
        selected = selection;
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

    public Marker getMarker() {
        return marker;
    }

    Bitmap textToBitmap(String text, boolean isSelected) {
        int width = dpToPx(180);
        int height = dpToPx(80);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap arrow = BitmapFactory.decodeResource(MainActivity.resources, drawable.arrow);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawBitmap(arrow, null, new RectF((float)dpToPx(80), (float)dpToPx(60), (float)dpToPx(100), (float)dpToPx(80)), null);
        canvas.clipRect(0, 0, width, dpToPx(60));
        if (isSelected) {
            //selected marker drawn blue
            canvas.drawARGB(255, 51, 181, 229);
        }
        else {
            //else drawn white
            canvas.drawARGB(255, 255, 255, 255);
        }
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(16.0f * MainActivity.scale);
        StaticLayout sl= new StaticLayout(text, textPaint, bitmap.getWidth()-8, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.translate((int)(3.0f * MainActivity.scale), (int)(10.0f * MainActivity.scale));
        sl.draw(canvas);
        return bitmap;
    }

    int dpToPx(int dp) {
        return (int) (dp * MainActivity.scale + 0.5f);
    }
}
