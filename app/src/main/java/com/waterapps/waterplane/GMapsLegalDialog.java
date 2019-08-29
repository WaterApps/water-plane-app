package com.waterapps.waterplane;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * DialogPreference that displays the Google Maps legal information.
 */
public class GMapsLegalDialog extends DialogPreference {
    public GMapsLegalDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Deprecated. No Longer Needed.
        // setDialogMessage(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(context));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
        }
    }
}
