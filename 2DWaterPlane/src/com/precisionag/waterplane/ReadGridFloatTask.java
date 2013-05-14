package com.precisionag.waterplane;

import java.net.URI;
import android.os.AsyncTask;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.content.Context;

public class ReadGridFloatTask extends AsyncTask <URI, Integer, ElevationRaster> {
	Context context;
	ElevationRaster data;
	ProgressDialog dialog;
	
	ReadGridFloatTask(Context con, ElevationRaster raster) {
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
		data.readGridFloat(params[0]);
		publishProgress(100);
		return null;
	}
	
	protected void onProgressUpdate(Integer... progress) {
		dialog.setProgress(progress[0]);
	}
	
	protected void onPostExecute(ElevationRaster rasters) {
		dialog.dismiss();
		Toast.makeText(context, "File load complete", Toast.LENGTH_LONG).show();
		
	}

}
