package com.waterapps.lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Steve on 8/29/13.
 */
public class GetOpenTopoData {
    //url: http://opentopo.sdsc.edu/gridsphere/gridsphere?gs_action=lidarDataset&cid=geonlidarframeportlet&opentopoID=OTLAS.062012.4326.1&minX=-86.088&minY=40.739&maxX=-86.064&maxY=40.754
    public BufferedInputStream getPage(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
            readStream(in);
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return in;
    }

    private String readStream(InputStream in) {
        return in.toString();
    }

    public InputStream sendForm(URL url, httpRequest request) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream in = null;
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(request.getBytes());

            in = new BufferedInputStream(urlConnection.getInputStream());
            readStream(in);

        } catch (IOException e) {
            e.printStackTrace();
        }             finally {
            urlConnection.disconnect();
        }
        return in;
    }

    public InputStream sendTestData() {
        httpRequest form = new httpRequest("username", "guest");
        form.addField("minX", "-86.088"); //location
        form.addField("minY", "-86.088");
        form.addField("maxX", "-86.088");
        form.addField("maxY", "-86.088");
        form.addField("classification", "all");
        form.addField("reprojectOutput", "16N"); //UTM projection
        form.addField("lasOutput", "1");
        form.addField("idwView", "1");
        form.addField("resolution", "2"); //resolution in meters
        form.addField("radius", "1");
        form.addField("format", "GTiff");
        form.addField("nullfill", "0");
        form.addField("tinResolution", "1");
        form.addField("maxTriangleSize", "50");
        form.addField("tinFormat", "arc");
        form.addField("derivativeFormat", "GTiff");
        form.addField("vizAltitude", "45");
        form.addField("vizAzimuth", "315");
        form.addField("title", "dtre");
        form.addField("jobDescription", "sfse");
        form.addField("prevJobDescription", "jfweio");
        form.addField("email", "guest%40domain.org");
        form.addField("referer", "LIDARDATASET");
        form.addField("numRows", "1077000"); //may need to be computed
        form.addField("opentopoID", "OTLAS.062012.4326.1");
        form.addField("queryLimit", "50000000"); //can probably be changed to get bigger dems
        form.addField("espg", "32616"); //projection format espg code
        URL url = null;
        try {
            url = new URL("http://opentopo.sdsc.edu/gridsphere/gridsphere?gs_action=lidarDataset&cid=geonlidarframeportlet&opentopoID=OTLAS.062012.4326.1&minX=-86.088&minY=40.739&maxX=-86.064&maxY=40.754");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            return sendForm(url, form);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
