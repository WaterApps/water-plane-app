package com.precisionag.lib;

import com.precisionag.waterplane.MainActivity;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public  class TouchableWrapper extends FrameLayout {

public TouchableWrapper(Context context) {
   super(context);
   // TODO Auto-generated constructor stub
}

@Override
public boolean dispatchTouchEvent(MotionEvent ev) {
	MainActivity.updateMarkers();
   return super.dispatchTouchEvent(ev);
 }
}