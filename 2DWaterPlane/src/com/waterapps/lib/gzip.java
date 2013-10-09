package com.waterapps.lib;

import android.os.SystemClock;
import android.util.Log;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;
import java.util.zip.GZIPInputStream;

/**
 * Created by Steve on 9/12/13.
 */
public class gzip {
    public static void extractGzip(File in, File outDir) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        GZIPInputStream zis = null;
        try {
            TarInputStream tis = new TarInputStream(new GZIPInputStream(new BufferedInputStream(is)));
            TarEntry entry;
            while((entry = tis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[2048];

                Random rand = new Random();
                FileOutputStream fos = new FileOutputStream(outDir.getPath() + "/" + new Date().toString() + ".tif");
                Log.d("inputfilename", entry.getName());
                Log.d("outputfilename", outDir.getPath() + "/" + entry.getName());
                BufferedOutputStream dest = new BufferedOutputStream(fos);

                while((count = tis.read(data)) != -1) {
                    dest.write(data, 0, count);
                }

                dest.flush();
                dest.close();
            }

            tis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    void extractTar(String in, String out) {

    }
}
