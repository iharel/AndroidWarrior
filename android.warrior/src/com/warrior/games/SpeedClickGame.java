package com.warrior.games;

import android.os.AsyncTask;
import android.text.style.ClickableSpan;
import android.util.Log;

import com.warrior.bluetooth.CommHandler;
import com.warrior.bluetooth.Sync;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.main.MyApp;

public class SpeedClickGame  extends Game  {

	private int myCounterClick = 0;
	private static final int MAX_COUNTER = 10;
	
	
	public SpeedClickGame(MyApp app) {
		super(app);
	}
	public void start() {
		super.start();
		new Run();
	}
	class Run extends AsyncTask<Void,Integer,Void>{
		public Run()
		{
			Log.d("gal","in SpeedTestGame - start2");
			this.execute();
			Log.d("gal","in SpeedTestGame - start3");

		}
		protected Void doInBackground(Void... arg0) {
			Log.d("gal","into doInBackground of RunningGame");
			startTime = Sync.getSystemTime();
			while(myCounterClick < MAX_COUNTER)
			{
				// check if the other device is finished the game
				if(otherDeviceFinishedGame){
					break;
				}
				
				if(isClose){
					break;
				}	
			}
			return null;
		}
		protected void onPostExecute(Void result) {
			if(!otherDeviceFinishedGame || !isClose){
				sendFinish();
			}
		}
	}
	public int getMyCounterClick(){
		return myCounterClick;
	}
	
	public boolean addCounterClick(){
		boolean retValue = false;
		if(state == GAME_STATES.RUNNING){
			if(myCounterClick >= MAX_COUNTER){
				myCounterClick = MAX_COUNTER;
				return retValue;
			}
			else if(myCounterClick <= MAX_COUNTER){
				myCounterClick++;
				commHandler.writeToRmoteDevice((long)myCounterClick);
				iStateGame.myStatusGame(myCounterClick);
				retValue = true;
			}
			
		}
		return retValue;
	}
	public void dateRecevieRunningGame(long data) {
		super.dateRecevieRunningGame(data);
		// because if the value of data is upper to 100 
		// so data belongs to states game  
		if(data >= 0 && data < MAX_COUNTER){
			int competitorCounterClick = (int)data;
			iStateGame.competitorStatusGame(competitorCounterClick);
		}
	}
	public String toString(){
		return "speed click " + Sync.getDateTime();
	}
	public void reset() {
		super.reset();
		myCounterClick = 0;
	}
}
