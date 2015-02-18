package com.waterapps.waterplane;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.DecimalFormat;

/**
 * An implementation of PreferenceActivity.
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    static Preference demDirPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	savedInstanceState = getIntent().getExtras();
    	String minElevation = savedInstanceState.getString("min_elev");
    	String maxElevation = savedInstanceState.getString("max_elev");
        DecimalFormat df = new DecimalFormat("#.#");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        MainActivity.prefs.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        Preference preference = findPreference("pref_min_elevation");
        EditTextPreference editTextPref = (EditTextPreference) findPreference("pref_min_elevation");
        editTextPref.setDialogMessage("The lowest elevation data point is " + df.format(Float.parseFloat(minElevation))+ "m.");
        preference.setSummary(preferences.getString("pref_min_elevation", "100.0"));
        preference = findPreference("pref_max_elevation");
        editTextPref = (EditTextPreference) findPreference("pref_max_elevation");
        editTextPref.setDialogMessage("The highest elevation data point is " + df.format(Float.parseFloat(maxElevation))+ "m.");
        preference.setSummary(preferences.getString("pref_max_elevation", "300.0"));
        preference = findPreference("dem_dir");
        demDirPref = preference;
        preference.setSummary(preferences.getString("dem_dir", "/dem"));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        DecimalFormat df = new DecimalFormat("#.#");
        Preference preference = findPreference(key);
        if (key.equals("pref_key_trans_level")) {
            int alpha = sharedPreferences.getInt(key, 50);
            MainActivity.alpha = (1 - (float) alpha / 100.0f);
        }
        else if (key.equals("pref_key_transparency")) {
            MainActivity.transparency = sharedPreferences.getBoolean(key, true);
        }
        else if (key.equals("pref_key_coloring")) {
            MainActivity.coloring = sharedPreferences.getBoolean("pref_key_coloring", true);
        }
        else if (key.equals("pref_min_elevation")) {
            preference.setSummary(sharedPreferences.getString("pref_min_elevation", "100.0"));
        }
        else if (key.equals("pref_max_elevation")) {
            preference.setSummary(sharedPreferences.getString("pref_max_elevation", "300.0"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        if (item.getItemId() == R.id.menuDone) {
            finish();
        }
        return true;
    }

    public static void updateDemFolder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        demDirPref.setSummary(preferences.getString("dem_dir", "/dem"));
    }
}