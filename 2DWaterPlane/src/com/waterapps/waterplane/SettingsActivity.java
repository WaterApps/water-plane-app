package com.waterapps.waterplane;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import java.text.DecimalFormat;

/**
 * An implementation of PreferenceActivity.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static Preference demDirPref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        DecimalFormat df = new DecimalFormat("#.#");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        MainActivity.prefs.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        Preference preference = findPreference("pref_min_elevation");
        preference.setSummary(preferences.getString("pref_min_elevation", "100.0")  + ", demData min is " + df.format(MainActivity.demData.getMinElevation()));
        preference = findPreference("pref_max_elevation");
        preference.setSummary(preferences.getString("pref_max_elevation", "300.0") + ", demData max is " + df.format(MainActivity.demData.getMaxElevation()));
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
            preference.setSummary(sharedPreferences.getString("pref_min_elevation", "100.0")  + ", demData min is " + df.format(MainActivity.demData.getMinElevation()));
        }
        else if (key.equals("pref_max_elevation")) {
            MainActivity.sliderMax = Float.parseFloat(sharedPreferences.getString("pref_max_elevation", "300.0"));
            MainActivity.updateEditText(MainActivity.sliderMin, MainActivity.sliderMax);
            preference.setSummary(sharedPreferences.getString("pref_max_elevation", "300.0") + ", demData max is " + df.format(MainActivity.demData.getMaxElevation()));
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