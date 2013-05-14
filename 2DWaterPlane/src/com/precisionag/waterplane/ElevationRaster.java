package com.precisionag.waterplane;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class ElevationRaster {

	private int ncols;
	private int nrows;
	private float [][]elevationData;
	private float minElevation;
	private float maxElevation;
	private LatLng lowerLeft;
	private LatLng upperRight;
	
	ElevationRaster(int w, int h, float [][]data) {
		minElevation = Float.POSITIVE_INFINITY;
		maxElevation = Float.NEGATIVE_INFINITY;
		ncols = w;
		nrows = h;
		elevationData = data;
	}
	
	ElevationRaster(int w, int h) {
		minElevation = Float.POSITIVE_INFINITY;
		maxElevation = Float.NEGATIVE_INFINITY;
		ncols = w;
		nrows = h;
		elevationData = new float[ncols][nrows];
	}
	
	ElevationRaster() {
		minElevation = Float.POSITIVE_INFINITY;
		maxElevation = Float.NEGATIVE_INFINITY;
		ncols = 1;
		nrows = 1;
		elevationData = new float[ncols][nrows];
	}
	
	public void setDimensions(int w, int h) {
		ncols = w;
		nrows = h;
		elevationData = new float[ncols][nrows];
	}
	
	public float[][] getData() {
		return elevationData;
	}
	
	public LatLngBounds getBounds() {
		return new LatLngBounds(lowerLeft, upperRight);
	}
	
	public Bitmap getBitmap() {
		Bitmap bitmap;
		int intpixels[] = new int[nrows*ncols];
		
		for(int k = 0; k<nrows; k++) {
			for(int m=0; m<ncols; m++) {
		    	//normalize each float to a value from 0-255
		    	intpixels[m+(k*ncols)] = (int)((elevationData[k][m]-minElevation)*maxElevation/(double)255);
		    	//intpixels[k] = (int)( ((pixels[k]-min)/(max-min))*(double)255.0);
		    	//convert to greyscale ARGB value
		    	intpixels[k] = 0xFF000000 + intpixels[k] + intpixels[k]<<8 + intpixels[k]<<16;
			}	
	    }
		bitmap = Bitmap.createBitmap(intpixels, 0, nrows, nrows, ncols, Bitmap.Config.ARGB_8888);
		return bitmap;
	}
	
	public void readGridFloat(URI fileUri) {
		//gridfloat splits the DEM into a header file and data file which consists of packed 32 bit floats
		try {
			//read header file (.hdr)
			BufferedReader reader = new BufferedReader(new FileReader(new File(fileUri)));
			String line = null;
			String[] data;
			//lower left and upper right x and y
			float llx=0, lly=0, nodata=-9999, cellsize=0;
			
			while((line = reader.readLine()) != null) {
				data = line.split("[ \t]+");
				/*
				System.out.println(line);
				for(int i=0;i<data.length;i++) {
					System.out.println(i);
					System.out.println(data[i]);
				}
				*/
				if (data[0].equals("ncols")) ncols = Integer.parseInt(data[1]);
				if (data[0].equals("nrows")) nrows = Integer.parseInt(data[1]);
				if (data[0].equals("xllcorner")) llx = Float.parseFloat(data[1]);
				if (data[0].equals("yllcorner")) lly = Float.parseFloat(data[1]);
				if (data[0].equals("cellsize")) cellsize = Float.parseFloat(data[1]);
				if (data[0].equals("NODATA_value")) nodata = Float.parseFloat(data[1]);
			}
			
			lowerLeft = new LatLng(llx, lly);
			upperRight = new LatLng(llx+(ncols*cellsize), lly+(nrows*cellsize));
			System.out.println(upperRight.latitude);
			
			//now read data file (.flt)
			String dataFilename = fileUri.toString().replace("hdr", "flt"); //fileUri.getPath().replace("hdr", "flt");
			System.out.println(dataFilename);
			URI dataUri = URI.create(dataFilename);
			System.out.println(dataUri.getPath());
			//reader = new BufferedReader(new FileReader(new File(dataUri)));
			//reader.read
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(dataUri)));
			byte[] bytes = new byte[ncols*nrows*4];
			bis.read(bytes, 0, ncols*nrows*4);
			
			elevationData = new float[ncols][nrows];
			for(int i = 0; i<ncols; i++) {
				for(int j = 0; j<nrows; j++) {
					elevationData[i][j] = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 4*(i+(ncols*j)), 4*(i+(ncols*j))+4)).order(ByteOrder.BIG_ENDIAN).getFloat();
					if (elevationData[i][j] != nodata) {
						if (elevationData[i][j] < minElevation) minElevation = elevationData[i][j];
						if (elevationData[i][j] > maxElevation) maxElevation = elevationData[i][j];
					}
				}
			}
			System.out.println(elevationData[0][0]);
		}
		
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
