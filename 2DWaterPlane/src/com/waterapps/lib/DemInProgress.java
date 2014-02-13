package com.waterapps.lib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.waterapps.waterplane.R;

/**
 * Created by Steve on 13/10/30.
 */
public class DemInProgress extends GeoRectangle  {
    GoogleMap map = null;
    Polygon outline = null;
    Polygon fill= null;
    setProgress updater = null;
    Marker mark;
    float density = 3.0f;

    public DemInProgress(LatLngBounds extent, GoogleMap map) {
        super(extent);
        this.map = map;
        outline = map.addPolygon(getPolyOptions(Color.BLUE));
        BitmapDescriptor b = BitmapDescriptorFactory.fromResource(R.drawable.notification);
        b = BitmapDescriptorFactory.fromBitmap(textToBitmap("Downloading..."));
        mark = map.addMarker(new MarkerOptions().icon(b)
                                                .position(this.center())
                                                .anchor(0.5f, 0.5f));
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
        if (mark != null)
            mark.remove();
    }

    private class setProgress extends AsyncTask<Long, Integer, Long> {
        protected Long doInBackground(Long... urls) {
            long totalSize = 0;
            for (int i = 0; i < 100; i++) {
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
            updateProgress((double)progress[0]/150.0);
        }

        protected void onPostExecute(Long result) {

        }
    }

    Bitmap textToBitmap(String text) {
        int width = dpToPx(180);
        int height = dpToPx(80);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(16.0f * density);
        textPaint.setColor(Color.WHITE);
        StaticLayout sl= new StaticLayout(text, textPaint, bitmap.getWidth()-8, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.translate((int)(3.0f * density), (int)(10.0f * density));
        sl.draw(canvas);
        return bitmap;
    }

    int dpToPx(int dp) {
        return (int) (dp * density + 0.5f);
    }

}
