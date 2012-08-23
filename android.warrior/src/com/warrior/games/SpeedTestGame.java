package com.warrior.games;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import com.warrior.games.Game.GAME_STATES;
import com.warrior.main.CommHandler;
import com.warrior.main.CommHandler.IDataRecevieGame;
import com.warrior.main.Sync;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

class SpeedTestGame extends Game {
		
	
	private boolean otherDeviceFinishedGame = false;
	private final static int MAX_COUNTER = 10000000;
	
	
	public SpeedTestGame(CommHandler commHandler,long timeAir)
	{
		super(commHandler, timeAir);
	}
	public void startGame()
	{
		Log.d("gal","into startGame");
		commHandler.setStateGame(GAME_STATES.RUNNING);
		new RunningGame().execute();
	}
	class RunningGame extends AsyncTask<Void,Integer,Void>{
		protected Void doInBackground(Void... arg0) {
			Log.d("gal","into doInBackground");
			int onePercentProgress = MAX_COUNTER/100;
			startTime = Sync.getSystemTime();
			boolean otherDeviceWon = false;
			for(int i=0;i<=MAX_COUNTER;i++)
			{
				// check if the other device is finished the game
				if(otherDeviceFinishedGame)
				{
					commHandler.setStateGame(GAME_STATES.FINISHED);
					otherDeviceWon = true;
					break;
				}
				if(i % onePercentProgress == 0)
				{
					// update the progressBar of gameActivity 
					int myProgress = i / onePercentProgress;
					publishProgress(myProgress);
				}
			}
			finishTime = Sync.getSystemTime();

			gameTimeMyDevice = finishTime - startTime;
			if (otherDeviceWon) gameTimeMyDevice+=300; // if other device won we wan't to assure that the airTime will not ruin the result.
			if(commHandler.getStateGame() != GAME_STATES.FINISHED){
				try {
					//Thread.sleep(0);//timeAir); // timeAir not required here any longer.
					commHandler.setStateGame(GAME_STATES.FINISHED);
					commHandler.writeToRmoteDevice(Game.SEND_FINISH_GAME);
					Thread.sleep(300);
				} catch (InterruptedException e) {
					Log.d("gal",e.getMessage());
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
			try {
				commHandler.writeToRmoteDevice(Game.SEND_GAME_TIME);
				Thread.sleep(300);
				commHandler.writeToRmoteDevice(gameTimeMyDevice);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void dataRecevieInGame(long data) {
		if(data == Game.RECEIVE_FINISH_GAME){ // Competitor reached end game.
			otherDeviceFinishedGame = true;
		}
		if(data > 0 && data <= 100){ // competitor hasn't finished yet.
			int competitorProgress = (int)data;
			iStateGame.competitorStatusGame(competitorProgress);
		}
	}
	public String toString(){

		return "speed test phone " + Sync.getDateTime();
	}
	protected void resetGame(){
		super.resetGame();
		otherDeviceFinishedGame = false;
	}
}




