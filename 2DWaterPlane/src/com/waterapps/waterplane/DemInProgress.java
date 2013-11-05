package com.waterapps.waterplane;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.google.android.gms.internal.dn;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.logging.Handler;

/**
 * Created by Steve on 13/10/30.
 */
public class DemInProgress extends GeoRectangle  {
    GoogleMap map = null;
    Polygon outline = null;
    Polygon fill= null;
    setProgress updater = null;
    public DemInProgress(LatLngBounds extent, GoogleMap map) {
        super(extent);
        this.map = map;
        outline = map.addPolygon(getPolyOptions(Color.BLUE));
        updateProgress(0.0);
        updater = new setProgress();
        updater.execute(Long.parseLong("42"));
    }

    public void cancelUpdate() {
        updater.cancel(true);
    }

    //displays progress by partially filling rectangle
    //progress given as double in 0..1
    public void updateProgress(double progress) {
        //check input
        assert (progress >= 0.0);
        assert (progress <= 1.0);

        //remove previous progress fill if it exists
        if (fill != null) fill.remove();

        //figure out what area to fill
        double top = south + (progress*(north-south));

        PolygonOptions fillOptions = new PolygonOptions()
                .add(getSW())
                .add(getSE())
                .add(new LatLng(top, east))
                .add(new LatLng(top, west))
                .fillColor(Color.argb(128, 0, 0, 128))
                .strokeColor(Color.TRANSPARENT);

        //draw polygon
        fill = map.addPolygon(fillOptions);
    }

    public void remove() {
        updater.cancel(true);
        if (outline != null)
            outline.remove();
        if (fill != null)
            fill.remove();
    }

    private class setProgress extends AsyncTask<Long, Integer, Long> {
        protected Long doInBackground(Long... urls) {
            long totalSize = 0;
            for (int i = 0; i < 200; i++) {
                publishProgress(i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return Long.parseLong("1");
        }

        protected void onProgressUpdate(Integer... progress) {
            updateProgress((double)progress[0]/200.0);
        }

        protected void onPostExecute(Long result) {

        }
    }

}
