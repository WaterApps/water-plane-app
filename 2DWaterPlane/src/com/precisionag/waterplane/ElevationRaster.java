package com.precisionag.waterplane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

import com.google.android.gms.maps.model.LatLng;

public class ElevationRaster {

	private int width;
	private int height;
	private float [][]elevationData;
	private LatLng lowerLeft;
	private LatLng upperRight;
	
	ElevationRaster(int w, int h, float [][]data) {
		width = w;
		height = h;
		elevationData = data;
	}
	
	ElevationRaster(int w, int h) {
		width = w;
		height = h;
		elevationData = new float[width][height];
	}
	
	ElevationRaster() {
		width = 1;
		height = 1;
		elevationData = new float[width][height];
	}
	
	public void setDimensions(int w, int h) {
		width = w;
		height = h;
	}
	
	public float[][] getData() {
		return elevationData;
	}
	
	public void readGridFloat(URI fileUri) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(fileUri)));
			String line = null;
			String[] data;
			//lower left and upper right x and y
			float llx=0, lly=0, nodata=-9999, cellsize=0;
			
			while((line = reader.readLine()) != null) {
				data = line.split(" ");
				if (data[0] == "ncols") width = Integer.parseInt(data[1]);
				if (data[0] == "xllcorner") llx = Integer.parseInt(data[1]);
				if (data[0] == "yllcorner") lly = Integer.parseInt(data[1]);
				if (data[0] == "cellsize") cellsize = Integer.parseInt(data[1]);
				if (data[0] == "NODATA_value") nodata = Float.parseFloat(data[1]);
			}
			
			lowerLeft = new LatLng(llx, lly);
			upperRight = new LatLng(llx+(width*cellsize), lly+(height*cellsize));
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
