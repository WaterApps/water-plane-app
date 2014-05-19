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

    public DownloadDem(final LatLngBounds extent, final String directory, GoogleMap map, Context con) {
        MainActivity.downloading = true;
        this.extent = extent;
        notificationID = (int)System.currentTimeMillis();
        context = con;
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

        //show dl area on map
        PolygonOptions rectOptions = new PolygonOptions()
                .add(extent.northeast)
                .add(new LatLng(extent.northeast.latitude, extent.southwest.longitude))
                .add(extent.southwest)
                .add(new LatLng(extent.southwest.latitude, extent.northeast.longitude))
                .strokeColor(Color.BLUE);
        dlArea = new DemInProgress(extent, map);

        new runWeb(extent, context).run();
        //new DownloadDemTask().execute();
    }

    private class runWeb {
        private static final String magicString = "25az225MAGICee4587da";
        private static final String errorString = "AsV2gZ2pxd9PcC8pJLkm";
        LatLngBounds extent;
        Context context;

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

            String initalURL = "file:///android_asset/OpenTopo.html";

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
                    else if(url.contains("lidarSubmit")) {

                        final String strFunction = "\n" +
                                "var jobRegex = /jobId=[0-9]+&/g;\n" +
                                "var numRegex = /[0-9]+/g;\n" +
                                "var docString = document.documentElement.innerHTML;\n" +
                                "\n" +
                                "setInterval(function(){\n" +
                                "\ttry {\n" +
                                "\t\tvar jobId = jobRegex.exec(docString);\n" +
                                "\t\tjobId = numRegex.exec(jobId[0]);\n" +
                                "\t\tjavascript:console.log('idregex:' + jobId);\n" +
                                "\t\twindow.location.href = \"http://opentopo.sdsc.edu/gridsphere/gridsphere?gs_action=lidarOutput&cid=geonlidarframeportlet&jobId=\" + jobId;\n" +
                                "\t} catch(err) {\n" +
                                "\t}\n" +
                                "}, 5000);\n";
                                //webView.loadUrl(strFunction);
                    }
                    else if(url.contains("lidarOutput")) {
                        Log.d("onPageFinished", "Searching for DEM");
                        Log.d("onPageFinished", "URL:" + url);
                        //Open topo pages
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
                if(cmsg.message().startsWith(magicString)){
                    String categoryMsg = cmsg.message().substring(magicString.length());
                    Log.d("magic:", magicString);
                    Log.d("Link:", categoryMsg);
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
                    System.out.println("Downloading: " + dl_progress);

                    cursor.close();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }); //.start();
    }

    private int getProgressPercentage() {
        int DOWNLOADED_BYTES_SO_FAR_INT = 0, TOTAL_BYTES_INT = 0, PERCENTAGE = 0;

        try {
            Cursor c = dm.query(new DownloadManager.Query()
                    .setFilterById(enqueue));

            if (c.moveToFirst()) {
                DOWNLOADED_BYTES_SO_FAR_INT = (int) c
                        .getLong(c
                                .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                TOTAL_BYTES_INT = (int) c
                        .getLong(c
                                .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            }

            System.out.println("PERCEN ------" + DOWNLOADED_BYTES_SO_FAR_INT
                    + " ------ " + TOTAL_BYTES_INT + "****" + PERCENTAGE);
            PERCENTAGE = (DOWNLOADED_BYTES_SO_FAR_INT * 100 / TOTAL_BYTES_INT);
            System.out.println("PERCENTAGE % " + PERCENTAGE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return PERCENTAGE;
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
