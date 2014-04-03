package com.waterapps.lib;

import android.content.Context;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Steve on 3/30/2014.
 */
public class OpenTopoWeb {
    WebView webView;
    Context context;
    private static final String magicString = "25az225MAGICee4587da";
    LatLngBounds extent;

    public OpenTopoWeb(LatLngBounds extent, Context context) {
        this.extent = extent;
        this.context = context;
    };
    private void run(){
        //create progress box on map
        //progress.add(new DemInProgress(extent, map));
        //currentDemDownloads++;
        final double minX = (extent.southwest.longitude < extent.northeast.longitude) ? extent.southwest.longitude :  extent.northeast.longitude;
        final double minY = (extent.southwest.latitude < extent.northeast.latitude) ? extent.southwest.latitude :  extent.northeast.latitude;
        final double maxX = (extent.southwest.longitude > extent.northeast.longitude) ? extent.southwest.longitude :  extent.northeast.longitude;
        final double maxY = (extent.southwest.latitude > extent.northeast.latitude) ? extent.southwest.latitude :  extent.northeast.latitude;
        String initalURL = "http://opentopo.sdsc.edu/gridsphere/gridsphere?cid=datasets&minX=" +
                Double.toString(minX) + "&minY=" +
                Double.toString(minY) + "&maxX=" +
                Double.toString(maxX) + "&maxY=" +
                Double.toString(maxY);

        webView = new WebView(context);
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
                            "   Callback.noDataFound();" +
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
        webView.loadUrl("file:///android_asset/OpenTopo.html");
    }

    private class PageHandler extends WebChromeClient {
        public boolean onConsoleMessage(ConsoleMessage cmsg){
            if(cmsg.message().startsWith(magicString)){
                String categoryMsg = cmsg.message().substring(magicString.length());
                Log.d("magic:", magicString);
                Log.d("Link:", categoryMsg);
                //downloadFile(categoryMsg);
                //currentDemDownloads--;
                webView.stopLoading();
                webView.destroy();
                //if (downloads.peek() != null) {
                //    downloads.poll().run();
                //}
                return true;
            }
            return false;
        }
    }
}