package org.blockinger2.game.engine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;

import org.blockinger2.game.R;

import java.util.Objects;

public class Sound implements OnAudioFocusChangeListener
{
    private int soundID_tetrisSoundPlayer;
    private int soundID_dropSoundPlayer;
    private int soundID_clearSoundPlayer;
    private int soundID_gameOverPlayer;
    private int soundID_buttonSoundPlayer;

    private Activity host;
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private boolean noFocus;
    private boolean isMusicReady;
    private BroadcastReceiver noisyAudioStreamReceiver;
    private BroadcastReceiver ringerModeReceiver;
    private BroadcastReceiver headsetPlugReceiver;
    private SoundPool soundPool;
    private int songtime;
    private int musicType;
    private boolean inactive;

    public static final int MENU_MUSIC = 0x1;
    public static final int GAME_MUSIC = 0x2;

    public Sound(Activity activity)
    {
        host = activity;

        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Request AudioFocus if The Music Volume is greater than zero
        requestFocus();

        IntentFilter intentFilter;
        // Noise Receiver (when unplugging headphones)
        intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        noisyAudioStreamReceiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                Sound.this.pauseMusic();
            }
        };
        activity.registerReceiver(noisyAudioStreamReceiver, intentFilter);

        // Headphone Receiver (when headphone state changes)
        intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);

        headsetPlugReceiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                if (Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_HEADSET_PLUG)) {
                    int state = intent.getIntExtra("state", -1);

                    switch (state) {
                        case 0:
                            // Headset is unplugged
                            // this event is broadcasted later than ACTION_AUDIO_BECOMING_NOISY
                            // and hence it's the inferior choice
                            break;

                        case 1:
                            // Headset is plugged
                            Sound.this.startMusic(musicType, songtime);
                            break;

                        default:
                            // I have no idea what the headset state is
                    }
                }
            }
        };

        activity.registerReceiver(headsetPlugReceiver, intentFilter);

        // Ringer mode receiver (when the user changes audio mode to silent or back to normal)
        intentFilter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);

        ringerModeReceiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                songtime = getSongtime();
                Sound.this.pauseMusic();
                Sound.this.startMusic(musicType, songtime);
            }
        };

        activity.registerReceiver(ringerModeReceiver, intentFilter);

        soundPool = new SoundPool(activity.getResources()
            .getInteger(R.integer.audio_streams), AudioManager.STREAM_MUSIC, 0);

        soundID_tetrisSoundPlayer = -1;
        soundID_dropSoundPlayer = -1;
        soundID_clearSoundPlayer = -1;
        soundID_gameOverPlayer = -1;
        soundID_buttonSoundPlayer = -1;

        songtime = 0;
        musicType = 0;
        isMusicReady = false;
        inactive = false;
    }

    private void requestFocus()
    {
        SharedPreferences prefs;

        try {
            prefs = PreferenceManager.getDefaultSharedPreferences(host);
        } catch (Exception e) {
            noFocus = true;
            return;
        }

        if (prefs == null) {
            noFocus = true;
            return;
        }

        if (prefs.getInt("pref_music_volume", 60) > 0) {
            int result = audioManager.requestAudioFocus(this,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
            noFocus = result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    public void setInactive(boolean inactive)
    {
        this.inactive = inactive;
    }

    public void loadEffects()
    {
        soundID_tetrisSoundPlayer = soundPool.load(host, R.raw.tetris, 1);
        soundID_dropSoundPlayer = soundPool.load(host, R.raw.drop, 1);
        soundID_buttonSoundPlayer = soundPool.load(host, R.raw.key, 1);
        soundID_clearSoundPlayer = soundPool.load(host, R.raw.clear, 1);
        soundID_gameOverPlayer = soundPool.load(host, R.raw.gameover, 1);
    }

    private void loadMusic(int type, int startTime)
    {
        // Reset previous music
        isMusicReady = false;

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = null;

        // Check if music is allowed to start
        requestFocus();
        if (noFocus || inactive) {
            return;
        }

        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return;
        }

        // Start music
        songtime = startTime;
        musicType = type;

        switch (type) {
            case MENU_MUSIC:
                mediaPlayer = MediaPlayer.create(host, R.raw.lemmings);
                break;

            case GAME_MUSIC:
                mediaPlayer = MediaPlayer.create(host, R.raw.sadrobot);
                break;

            default:
                mediaPlayer = new MediaPlayer();
                break;
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.01f * PreferenceManager.getDefaultSharedPreferences(host).getInt("pref_music_volume", 60), 0.01f * PreferenceManager.getDefaultSharedPreferences(host).getInt("pref_music_volume", 60));
        mediaPlayer.seekTo(songtime);
        isMusicReady = true;
    }

    public void startMusic(int type, int startTime)
    {
        // Check if music is allowed to start
        requestFocus();

        if (noFocus | inactive) {
            return;
        }

        if (isMusicReady) {
            if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                return;
            }

            mediaPlayer.setVolume(0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_music_volume", 60), 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_music_volume", 60));
            mediaPlayer.start();
        } else {
            loadMusic(type, startTime);
        }
    }

    public void clearSound()
    {
        if (noFocus) {
            return;
        }

        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return;
        }

        soundPool.play(
            soundID_clearSoundPlayer,
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            1,
            0,
            1.0f
        );
    }

    public void buttonSound()
    {
        if (noFocus) {
            return;
        }

        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return;
        }

        if (!PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_button_sound", true)) {
            return;
        }

        soundPool.play(
            soundID_buttonSoundPlayer,
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            1,
            0,
            1.0f
        );
    }

    public void dropSound()
    {
        if (noFocus) {
            return;
        }

        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return;
        }

        soundPool.play(
            soundID_dropSoundPlayer,
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            1,
            0,
            1.0f
        );
    }

    public void tetrisSound()
    {
        if (noFocus) {
            return;
        }

        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return;
        }

        soundPool.play(
            soundID_tetrisSoundPlayer,
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            1,
            0,
            1.0f
        );
    }

    public void gameOverSound()
    {
        if (noFocus) {
            return;
        }

        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return;
        }

        pause(); // Pause music to make the end of the game feel more dramatic

        soundPool.play(
            soundID_gameOverPlayer,
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60),
            1,
            0,
            1.0f
        );
    }

    public void resume()
    {
        if (inactive) {
            return;
        }

        soundPool.autoResume();
        startMusic(musicType, songtime);
    }

    private void pauseMusic()
    {
        isMusicReady = false;

        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                isMusicReady = true;
            } catch (IllegalStateException e) {
                isMusicReady = false;
            }
        }
    }

    public void pause()
    {
        soundPool.autoPause();
        pauseMusic();
    }

    public void release()
    {
        soundPool.autoPause();
        soundPool.release();
        soundPool = null;
        isMusicReady = false;

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = null;

        host.unregisterReceiver(noisyAudioStreamReceiver);
        host.unregisterReceiver(ringerModeReceiver);
        host.unregisterReceiver(headsetPlugReceiver);
        audioManager.abandonAudioFocus(this);
        audioManager = null;
        host = null;
        noFocus = true;
    }

    @Override
    public void onAudioFocusChange(int focusChange)
    {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            noFocus = true;

            if (mediaPlayer != null) {
                try {
                    mediaPlayer.setVolume(0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                        .getInt("pref_music_volume", 60), 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                        .getInt("pref_music_volume", 60));
                } catch (IllegalStateException e) {
                    //
                }
            }

            soundPool.setVolume(soundID_tetrisSoundPlayer, 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));

            soundPool.setVolume(soundID_dropSoundPlayer, 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));

            soundPool.setVolume(soundID_clearSoundPlayer, 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));

            soundPool.setVolume(soundID_gameOverPlayer, 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));

            soundPool.setVolume(soundID_buttonSoundPlayer, 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.0025f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            noFocus = true;
            pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            noFocus = false;

            if (mediaPlayer != null) {
                try {
                    mediaPlayer.setVolume(0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                        .getInt("pref_music_volume", 60), 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                        .getInt("pref_music_volume", 60));
                } catch (IllegalStateException e) {
                    //
                }
            }

            soundPool.setVolume(soundID_tetrisSoundPlayer, 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));

            soundPool.setVolume(soundID_dropSoundPlayer, 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));

            soundPool.setVolume(soundID_clearSoundPlayer, 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));

            soundPool.setVolume(soundID_gameOverPlayer, 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));

            soundPool.setVolume(soundID_buttonSoundPlayer, 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60), 0.01f * PreferenceManager.getDefaultSharedPreferences(host)
                .getInt("pref_sound_volume", 60));

            resume();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            noFocus = true;
            pause();
        }
    }

    public int getSongtime()
    {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                //
            }
        }

        return 0;
    }
}
