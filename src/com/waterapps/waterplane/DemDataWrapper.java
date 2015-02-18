package com.waterapps.waterplane;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.waterapps.lib.DemData;
import org.waterapps.lib.WmacListener;

/**
 * Stores data read in from a DEM, as both raw floats and a bitmap representation.
 */
public class DemDataWrapper {
	private DemData demData;
    private static boolean currentlyDrawing = false;
    static Polyline polyline;
    private int nrows;
	private int ncols;
    private GroundOverlay prevoverlay;

    public DemDataWrapper(WmacListener wmacListener, Bitmap bitmap, LatLng southwest, LatLng northeast, double minHeight, double maxHeight) {
        demData = wmacListener.getDemData();
        nrows = demData.getElevationData().length;
    	ncols = demData.getElevationData()[0].length;
        prevoverlay = demData.getGroundOverlay();
//        calculateTenths(mainAct);
    }

    /**
     * Attempts to adjust to slider minimum and maximum to prevent data outliers from 
     * skewing the scale.  This function looks at the upper and lower 3% values to 
     * make the determination as to whether outliers exist at either or both ends.
     */
    public float[] findSliderMinMax(MainActivity mainAct) {
        int size = nrows*ncols;
        float []tempArray = new float[size];
        for(int i=0; i<nrows; i++) {
            for(int j=0; j<ncols; j++) {
                tempArray[i+(nrows*j)] = demData.getElevationData()[i][j];
            }
        }
        Arrays.sort(tempArray);
        float min = tempArray[(size/100)*3];
        float max = tempArray[(size/100)*97];
        float range = tempArray[size-1] - tempArray[0];
    	
        // Adjustment of the slider minimum away from the data minimum is
        // not necessary if the 3 percentile value is less than the minimum
        // elevation plus 10 percent of the overall data range.
        
        // If the minimum is an outlier, the 3 percentile value will be greater
        // than the minimum elevation plus 10 percent of the data range.
        if (min < (range*10.0f/100.0f)+tempArray[0]) {
        	min = tempArray[0];
        }
        // If the 97 percentile value is greater than 90% of the data range,
        // set the slider to the overall maximum elevation data value.
        if (max > (range*90.0f/100.0f)+tempArray[0]) {
        	max = tempArray[tempArray.length-1];
        }
        return new float[]{min, max};
    }

    ElevationPoint elevationPointFromPixel(int x, int y) {
        double north = demData.getBounds().northeast.latitude;
        double east = demData.getBounds().northeast.longitude;
        double south = demData.getBounds().southwest.latitude;
        double west = demData.getBounds().southwest.longitude;
        double longitude = west+(((double)y/(double)nrows)*(east-west));
        double latitude = south+(((double)x/(double)ncols)*(north-south));
        LatLng location = new LatLng(latitude, longitude);

        return new ElevationPoint(demData.getElevationFromLatLng(location), location);
    }


    /**
     * Finds elevation at every pixel in DEM between points
     * @param p1 Point 1
     * @param p2 Point 2
     * @return list of elevations connecting the points
     */
    public List<ElevationPoint> getLineElevations(LatLng p1, LatLng p2) {
        int width = demData.getElevationBitmap().getWidth();
        int height = demData.getElevationBitmap().getHeight();
        List<ElevationPoint> values = new ArrayList<ElevationPoint>();
        int x1 = 1, x2 = 1, y1 = 1, y2 = 1;
        if (demData.getBounds().contains(p1) && demData.getBounds().contains(p2)) {
            //use linear interpolation to figure out which pixel to get data from
            //should be accurate since fields <= ~1 mile wide
            double north = demData.getBounds().northeast.latitude;
            double east = demData.getBounds().northeast.longitude;
            double south = demData.getBounds().southwest.latitude;
            double west = demData.getBounds().southwest.longitude;

            x1 = (int)((double)width*(p1.longitude-west)/(east-west));
            y1 = (int)((double)height*(p1.latitude-south)/(north-south));
            x2 = (int)((double)width*(p2.longitude-west)/(east-west));
            y2 = (int)((double)height*(p2.latitude-south)/(north-south));

        }
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        int err = dx - dy;

        while (true) {
        	ElevationPoint ep = new ElevationPoint(demData.getElevationData()[height - y1][width - x1], demData.getLatLngFromXY(width - x1, height - y1));
            values.add(ep);

            if (x1 == x2 && y1 == y2) {
                break;
            }

            int e2 = 2 * err;

            if (e2 > -dy) {
                err = err - dy;
                x1 = x1 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y1 = y1 + sy;
            }
        }

        return values;
    }
    
    /**
     * Takes a new water elevation and updates the bitmap to reflect the change
     * @param level New water level
     */
    public void setWaterLevel(double level) {
        MainActivity.seekBar.setProgress((int)(255.0*(level-MainActivity.sliderMin)/(MainActivity.sliderMax-MainActivity.sliderMin)));
        updateColors(MainActivity.waterLevel, MainActivity.coloring, MainActivity.transparency, MainActivity.alpha);
    }

    /**
     * Finds elevation at every pixel in DEM along polyline
     * @param points Points in polyline
     * @return list of elevations connecting the points
     */
    public List<ElevationPoint> getLineElevations(List<LatLng> points) {
        Iterator<LatLng> iter = points.iterator();
        LatLng point1, point2;
        ArrayList<ElevationPoint> elevationPoints = new ArrayList<ElevationPoint>();
        if(iter.hasNext()) {
            point1 = iter.next();
        }
        else return null;
        if(iter.hasNext()) {
            point2 = iter.next();
        }
        else return null;
        elevationPoints.addAll(getLineElevations(point1, point2));
        while (iter.hasNext()) {
            point1 = point2;
            point2 = iter.next();
            elevationPoints.addAll(getLineElevations(point1, point2));
        }
        return elevationPoints;
    }

    /**
     * Finds the lowest elevation on the line connecting the two points
     * @param p1
     * @param p2
     * @return
     */
    public ElevationPoint getMinLine(LatLng p1, LatLng p2) {
        List<ElevationPoint> list = getLineElevations(p1, p2);
        Collections.sort(list);
        return list.get(0);
    }

    /**
     * Finds the lowest elevation on the polyline connecting the points
     * @param points
     * @return
     */
    public ElevationPoint getMinLine(List<LatLng> points) {
        LatLng point1, point2;
        Iterator<LatLng> iter = points.iterator();
        ArrayList<ElevationPoint> elevationPoints = new ArrayList<ElevationPoint>();
        if(iter.hasNext()) {
            point1 = iter.next();
        }
        else return null;
        if(iter.hasNext()) {
            point2 = iter.next();
        }
        else return null;
        elevationPoints.add(getMinLine(point1, point2));
        while (iter.hasNext()) {
            point1 = point2;
            point2 = iter.next();
            elevationPoints.add(getMinLine(point1, point2));
        }
        Collections.sort(elevationPoints);
        return elevationPoints.get(0);
    }

    /**
     * Finds the highest elevation on the line connecting the two points
     * @param p1
     * @param p2
     * @return
     */
    public ElevationPoint getMaxLine(LatLng p1, LatLng p2) {
        List<ElevationPoint> list = getLineElevations(p1, p2);
        Collections.sort(list);
        return list.get(list.size()-1);
    }

    /**
     * Finds the highest elevation on the polyline connecting the points
     * @param points
     * @return
     */
    public ElevationPoint getMaxLine(List<LatLng> points) {
        LatLng point1, point2;
        Iterator<LatLng> iter = points.iterator();
        ArrayList<ElevationPoint> elevationPoints = new ArrayList<ElevationPoint>();
        if(iter.hasNext()) {
            point1 = iter.next();
        }
        else return null;
        if(iter.hasNext()) {
            point2 = iter.next();
        }
        else return null;
        elevationPoints.add(getMaxLine(point1, point2));
        while (iter.hasNext()) {
            point1 = point2;
            point2 = iter.next();
            elevationPoints.add(getMinLine(point1, point2));
        }
        Collections.sort(elevationPoints);
        return elevationPoints.get(elevationPoints.size()-1);
    }

    public void updateColors(double waterLevelMeters, boolean coloring, boolean transparency, float alpha) {
        if (!currentlyDrawing) {
            currentlyDrawing = true;
            //get level from seekbar
//            double distFromMin = waterLevelMeters - MainActivity.sliderMin;
//            double fieldRange = demData.getMaxElevation() - demData.getMinElevation();
//            double fieldRange = MainActivity.sliderMax - MainActivity.sliderMin;
//            double level = 255.0*distFromMin/fieldRange;
//            int waterLevel = (int)level;
            int width = demData.getElevationBitmap().getWidth();
            int height = demData.getElevationBitmap().getHeight();
            int[] pixels = new int[width * height];
            demData.getElevationBitmap().getPixels(pixels, 0, width, 0, 0, width, height);
    
            if (!coloring) {
            	//No elevation shading, just transparent or blue (below water plane)
            	for (int i = 0; i < (width * height); i++) {
                    pixels[i] = (demData.getElevationData()[i/width][width-(i%width)-1] < waterLevelMeters) ? 0xFF0000FF : 0x00000000;
                }

//                for (int i = 0; i < (width * height); i++) {
//                    pixels[i] = ((pixels[i] & 0x000000FF) < waterLevel) ? 0xFF0000FF : 0x00000000;
//                }
            }
            else {
                //Elevation shading is being used
                int c;
                for (int i = 0; i < (width * height); i++) {
                    c = pixels[i] & 0x000000FF;
                    pixels[i] = (demData.getElevationData()[i/width][width-(i%width)-1] < waterLevelMeters) ? demData.getHsvColors()[c] : 0x00000000;
                }
            }
            Bitmap bitmap = demData.getElevationBitmap().copy(demData.getElevationBitmap().getConfig(), true);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            //remove old map overlay and create new one
            //this unfortunately creates annoying flickering
            //currently not aware of any way to avoid this
            //sanoel update: I believe this has been fixed with overlay.setImage
            prevoverlay.setImage(BitmapDescriptorFactory.fromBitmap(bitmap));
//            GroundOverlay ppo = prevoverlay;
//            prevoverlay = createOverlay(mapFragment.getMap());
//            prevoverlay.setImage(BitmapDescriptorFactory.fromBitmap(bitmap));
            if (transparency) {
                prevoverlay.setTransparency(alpha);
            }
//            ppo.remove();
            currentlyDrawing = false;
        }
    }

    public DemData getDemData() {
    	return demData;
    }

}
