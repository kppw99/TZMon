package com.learn.gamelearn;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {

	private Context context;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap;
	private AudioManager manager;


	public SoundManager(Context context){
		this.context = context;
		this.soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		this.soundPoolMap = new HashMap<Integer, Integer>();
		this.manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void addSound(int index, int SoundID)
	{
		soundPoolMap.put(index, soundPool.load(context, SoundID, 1));
	}

	public void playSound(int index){

		int streamVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		//streamVolume = streamVolume / manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		soundPool.play(soundPoolMap.get(index), streamVolume, streamVolume, 1, 0, 1f);
	}
}
