package com.precisionag.lib;

import java.net.URI;

import com.precisionag.waterplane.MainActivity;


import android.os.AsyncTask;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.content.Context;

public class ReadElevationRasterTask extends AsyncTask <URI, Integer, ElevationRaster> {
	Context context;
	ElevationRaster data;
	ProgressDialog dialog;
	
	public ReadElevationRasterTask(Context con, ElevationRaster raster) {
		context = con;
		data = raster;
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		//dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(100);
		dialog.setMessage("Please wait while file is read...");
		dialog.show();
	}
	
	protected ElevationRaster doInBackground(URI... params) {
		publishProgress(0);
		
		ReadElevationRaster readObject = null;
		
		//select proper reader for filetype
		if (params[0].getPath().contains(".hdr")) readObject = new ReadGridFloat();
		
		data = readObject.readFromFile(params[0]);
		publishProgress(100);
		return data;
	}
	
	protected void onProgressUpdate(Integer... progress) {
		dialog.setProgress(progress[0]);
	}
	
	protected void onPostExecute(ElevationRaster rasters) {
		dialog.dismiss();
		Toast.makeText(context, "File load complete", Toast.LENGTH_LONG).show();
		MainActivity.onFileRead(rasters);
	}

}
