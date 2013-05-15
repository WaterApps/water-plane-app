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
				if (data[0].equals("ncols")) setNcols(Integer.parseInt(data[1]));
				if (data[0].equals("nrows")) setNrows(Integer.parseInt(data[1]));
				if (data[0].equals("xllcorner")) llx = Float.parseFloat(data[1]);
				if (data[0].equals("yllcorner")) lly = Float.parseFloat(data[1]);
				if (data[0].equals("cellsize")) cellsize = Float.parseFloat(data[1]);
				if (data[0].equals("NODATA_value")) nodata = Float.parseFloat(data[1]);
			}
			
			setLowerLeft(new LatLng(llx, lly));
			setUpperRight(new LatLng(llx+(getNcols()*cellsize), lly+(getNrows()*cellsize)));
			System.out.println(getUpperRight().latitude);
			
			//now read data file (.flt)
			String dataFilename = fileUri.toString().replace("hdr", "flt"); //fileUri.getPath().replace("hdr", "flt");
			System.out.println(dataFilename);
			URI dataUri = URI.create(dataFilename);
			System.out.println(dataUri.getPath());
			//reader = new BufferedReader(new FileReader(new File(dataUri)));
			//reader.read
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(dataUri)));
			byte[] bytes = new byte[getNcols()*getNrows()*4];
			bis.read(bytes, 0, getNcols()*getNrows()*4);
			
			setElevationData(new float[getNcols()][getNrows()]);
			for(int i = 0; i<getNcols(); i++) {
				for(int j = 0; j<getNrows(); j++) {
					getElevationData()[i][j] = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 4*(i+(getNcols()*j)), 4*(i+(getNcols()*j))+4)).order(ByteOrder.BIG_ENDIAN).getFloat();
					if (getElevationData()[i][j] != nodata) {
						if (getElevationData()[i][j] < getMinElevation()) setMinElevation(getElevationData()[i][j]);
						if (getElevationData()[i][j] > getMaxElevation()) setMaxElevation(getElevationData()[i][j]);
					}
				}
			}
			System.out.println(getElevationData()[0][0]);
		}
		
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
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
