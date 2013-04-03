package com.warrior.games;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.warrior.bluetooth.CommHandler;
import com.warrior.bluetooth.Sync;
import com.warrior.games.TimerGame.IUpdateTime;
import com.warrior.main.MyApp;
import com.warrior.main.MyApp.IDataRecevieGame;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

abstract public class Game implements IDataRecevieGame{
	
	public final static long SPEED_TEST_PHONE = 1000;
	public final static long SPEED_CLICK = 1001;
	public final static long SPEED_CALCULATION = 1002;
	
	public final static long RECEIVE_READY=101,SEND_READY= 101;	
	public final static long RECEIVE_START = 102,SEND_START = 102;
	public final static long RECEIVE_FINISH = 103,SEND_FINISH = 103;
	public final static long RECEIVE_GAME_TIME = 104,SEND_GAME_TIME = 104;
	public final static long RECEIVE_REAMTCH = 105,SEND_REAMTCH = 105;
	public final static long RECEIVE_CLOSE = 106,SEND_CLOSE = 106;
	public final static long RECEIVE_PAUSE = 107,SEND_PAUSE = 107;
	public final static long RECEIVE_RESUME = 108,SEND_RESUME = 108;
	
	protected boolean otherDeviceFinishedGame = false;
	protected boolean otherDeviceCloseGame = false;
	protected boolean isPause = false;
	protected boolean isClose = false;
	protected boolean waitToReciveGameTime = false;
	protected boolean isReady = false;
	protected long startTime,finishTime;
	protected long gameTimeMyDevice,gameTimeOtherDevice;
	protected CommHandler commHandler;
	protected long timeAir;
	private TimerGame timer;
	protected GAME_STATES state;
	
	protected IStateGame iStateGame;
	private ITimerGame iTimerGame;
	
	public Game(MyApp app){
		this.commHandler = app.getCommHandler();
		this.timeAir = app.getTimeAir();
		app.setDataRecevieGameListener(this);
		timer = new TimerGame();
		reset();
		
		timer.setUpdateTimeListener(new IUpdateTime() {
			public void updateTime(String time,TimerGame timer) {
				iTimerGame.gameTimeChanged(time, timer);
				 Log.d("updateTime","into updateTime" + time);
			}
		});
	}
	public void setStateGameListener(IStateGame iStateGame){
		this.iStateGame = iStateGame;
	}
	public void setTimerGameListener(ITimerGame iTimerGame){
		this.iTimerGame = iTimerGame;
	}
	public void setPause(boolean isPause){
		if(isPause){
			sendPause();
		}
		else{
			sendResume();
		}
		
	}
	public void setClose(boolean isClose){
		if(isClose){
			sendClose();
		}
	}
	public void setReady(boolean isReady) {
		this.isReady = isReady;
		if(isReady){
			sendReady();
		}
	}
	public void setRematch(boolean isRematch){
		if(isRematch){
			sendRematch();
		}
	}
	public boolean isPause(){
		return isPause;
	}
	public GAME_STATES getState(){
		return state;
	}
	public boolean isWinner(){
		return !otherDeviceFinishedGame;
	}
	public void start(){
		state = GAME_STATES.RUNNING;
		timer.setStart();
		iStateGame.startGame();
		Log.d("gal","into start game");
	}
	protected void sendReady(){
		commHandler.writeToRmoteDevice(SEND_READY);
		Sync.waitTime(timeAir);
	}
	protected void sendPause(){
		commHandler.writeToRmoteDevice(SEND_PAUSE);
		Sync.waitTime(timeAir);
		pause();
	}
	protected void sendResume(){
		commHandler.writeToRmoteDevice(SEND_RESUME);
		Sync.waitTime(timeAir);
		resume();
	}
	protected void sendRematch(){
		commHandler.writeToRmoteDevice(SEND_REAMTCH);
		Sync.waitTime(timeAir);
		rematch();
	}
	protected void sendFinish(){
		timer.stop();
		calculateGameTime();
		// send game time to other device
		Log.d("gal","i finished and write to remote SEND_FINISH_GAME");
		commHandler.writeToRmoteDevice(SEND_FINISH);
		Sync.waitTime(200);
		state = GAME_STATES.FINISHED;
		commHandler.writeToRmoteDevice(SEND_GAME_TIME);
		Sync.waitTime(200);
		commHandler.writeToRmoteDevice(gameTimeMyDevice);
	}
	protected void sendClose(){
		commHandler.writeToRmoteDevice(SEND_CLOSE);
		close();
	}
	protected void calculateGameTime(){
		// calculation my game time
		finishTime = Sync.getSystemTime();
		gameTimeMyDevice += finishTime - startTime;
		//gameTimeMyDevice += (long)timer.getTime();
	}
	protected void pause(){
		isPause = true;
		timer.setPause(isPause);
		iStateGame.pauseGame();
	}
	protected void resume(){
		isPause = false;
		timer.setPause(isPause);
		iStateGame.resumeGame();
	}
	protected void rematch(){
		reset();
		iStateGame.rematchGame();
	}
	protected void close(){
		isClose = true;
		state = GAME_STATES.FINISHED;
		timer.stop();
		iStateGame.closeGame();
	}
	protected void dataReceiveNotYetGame(long data) {
		Log.d("gal","data before game " + data);
		if(RECEIVE_READY == data){
			if(!isReady){
				commHandler.writeToRmoteDevice(SEND_READY);
				Sync.waitTime(timeAir);
				isReady = true;
			}
			iStateGame.readyGame();
		}
	}
	
	protected void dateRecevieRunningGame(long data) {
		if(data == RECEIVE_FINISH){ // Competitor reached end game.
			otherDeviceFinishedGame = true;
			gameTimeMyDevice += 300;
			sendFinish();
		}
	
		else if(data == RECEIVE_PAUSE){
			pause();
		}
		else if(data == RECEIVE_RESUME){
			resume();
		}
		else if(data == RECEIVE_CLOSE){
			close();
		}
	}
	protected void dataReceiveFinishGame(long data){
		Log.d("gal","Data when entering data receive finish game " + data);
		if(data == RECEIVE_GAME_TIME){
			Log.d("gal","the game wait to receive game time");
			waitToReciveGameTime = true;
			return;
		}
		
		else if(waitToReciveGameTime){
			Log.d("gal","the game receive game time");
			gameTimeOtherDevice = data;
			waitToReciveGameTime = false;
			// send message to activity who is winner
			boolean myDeviceWinner = myDeviceIsWinner();
			iStateGame.finishGame(myDeviceWinner);
			writeResultGame(myDeviceWinner);
			
		}
		// this code section just to client side
		if(data == RECEIVE_REAMTCH){
			rematch();
		}
		else if(data == RECEIVE_CLOSE){
			close();
		}
	}
	@Override
	public void dataReceiveGame(long data) {
		switch(state){
			case NOT_YET:{
				dataReceiveNotYetGame(data);
				break;
			}
			case RUNNING:{
				dateRecevieRunningGame(data);
				break;
			}
			case FINISHED:{
				dataReceiveFinishGame(data);
				break;
			}
		}
	}
	
	protected boolean myDeviceIsWinner()
	{
		// check if this device is winner
		
		boolean isWinner = true;
		
		
		if(gameTimeMyDevice > gameTimeOtherDevice){
			isWinner = false;
		}
		
		return isWinner;
	}

	public void reset(){
		Log.d("gal","into resetGame");
		state = GAME_STATES.NOT_YET;
		startTime = 0;
		finishTime = 0;
		gameTimeMyDevice = 0;
		gameTimeOtherDevice = 0;
		otherDeviceCloseGame = false;
		otherDeviceFinishedGame = false;
		isPause = false;
		isClose = false;
		isReady = false;
		timer.reset();
	}
	protected void writeResultGame(boolean isWinner)
	{
		String filename = this.toString() + ".txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		try {
		    PrintWriter pw = new PrintWriter(new FileOutputStream(file,true));
		    pw.println("type game is:" + this.toString());
		    pw.println("my device game time is:" + gameTimeMyDevice);
		    pw.println("other device game time is:" + gameTimeOtherDevice);
		    if(isWinner)
		    {
		    	pw.println("I won");
		    }
		    else
		    {
		    	pw.println("I lost");
		    }
		    pw.println("********************************************************");
		    pw.flush();
		    pw.close();
		} catch (FileNotFoundException e) {
		    try {
				file.createNewFile();
				writeResultGame(isWinner);
			} catch (IOException e1) {
				Log.d("gal",e.getMessage());
				e1.printStackTrace();
			}
		} 
	}
	
	public interface IStateGame
	{
		void readyGame();
		void startGame();
		void pauseGame();
		void resumeGame();
		void myStatusGame(int status);
		void competitorStatusGame(int status);
		void finishGame(boolean isWinner);
		void closeGame();
		void rematchGame();
	}
	public interface ITimerGame{
		void gameTimeChanged(String time,TimerGame timer);
	}
	
	static public enum GAME_STATES{
		NOT_YET,
		RUNNING,
		FINISHED
	}
}
