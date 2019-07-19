package org.blockinger2.game.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;

import org.blockinger2.game.R;

public class HelpActivity extends PreferenceActivityAbstract
{
    private AlertDialog.Builder dialog;

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_help);

        initializeDialog();

        findPreference("pref_help_scoring").setOnPreferenceClickListener(preference -> {
            dialog.setTitle(R.string.pref_help_scoring_title);
            dialog.setMessage(R.string.pref_help_scoring_message);
            dialog.show();

            return true;
        });

        findPreference("pref_help_levels").setOnPreferenceClickListener(preference -> {
            dialog.setTitle(R.string.pref_help_levels_title);
            dialog.setMessage(R.string.pref_help_levels_message);
            dialog.show();

            return true;
        });

        findPreference("pref_help_vibration").setOnPreferenceClickListener(preference -> {
            dialog.setTitle(R.string.pref_help_vibration_title);
            dialog.setMessage(R.string.pref_help_vibration_message);
            dialog.show();

            return true;
        });

        findPreference("pref_help_apm").setOnPreferenceClickListener(preference -> {
            dialog.setTitle(R.string.pref_help_apm_title);
            dialog.setMessage(R.string.pref_help_apm_message);
            dialog.show();

            return true;
        });

        findPreference("pref_help_fps").setOnPreferenceClickListener(preference -> {
            dialog.setTitle(R.string.pref_help_fps_title);
            dialog.setMessage(R.string.pref_help_fps_message);
            dialog.show();

            return true;
        });

        findPreference("pref_help_randomizer").setOnPreferenceClickListener(preference -> {
            dialog.setTitle(R.string.pref_help_randomizer_title);
            dialog.setMessage(R.string.pref_help_randomizer_message);
            dialog.show();

            return true;
        });

        findPreference("pref_help_resumability").setOnPreferenceClickListener(preference -> {
            dialog.setTitle(R.string.pref_help_resumability_title);
            dialog.setMessage(R.string.pref_help_resumability_message);
            dialog.show();

            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        finish();

        return true;
    }

    public void initializeDialog()
    {
        dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);
        dialog.setNeutralButton(R.string.back, (dialog, which) -> dialog.dismiss());
    }
}
