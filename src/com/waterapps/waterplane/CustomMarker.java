package com.waterapps.waterplane;

import java.text.DecimalFormat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.openatk.openatklib.atkmap.ATKMap;
import com.openatk.openatklib.atkmap.models.ATKPoint;
import com.openatk.openatklib.atkmap.views.ATKPointView;
import com.waterapps.waterplane.R.drawable;

/**
 * A custom Google Maps marker showing elevation information.
 * It wraps rather than extends the Marker class because Google doesn't allow extensions of it.
 */
public class CustomMarker {
//	private static ATKMap map;
	private static DemDataWrapper demData;
	private static double userElevation;
	private static double waterElevation;
    private ATKPointView marker;
//    private Marker marker;
	private static ATKPointView selected;
	static final int blue = 0xFF33B5E5;
    static float density;
    
    /**
     * Creates a marker at the given location.
     * @param point the location to place the marker
     */
	public CustomMarker(ATKPointView point) {
		this.marker = point;
	}

    /**
     * Sets display density.
     * Can be obtained in Activity from getResources().getDisplayMetrics().density;
     * @param newDensity the density to use
     */
    public static void setDensity(float newDensity) {
        density = newDensity;
    }

    /**
     * Updates the marker graphics.
     */
	public void updateMarker() {
        try {
            MapsInitializer.initialize(MainActivity.getContext());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (marker.isVisible()) {
            LatLng point = marker.getAtkPoint().position;
            double elevationDouble = demData.getDemData().getElevationFromLatLng(point);
            String title;
            String userDelta;
            String waterDelta;

            if (elevationDouble == 0.0) {
                title = "";
                userDelta = "available";
                waterDelta = "No elevation data";
            }
            else {
                String temp = new DecimalFormat("#.#").format(Math.abs(userElevation-elevationDouble));
                title = "";
                if (userElevation-elevationDouble < 0.0) {
                    userDelta = temp+"m above you";
                } else {
                    userDelta = temp+"m below you";
                }

                temp = new DecimalFormat("#.#").format(Math.abs(waterElevation-elevationDouble));
                if (waterElevation-elevationDouble < 0.0) {
                    waterDelta = temp+"m above water";
                } else {
                    waterDelta = temp+"m below water";
                }
            }

            Bitmap bitmap = textToBitmap(waterDelta + "\n" + userDelta, marker.equals(selected));

            BitmapDescriptor icon;

            if (marker != null) {
            	if (marker.getTitle().equals("true")) {
                    icon = BitmapDescriptorFactory.fromBitmap(bitmap);
                }
                else {
                    if (marker.equals(CustomMarker.getSelected())) {
                        icon = BitmapDescriptorFactory.fromResource(drawable.arrow_selected);
                    }
                    else {
                        icon = BitmapDescriptorFactory.fromResource(drawable.arrow);
                    }
                }
                try {
                    marker.setIcon(icon, 100, 200);
                    marker.update();
                } catch (java.lang.IllegalArgumentException e) {
                    System.out.println(e);
                }
            }

        }
	}

    /**
     * Deletes the marker.
     */
	public void removeMarker() {
        setSelected(null);
        MainActivity.deleteMarker(this);
	}

//    /**
//     * Set GoogleMap object to display the markers on.
//     * @param newMap the map
//     */
//	public static void setMap(ATKMap newMap) {
//		map = newMap;
//	}

    /**
     * Set the DemData object.
     * Used to get elevation data.
     * {@link DemDataWrapper}
     * @param newField demData to use
     */
	public static void setDemData(DemDataWrapper newField) {
		demData = newField;
	}

    /**
     * Set user's current elevation.
     * @param elevation user elevation
     */
	public static void setUserElevation(double elevation) {
		userElevation = elevation;
	}

    /**
     * Set water level.
     * @param elevation water level
     */
	public static void setWaterElevation(double elevation) {
		waterElevation = elevation;
	}

    /**
     * Gets the currently selected Marker.
     * May be null.
     * @return the selected marker
     */
	public static ATKPointView getSelected() {
		return selected;
	}

    /**
     * Sets the currently selected Marker.
     * @param marker2 marker to select
     */
	public static void setSelected(ATKPointView selection) {
        selected = selection;
   	}

    /**
     * Gets the Marker object.
     * @return the marker
     */
    public ATKPointView getAtkPointView() {
        return marker;
    }

    /**
     * Get the location of the marker.
     * @return marker location
     */
    public LatLng getLocation() {
        return marker.getAtkPoint().position;
    }

    /**
     * Creates icon bitmap from given text.
     * @param text text to display on marker
     * @param isSelected whether or not this marker is currently selected
     * @return icon to display on the marker
     */
    Bitmap textToBitmap(String text, boolean isSelected) {
        int width = dpToPx(180);
        int height = dpToPx(80);

        Bitmap arrow;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (isSelected) {
             arrow = BitmapFactory.decodeResource(MainActivity.getResource(), drawable.arrow_selected);
        }
        else {
             arrow = BitmapFactory.decodeResource(MainActivity.getResource(), drawable.arrow);
        }
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
        textPaint.setTextSize(16.0f * density);
        StaticLayout sl= new StaticLayout(text, textPaint, bitmap.getWidth()-8, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.translate((int)(3.0f * density), (int)(10.0f * density));
        sl.draw(canvas);
        return bitmap;
    }

    /**
     * Converts dp (display-independent pixels) to physical pixels based on density.
     * @param dp display pixels
     * @return physical pixels
     */
    int dpToPx(int dp) {
        return (int) (dp * density + 0.5f);
    }
}
