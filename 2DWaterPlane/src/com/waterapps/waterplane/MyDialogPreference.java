package com.waterapps.waterplane;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by steve on 6/27/13.
 */
public class MyDialogPreference extends DialogPreference {

    public MyDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            MainActivity.defaultSlider();
        }
    }
}