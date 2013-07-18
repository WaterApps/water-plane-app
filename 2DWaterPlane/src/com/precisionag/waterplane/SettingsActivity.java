package com.precisionag.waterplane;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.precisionag.lib.Field;

import java.text.DecimalFormat;

/**
 * Created by steve on 6/27/13.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static Preference demDirPref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        DecimalFormat df = new DecimalFormat("#.#");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        MainActivity.prefs.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        Preference preference = findPreference("pref_min_elevation");
        preference.setSummary(preferences.getString("pref_min_elevation", "100.0")  + ", field min is " + df.format(MainActivity.field.getMinElevation()));
        preference = findPreference("pref_max_elevation");
        preference.setSummary(preferences.getString("pref_max_elevation", "300.0") + ", field max is " + df.format(MainActivity.field.getMaxElevation()));
        preference = findPreference("dem_dir");
        demDirPref = preference;
        preference.setSummary(preferences.getString("dem_dir", "/dem"));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        DecimalFormat df = new DecimalFormat("#.#");
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
            MainActivity.updateEditText(MainActivity.sliderMin, MainActivity.sliderMax);
            preference.setSummary(sharedPreferences.getString("pref_min_elevation", "100.0")  + ", field min is " + df.format(MainActivity.field.getMinElevation()));
        }
        else if (key.equals("pref_max_elevation")) {
            MainActivity.sliderMax = Float.parseFloat(sharedPreferences.getString("pref_max_elevation", "300.0"));
            MainActivity.updateEditText(MainActivity.sliderMin, MainActivity.sliderMax);
            preference.setSummary(sharedPreferences.getString("pref_max_elevation", "300.0") + ", field max is " + df.format(MainActivity.field.getMaxElevation()));
        }
    }

    @Override

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        System.out.print("Intent recieved");
    }

    public static void updateDemFolder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        demDirPref.setSummary(preferences.getString("dem_dir", "/dem"));
    }
}