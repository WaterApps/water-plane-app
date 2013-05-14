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
		elevationData = new float[width][height];
	}
	
	public float[][] getData() {
		return elevationData;
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
				if (data[0].equals("ncols")) width = Integer.parseInt(data[1]);
				if (data[0].equals("nrows")) height = Integer.parseInt(data[1]);
				if (data[0].equals("xllcorner")) llx = Float.parseFloat(data[1]);
				if (data[0].equals("yllcorner")) lly = Float.parseFloat(data[1]);
				if (data[0].equals("cellsize")) cellsize = Float.parseFloat(data[1]);
				if (data[0].equals("NODATA_value")) nodata = Float.parseFloat(data[1]);
			}
			
			lowerLeft = new LatLng(llx, lly);
			upperRight = new LatLng(llx+(width*cellsize), lly+(height*cellsize));
			System.out.println(upperRight.latitude);
			
			//now read data file (.flt)
			String dataFilename = fileUri.toString().replace("hdr", "flt"); //fileUri.getPath().replace("hdr", "flt");
			System.out.println(dataFilename);
			URI dataUri = URI.create(dataFilename);
			System.out.println(dataUri.getPath());
			//reader = new BufferedReader(new FileReader(new File(dataUri)));
			//reader.read
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(dataUri)));
			byte[] bytes = new byte[width*height*4];
			bis.read(bytes, 0, width*height*4);
			
			elevationData = new float[width][height];
			for(int i = 0; i<width; i++) {
				for(int j = 0; j<height; j++) {
					elevationData[i][j] = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 4*(i+(width*j)), 4*(i+(width*j))+4)).order(ByteOrder.BIG_ENDIAN).getFloat();
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
