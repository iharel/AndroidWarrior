package com.warrior.games;

import java.io.IOException;

import com.androidWarrior.R;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

public class MusicGame {

	private MediaPlayer mp;
	private int currentPosition;
	public MusicGame(Context context){
		 mp = MediaPlayer.create(context, R.raw.music);
	     mp.setLooping(true); // Set looping 
	     mp.setVolume(100,100);
	     mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}
	public void start(){
		mp.start();
	}
	public void pause(){
		mp.pause();
		currentPosition = mp.getCurrentPosition();
	}
	public void resume(){
		mp.seekTo(currentPosition);
		mp.start();
	}
	public void stop(){
		mp.stop();
	}
}
