package com.precisionag.lib;

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
import android.graphics.Bitmap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class ElevationRaster {

	private int ncols;
	private int nrows;
	float [][]elevationData;
	private float minElevation;
	private float maxElevation;
	private LatLng lowerLeft;
	private LatLng upperRight;
	
	public ElevationRaster(int w, int h, float [][]data) {
		setMinElevation(Float.POSITIVE_INFINITY);
		setMaxElevation(Float.NEGATIVE_INFINITY);
		setNcols(w);
		setNrows(h);
		setElevationData(data);
	}
	
	public ElevationRaster(int w, int h) {
		setMinElevation(Float.POSITIVE_INFINITY);
		setMaxElevation(Float.NEGATIVE_INFINITY);
		setNcols(w);
		setNrows(h);
		setElevationData(new float[getNcols()][getNrows()]);
	}
	
	public ElevationRaster() {
		setMinElevation(Float.POSITIVE_INFINITY);
		setMaxElevation(Float.NEGATIVE_INFINITY);
		setNcols(1);
		setNrows(1);
		setElevationData(new float[getNcols()][getNrows()]);
	}
	
	public void setDimensions(int w, int h) {
		setNcols(w);
		setNrows(h);
		setElevationData(new float[getNcols()][getNrows()]);
	}
	
	public float[][] getData() {
		return getElevationData();
	}
	
	public LatLngBounds getBounds() {
		return new LatLngBounds(getLowerLeft(), getUpperRight());
	}
	
	public Bitmap getBitmap() {
		Bitmap bitmap;
		int intpixels[] = new int[getNrows()*getNcols()];
		
		for(int k = 0; k<getNrows(); k++) {
			for(int m=0; m<getNcols(); m++) {
		    	//normalize each float to a value from 0-255
		    	intpixels[m+(k*getNcols())] = (int)((getElevationData()[k][m]-getMinElevation())*getMaxElevation()/(double)255);
		    	//intpixels[k] = (int)( ((pixels[k]-min)/(max-min))*(double)255.0);
		    	//convert to greyscale ARGB value
		    	intpixels[k] = 0xFF000000 + intpixels[k] + intpixels[k]<<8 + intpixels[k]<<16;
			}	
	    }
		bitmap = Bitmap.createBitmap(intpixels, 0, getNrows(), getNrows(), getNcols(), Bitmap.Config.ARGB_8888);
		return bitmap;
	}
	

	public float [][] getElevationData() {
		return elevationData;
	}

	public void setElevationData(float [][] elevationData) {
		this.elevationData = elevationData;
	}

	public float getMaxElevation() {
		return maxElevation;
	}

	public void setMaxElevation(float maxElevation) {
		this.maxElevation = maxElevation;
	}

	public float getMinElevation() {
		return minElevation;
	}

	public void setMinElevation(float minElevation) {
		this.minElevation = minElevation;
	}

	public int getNcols() {
		return ncols;
	}

	public void setNcols(int ncols) {
		this.ncols = ncols;
	}

	public int getNrows() {
		return nrows;
	}

	public void setNrows(int nrows) {
		this.nrows = nrows;
	}

	public LatLng getLowerLeft() {
		return lowerLeft;
	}

	public void setLowerLeft(LatLng lowerLeft) {
		this.lowerLeft = lowerLeft;
	}

	public LatLng getUpperRight() {
		return upperRight;
	}

	public void setUpperRight(LatLng upperRight) {
		this.upperRight = upperRight;
	}

}
