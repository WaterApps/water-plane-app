package com.waterapps.lib;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.waterapps.waterplane.MainActivity;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Steve on 13/09/20.
 */


public class DownloadDemTask extends AsyncTask <URL, Integer, String> {
    long enqueue;
    DownloadManager dm;

    @Override
    protected String doInBackground(URL... urls) {
        String demURL = null;
        try {
            HttpURLConnection con = (HttpURLConnection) urls[0].openConnection();
            demURL = htmlParser.findLink(con.getInputStream());
            Toast toast = Toast.makeText(MainActivity.getContext(), "URL fetched", Toast.LENGTH_SHORT);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return demURL;
    }

    protected void onPostExecute(String demURL) {
        MainActivity.getDEM(demURL);
        MainActivity.scanDEMs();
    }
}
