package com.waterapps.lib;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.widget.Toast;
import com.waterapps.waterplane.MainActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Steve on 9/27/13.
 */
public class GetOpenTopoDataTask extends AsyncTask<URL, Integer, String> {
    long enqueue;
    DownloadManager dm;

    @Override
    protected String doInBackground(URL... urls) {
        String formString = null;
        try {
            HttpURLConnection con = (HttpURLConnection) urls[0].openConnection();
            InputStream is = com.waterapps.lib.GetOpenTopoData.sendTestData();
            formString = htmlParser.findJobId(is);
            Toast toast = Toast.makeText(MainActivity.getContext(), "job ID is " + formString, Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formString;
    }

    protected void onPostExecute(String demURL) {
        //MainActivity.getDEM(demURL);
        //MainActivity.scanDEMs();
    }
}