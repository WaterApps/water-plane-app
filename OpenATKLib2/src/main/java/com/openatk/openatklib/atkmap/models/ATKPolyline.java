package com.openatk.openatklib.atkmap.models;

import java.util.ArrayList;
import java.util.List;
import com.openatk.openatklib.atkmap.views.ATKPolylineViewOptions;


import com.google.android.gms.maps.model.LatLng;

public class ATKPolyline extends ATKModel {
	public List<LatLng> boundary;
	public String label;
	public ATKPolylineViewOptions viewOptions;

	public ATKPolyline(Object id){
		this.id = id;
		this.boundary = new ArrayList<LatLng>();
		this.viewOptions = new ATKPolylineViewOptions();
	}
	public ATKPolyline(Object id, List<LatLng> boundary){
		this.id = id;
		this.boundary = boundary;
		this.viewOptions = new ATKPolylineViewOptions();
	}
}