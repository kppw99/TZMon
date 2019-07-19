package org.blockinger2.game.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import org.blockinger2.game.R;

public class AboutActivity extends PreferenceActivityAbstract
{
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        addPreferencesFromResource(R.xml.pref_about);

        findPreference("pref_license").setOnPreferenceClickListener(preference -> {
            String licenseUrl = getResources().getString(R.string.license_url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(licenseUrl));
            startActivity(intent);

            return true;
        });

        findPreference("pref_license_music").setOnPreferenceClickListener(preference -> {
            String musicUrl = getResources().getString(R.string.music_url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(musicUrl));
            startActivity(intent);

            return true;
        });

        findPreference("pref_version").setOnPreferenceClickListener(preference -> {
            String repositoryUrl = getResources().getString(R.string.repository_url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(repositoryUrl));
            startActivity(intent);

            return true;
        });

        findPreference("pref_maintainer").setOnPreferenceClickListener(preference -> {
            String maintainerEmail = getResources().getString(R.string.maintainer_email);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{maintainerEmail});
            intent.setType("plain/text");
            startActivity(Intent.createChooser(intent, "Send email..."));

            return true;
        });

        findPreference("pref_author").setOnPreferenceClickListener(preference -> {
            String authorUrl = getResources().getString(R.string.author_url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(authorUrl));
            startActivity(intent);

            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        finish();

        return true;
    }
}
