package com.waterapps.lib;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLngBounds;
import com.waterapps.waterplane.MainActivity;
import com.waterapps.waterplane.R;

import java.io.File;
import java.util.Random;

import static android.os.Environment.getExternalStorageDirectory;

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

    public DownloadDem(final LatLngBounds extent, final String directory, Context con) {
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
        new runWeb(extent, context).run();
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
            String initalURL = "http://opentopo.sdsc.edu/gridsphere/gridsphere?cid=datasets&minX=" +
                    Double.toString(minX) + "&minY=" +
                    Double.toString(minY) + "&maxX=" +
                    Double.toString(maxX) + "&maxY=" +
                    Double.toString(maxY);

            webView = new WebView(MainActivity.getContext());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new PageHandler());
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {

                    Log.d("onPageFinished", "url:" + url);
                    if(url.contains("datasets")) {
                        Log.d("onPageFinished", "Finding dataset");
                        String strFunction = "javascript:" +
                                "var buttons;" +
                                "buttons = document.querySelectorAll(\"input[value='Get Data']\");" +
                                "if (buttons.length > 0) {" +
                                "   buttons[0].onclick();" +
                                "}" +
                                "else {" +
                                "   javascript:console.log('"+errorString+"');" +
                                "}";
                        webView.loadUrl(strFunction);
                    }
                    else if(url.contains("lidarDataset")){
                        Log.d("onPageFinished", "Submitting form");
                        //Initial homemade post page, change post vars here, then post form;
                        final String strFunction = "javascript:"
                                + "document.getElementById('email').value = 'newEmailAddress@email.com';"
                                + "document.getElementById('minX').value = '" + Double.toString(minX) + "';"
                                + "document.getElementById('minY').value = '" + Double.toString(minY) + "';"
                                + "document.getElementById('maxX').value = '" + Double.toString(maxX) + "';"
                                + "document.getElementById('maxY').value = '" + Double.toString(maxY) + "';"
                                + "document.getElementById('lasOutput').checked = 'unchecked';"
                                + "document.getElementById('derivativeSelect').checked = 'unchecked';"
                                + "document.getElementById('visualization').checked = 'unchecked';"
                                + "document.getElementById('resolution').value = '" + Double.toString(3.0) + "';"
                                + "document.getElementById('format').value = 'GTiff';"
                                + "document.getElementsByName('selectForm')[0].submit();";
                        webView.loadUrl(strFunction);
                        Log.d("url", strFunction);
                    }
                    else {
                        Log.d("onPageFinished", "Searching for DEM");
                        Log.d("onPageFinished", "URL:" + url);
                        //Open topo pages
                        String strFunction = "javascript:"
                                + "var els = document.getElementsByTagName('a');"
                                + "for (var i = 0, l = els.length; i < l; i++) {"
                                + "    var el = els[i];"
                                + "    if (el.innerHTML.indexOf('dems.tar.gz') != -1) {"
                                + "         if (el.href.indexOf('appBulkFormat') != -1) {"
                                + "             javascript:console.log('"+magicString+"'+ el.href);"
                                + "         }"
                                + "    }"
                                + "}";
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
                    return true;
                }
                else if (cmsg.message().startsWith(errorString)) {
                    Log.d("onConsoleMessage", "no dem data available");
                    Toast toast = Toast.makeText(context, "No DEM data available for this region", Toast.LENGTH_LONG);
                    toast.show();
                    NotificationManager mNotifyManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyManager.cancel(notificationID);
                }
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
        }).start();
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
}