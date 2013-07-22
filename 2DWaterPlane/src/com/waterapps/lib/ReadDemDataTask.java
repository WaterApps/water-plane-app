package com.waterapps.lib;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.waterapps.waterplane.MainActivity;

import java.net.URI;

/**
 * Makes ReadDemData interface into an AsyncTask to run off of UI thread
 */
public class ReadDemDataTask extends AsyncTask<URI, Integer, DemData> {
    Context context;
    DemData data;
    ProgressDialog dialog;
    String filename;

    public ReadDemDataTask(Context con, DemData raster) {
        context = con;
        data = raster;
        filename = "the default elevation file";
    }

    public ReadDemDataTask(Context con, DemData raster, String filename) {
        context = con;
        data = raster;
        this.filename = filename;
    }

    /**
     * Creates progress bar before file is read
     */
    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMax(100);
        dialog.setMessage("Loading elevations into map from " + filename);
        dialog.show();
    }

    protected DemData doInBackground(URI... params) {
        publishProgress(0);

        ReadDemData readObject = null;

        //select proper reader for filetype
        //only geotiff supported for now
        if (params[0].getPath().contains(".tif")) readObject = new ReadGeoTiff();
        data = readObject.readFromFile(params[0]);
        publishProgress(100);
        return data;
    }

    protected void onProgressUpdate(Integer... progress) {
        dialog.setProgress(progress[0]);
    }

    /**
     * Once the file read is complete, pass data to handler in MainActivity
     * @param rasters
     */
    protected void onPostExecute(DemData rasters) {
        dialog.dismiss();
        MainActivity.onFileRead(rasters);
    }

}
