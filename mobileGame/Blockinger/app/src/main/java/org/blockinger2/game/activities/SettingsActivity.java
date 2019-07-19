package org.blockinger2.game.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import org.blockinger2.game.R;

public class SettingsActivity extends PreferenceActivityAbstract implements OnSharedPreferenceChangeListener
{
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_settings);

        Preference prefVibrationDuration = findPreference("pref_button_vibration_duration");
        String vibrationTimeString = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(prefVibrationDuration.getKey(), "");
        // Set summary to be the user-description for the selected value
        prefVibrationDuration.setSummary(parseTimeString(vibrationTimeString));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("pref_button_vibration_duration")) {
            Preference preference = findPreference(key);
            // Set summary to be the user-description for the selected value
            String vibrationTimeString = sharedPreferences.getString(key, "");
            preference.setSummary(parseTimeString(vibrationTimeString));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        finish();

        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private String parseTimeString(String timeString)
    {
        timeString = timeString.replaceFirst("^0+(?!$)", "");

        if (timeString.equals("")) {
            return "0 ms";
        }

        return timeString + " ms";
    }
}
