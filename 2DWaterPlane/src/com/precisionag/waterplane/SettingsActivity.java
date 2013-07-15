package com.precisionag.waterplane;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.precisionag.lib.Field;

/**
 * Created by steve on 6/27/13.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        MainActivity.prefs.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        Preference preference = findPreference("pref_min_elevation");
        preference.setSummary(preferences.getString("pref_min_elevation", "100.0"));
        preference = findPreference("pref_max_elevation");
        preference.setSummary(preferences.getString("pref_max_elevation", "100.0"));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i("preference", key);
        Preference preference = findPreference(key);
        if (key.equals("pref_key_trans_level")) {
            int alpha = sharedPreferences.getInt(key, 50);
            MainActivity.setAlpha(1 - (float) alpha / 100.0f);
        }
        else if (key.equals("pref_key_transparency")) {
            MainActivity.transparency = sharedPreferences.getBoolean(key, true);
        }
        else if (key.equals("pref_key_coloring")) {
            MainActivity.coloring = sharedPreferences.getBoolean("pref_key_coloring", true);
        }
        else if (key.equals("pref_min_elevation")) {
            MainActivity.sliderMin = Float.parseFloat(sharedPreferences.getString("pref_min_elevation", "100.0"));
            if (MainActivity.sliderMin < MainActivity.field.getMinElevation()) {
                MainActivity.sliderMin = (float)MainActivity.field.getMinElevation();
            }
            MainActivity.updateEditText(MainActivity.sliderMin, MainActivity.sliderMax);
            preference.setSummary(sharedPreferences.getString("pref_min_elevation", "100.0"));
        }
        else if (key.equals("pref_max_elevation")) {
            MainActivity.sliderMax = Float.parseFloat(sharedPreferences.getString("pref_max_elevation", "300.0"));
            if (MainActivity.sliderMax > MainActivity.field.getMaxElevation()) {
                MainActivity.sliderMax = (float)MainActivity.field.getMaxElevation();
            }
            MainActivity.updateEditText(MainActivity.sliderMin, MainActivity.sliderMax);
            preference.setSummary(sharedPreferences.getString("pref_max_elevation", "300.0"));
        }
    }

    @Override

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        System.out.print("Intent recieved");
    }
}