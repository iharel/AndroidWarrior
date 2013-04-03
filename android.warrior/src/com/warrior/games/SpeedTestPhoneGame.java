package com.warrior.games;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import com.warrior.bluetooth.CommHandler;
import com.warrior.bluetooth.Sync;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.main.MyApp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class SpeedTestPhoneGame extends Game {
		
	private final static int MAX_COUNTER = 10000000;
	
	
	public SpeedTestPhoneGame(MyApp app){
		super(app);
	}
	public void start(){
		super.start();
		new Run();
	}
	class Run extends AsyncTask<Void,Integer,Void>{
		public Run()
		{
			Log.d("gal","in SpeedTestGame - start3");
			this.execute();
			Log.d("gal","in SpeedTestGame - start3");

		}
		protected Void doInBackground(Void... arg0) {
			Log.d("gal","into doInBackground of RunningGame");
			int onePercentProgress = MAX_COUNTER/100;
			startTime = Sync.getSystemTime();
			for(int i=0;i<=MAX_COUNTER;i++)
			{
				// check if the other device is finished the game
				if(otherDeviceFinishedGame){
					break;
				}
				while(isPause){
					if(isClose){
						return null;
					}	
				}
				if(i % onePercentProgress == 0)
				{
					// update the progressBar of gameActivity 
					int myProgress = i / onePercentProgress;
					publishProgress(myProgress);
				}
			}
			return null;
		}
		protected void onProgressUpdate(Integer... values) {
			iStateGame.myStatusGame(values[0]); // update our progress in progress bar
			long myProgress = values[0];
			commHandler.writeToRmoteDevice(myProgress); // update competitor of our progress
		}
		protected void onPostExecute(Void result) {
			if(!otherDeviceFinishedGame || !isClose){
				sendFinish();
			}
		}
	}
	public void dateRecevieRunningGame(long data) {
		super.dateRecevieRunningGame(data);
		if(data > 0 && data <= 100){ // competitor hasn't finished yet.
			int competitorProgress = (int)data;
			iStateGame.competitorStatusGame(competitorProgress);
		}
	}
	public String toString(){
		return "speed test phone " + Sync.getDateTime();
	}
}




