package com.waterapps.lib;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.android.gms.maps.model.LatLngBounds;
import com.waterapps.waterplane.MainActivity;
import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Steve on 2/26/14.
 */
class DownloadDem {
    WebView webView;
    static long enqueue;
    DownloadManager dm;
    BroadcastReceiver receiver;

    public DownloadDem(LatLngBounds extent) {
        new runWeb(extent);
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
                            File outputdir = new File(getExternalStorageDirectory() + "/dem");
                            gzip.extractGzip(in, outputdir);
                            in.delete();
                            MainActivity.scanDEMs();
                        }
                    }
                }
            }
        };
    }

    private class runWeb {
        private static final String magicString = "25az225MAGICee4587da";
        //String magicString;
        LatLngBounds extent;

        public runWeb(LatLngBounds extent) {
            this.extent = extent;
            //magicString = randomString();
            Log.d("magic constructor:", magicString);
        };
        private void run(){
            final double minX = extent.southwest.longitude;
            final double minY = extent.southwest.latitude;
            final double maxX = extent.northeast.longitude;
            final double maxY = extent.northeast.latitude;

            webView = new WebView(MainActivity.getContext());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new PageHandler());
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d("onPageFinished", "url:" + url);
                    if(url.contentEquals("file:///android_asset/OpenTopo.html") == true){
                        Log.d("onPageFinished", "Submitting form");
                        //Initial homemade post page, change post vars here, then post form;
                        final String strFunction = "javascript:"
                                + "document.getElementById('email').value = 'newEmailAddress@email.com';"
                                + "document.getElementById('minX').value = '" + Double.toString(minX) + "';"
                                + "document.getElementById('minY').value = '" + Double.toString(minY) + "';"
                                + "document.getElementById('maxX').value = '" + Double.toString(maxX) + "';"
                                + "document.getElementById('maxY').value = '" + Double.toString(maxY) + "';"
                                + "document.getElementById('resolution').value = '" + Double.toString(3.0) + "';"
                                + "document.getElementById('format').value = 'GTiff';"
                                + "document.getElementById('theForm').submit();";
                        webView.loadUrl(strFunction);
                        Log.d("url", strFunction);

                    } else {
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
            webView.loadUrl("file:///android_asset/OpenTopo.html");
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
}
