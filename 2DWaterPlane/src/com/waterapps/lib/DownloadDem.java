package com.waterapps.lib;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.waterapps.waterplane.MainActivity;
import com.waterapps.waterplane.R;
import java.io.File;
import java.net.URL;

/**
 * Created by Steve on 2/26/14.
 */
public class DownloadDem {
    WebView webView;
    long enqueue;
    DownloadManager dm;
    BroadcastReceiver receiver;
    Context context;
    int notificationID;
    DemInProgress dlArea;
    LatLngBounds extent;
    private boolean jSubmitForm = false;

    /**
     * Initiates a download of a DEM file from OpenTopo.
     * @param extent the area to be downloaded
     * @param directory the directory to write the DEM to
     * @param map map where progress will be shown
     * @param con app context
     */
    public DownloadDem(final LatLngBounds extent, final String directory, GoogleMap map, Context con) {
        MainActivity.downloading = true;
        this.extent = extent;
        notificationID = (int)System.currentTimeMillis();
        context = con;

        //setup for the the download manager & notification handling
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);

                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {

                            String fileString = c
                                    .getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));

                            File in = new File(fileString);
                            File outputdir = new File(directory);
                            gzip.extractGzip(in, outputdir);
                            in.delete();
                            MainActivity.scanDEMs();

                            //update notification to show completion
                            NotificationManager mNotifyManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.done)
                                            .setContentTitle("DEM download")
                                            .setContentText("Download complete")
                                            .setProgress(0, 0, false);

                            mNotifyManager.notify(notificationID, mBuilder.build());

                            dlArea.remove();
                        }
                    }
                }
                context.unregisterReceiver(receiver);
            }
        };

        context.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        dm = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);

        //create notification
        NotificationManager mNotifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle("DEM download")
                        .setContentText("Download in progress")
                        .setProgress(0, 0, true);

        mNotifyManager.notify(notificationID, mBuilder.build());

        //show dl area on map to update progress
        PolygonOptions rectOptions = new PolygonOptions()
                .add(extent.northeast)
                .add(new LatLng(extent.northeast.latitude, extent.southwest.longitude))
                .add(extent.southwest)
                .add(new LatLng(extent.southwest.latitude, extent.northeast.longitude))
                .strokeColor(Color.BLUE);
        dlArea = new DemInProgress(extent, map);

        //all of the magic happens here
        new runWeb(extent, context).run();
    }

    /**
     * Creates a webview of the opentopo site and uses javascript injection to get the DEM
     */
    private class runWeb {
        private static final String magicString = "25az225MAGICee4587da";
        private static final String errorString = "AsV2gZ2pxd9PcC8pJLkm";
        LatLngBounds extent;
        Context context;

        /**
         *
         * @param extent the area to be downloaded
         * @param con app context
         */
        public runWeb(LatLngBounds extent, Context con) {
            this.extent = extent;
            this.context = con;
        };
        private void run(){
            //create progress box on map
            final double minX = (extent.southwest.longitude < extent.northeast.longitude) ? extent.southwest.longitude :  extent.northeast.longitude;
            final double minY = (extent.southwest.latitude < extent.northeast.latitude) ? extent.southwest.latitude :  extent.northeast.latitude;
            final double maxX = (extent.southwest.longitude > extent.northeast.longitude) ? extent.southwest.longitude :  extent.northeast.longitude;
            final double maxY = (extent.southwest.latitude > extent.northeast.latitude) ? extent.southwest.latitude :  extent.northeast.latitude;

            //this is a slightly modified version of the opentopo job submit page with problematic elements removed
            String initalURL = "file:///android_asset/OpenTopo.html";

            //set up the webview and load the page
            webView = new WebView(MainActivity.getContext());
            webView.setVisibility(View.VISIBLE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new PageHandler());
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d("onPageFinished", "url:" + url);
                    //this is the page where opentopo's datasets are listed
                    //the correct one needs to be selected and its page opened
                    if(url.contains("OpenTopo.html")){
                        if(jSubmitForm == false){
                            Log.d("onPageFinished", "Submitting form");
                            //Initial homemade post page, change post vars here, then post form;
                            String strFunction = "javascript:"
                                    + "   var findForm = setInterval(function(){"
                                    + "	    if(document.getElementsByName('selectForm').length == 0) {"
                                    + "       /*javascript:console.log('"+errorString+" Waiting to find form for entry.');*/"
                                    + "	    } else {"
                                    + "         clearInterval(findForm);"
                                    + "         /*javascript:console.log('"+errorString+" Waiting 3 seconds to submit');*/"
                                    + "         setTimeout(function(){"
                                    + "             document.getElementById('email').value = 'newEmailAddress@email.com';"
                                    + "		        document.getElementById('minX').value = '" + Double.toString(minX) + "';"
                                    + "		        document.getElementById('minY').value = '" + Double.toString(minY) + "';"
                                    + "		        document.getElementById('maxX').value = '" + Double.toString(maxX) + "';"
                                    + "		        document.getElementById('maxY').value = '" + Double.toString(maxY) + "';"
                                    + "		        document.getElementById('lasOutput').checked = 'unchecked';"
                                    + "		        document.getElementById('derivativeSelect').checked = 'unchecked';"
                                    + "		        document.getElementById('visualization').checked = 'unchecked';"
                                    + "		        document.getElementById('resolution').value = '" + Double.toString(3.0) + "';"
                                    + "		        document.getElementById('format').value = 'GTiff';"
                                    + "             setInterval(function(){document.getElementsByName('selectForm')[0].submit(); }, 3000);"
                                    + "         }, 3000);"
                                    + "	    }"
                                    + "   }, 1000);";
                            webView.loadUrl(strFunction);
                            Log.d("onPageFinished - ", "Javascript for submitting form");
                            jSubmitForm = true;
                        }
                    }
                    else if(url.contains("lidarOutput")) {
                        //this is the page with the link to the DEM
                        //but the page dynamically updates with progress until the DEM is ready
                        //so periodically check if the link exists and download if it does
                        String strFunction = "javascript:"
                                + "setInterval(function(){"
                                + " var els = document.getElementsByTagName('a');"
                                + " for (var i = 0, l = els.length; i < l; i++) {"
                                + " var el = els[i];"
                                + " if (el.innerHTML.indexOf('dems.tar.gz') != -1) {"
                                + " javascript:console.log('"+magicString+"'+ el.href);"
                                + " clearInterval();"
                                + " }"
                                + " }"
                                + "}, 5000);";
                        Log.d("url:",strFunction);

                        webView.loadUrl(strFunction);
                    }
                }
            });


            webView.loadUrl(initalURL);
        }

        private class PageHandler extends WebChromeClient {
            public boolean onConsoleMessage(ConsoleMessage cmsg){
                //the 'magic string' indicates that the message contains a URL to download
                if(cmsg.message().startsWith(magicString)){
                    //extract URL
                    String categoryMsg = cmsg.message().substring(magicString.length());
                    downloadFile(categoryMsg);
                    webView.stopLoading();
                    webView.destroy();
                    MainActivity.downloading = false;
                    return true;
                }
                else if (cmsg.message().startsWith(errorString)) {
                    Log.d("onConsoleMessage", "no dem data available");
                    Toast toast = Toast.makeText(context, "No DEM data available for this region", Toast.LENGTH_LONG);
                    toast.show();
                    NotificationManager mNotifyManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyManager.cancel(notificationID);
                    dlArea.remove();
                    webView.stopLoading();
                    webView.destroy();
                    MainActivity.downloading = false;
                    return true;
                }
                Log.d("javascript log: ", cmsg.message());
                return false;
            }
        }
    }

    /**
     * Uses download manager to download a file.
     * @param url the url to download
     */
    private void downloadFile(String url) {
        Log.d("downloadFile", url);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(url));
        enqueue = dm.enqueue(request);

        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;

                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(enqueue);

                    Cursor cursor = dm.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final double dl_progress = ((double) bytes_downloaded / (double) bytes_total);

                    cursor.close();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private class DownloadDemTask extends AsyncTask<URL, Integer, Long> {
        protected Long doInBackground(URL... urls) {
            new runWeb(extent, context).run();
            return (long)0;
        }

        protected void onProgressUpdate(Integer... progress) {
            //TODO meaningful progress updates if that is possible
        }

        protected void onPostExecute(Long result) {
            //TODO clear notification on download completion
        }
    }

}
