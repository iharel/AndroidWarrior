package com.warrior.games;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TwoLineListItem;

public class TimerGame implements Runnable{
	private IUpdateTime iUpdateTime;
	private Handler handler;
	private Message message;
	private int miliSec,sec,mins;
	private static final int MAX_MILI_SEC = 1000,MAX_SEC = 60,MAX_MINS = 60,TWO_NUMBERS = 10 ;
	private static final String KEY_GAME_TIME = "gameTime";
	private boolean isStop = false,isPause = false;
	
	public TimerGame(){
		buildHandler();
		reset();
	}
	public void setUpdateTimeListener(IUpdateTime iUpdateTime){
		this.iUpdateTime = iUpdateTime;
	}
	public void setStart(){
		Thread timerThread = new Thread(this);
		timerThread.start();
	}
	public int getMiliSecTime() {
		return miliSec;
	}
	public int getSecTime() {
		return sec;
	}
	public int getMinTime() {
		return mins;
	}
	public int getTime(){
		int timeInMiliSec = 0;
		timeInMiliSec += mins * 60 *1000;
		timeInMiliSec += sec & 1000;
		timeInMiliSec +=miliSec;
		Log.d("getTime","" + timeInMiliSec);
		return timeInMiliSec;
	}
	private void buildHandler(){
		handler = new Handler(){
			 public void handleMessage(Message msg) {
				 String time = msg.getData().getString(KEY_GAME_TIME);
		         iUpdateTime.updateTime(time,TimerGame.this);
		         Log.d("handleMessage","into handleMessage " + time);
		     }
		};
	}
	public void stop(){
		isStop = true;
	}
	public void setPause(boolean isPause){
		this.isPause= isPause; 
	}
	public void run(){
		while(!isStop){
			try {
				while(isPause){
					Thread.sleep(10);
				}
				updateTime();
				Thread.sleep(10);
				message = handler.obtainMessage();
				Bundle bundle = new Bundle();
				bundle.putString(KEY_GAME_TIME, toString());
				message.setData(bundle);
				message.sendToTarget();
				miliSec += 10;
				Log.d("time","" + miliSec);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void addSeconds(int seconds){
		seconds *= MAX_MILI_SEC;
		sec += seconds;
	}
	private void updateTime(){
		if(miliSec >= MAX_MILI_SEC){
			miliSec = 0;
			sec++;
		}
		if(sec >= MAX_SEC){
			sec = 0;
			mins++;
		}
		if(mins >= MAX_MINS){
			mins = 0;
		}
	}
	
	public void reset(){
		miliSec = 0;
		sec = 0;
		mins = 0;
		isStop = false;
		isPause = false;
	}
	public String toString() {
		StringBuilder strB = new StringBuilder();
		if(mins < TWO_NUMBERS){
			strB.append("0");
		}
		strB.append(mins + ":");
		if(sec < TWO_NUMBERS){
			strB.append("0");
		}
		strB.append(sec + ":");
		if(miliSec < TWO_NUMBERS){
			strB.append("0");
		}
		strB.append("" + miliSec);
		return strB.toString();
	}

	
	public interface IUpdateTime{
		void updateTime(String time,TimerGame timerGame);
	}
}
