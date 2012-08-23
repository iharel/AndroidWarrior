package com.warrior.games;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.warrior.main.CommHandler;
import com.warrior.main.CommHandler.IDataRecevieGame;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

abstract public class Game implements IDataRecevieGame{
	
	public final static long SEND_START_GAME = 102;
	public final static long RECEIVE_START_GAME = 102;
	public final static long SEND_FINISH_GAME = 103;
	public final static long RECEIVE_FINISH_GAME = 103;
	public final static long SEND_GAME_TIME = 104;
	public final static long RECEIVE_GAME_TIME = 104;
	public final static long SEND_ANOTHER_GAME = 105;
	public final static long RECEIVE_ANOTHER_GAME = 105;
	public final static long SEND_CLOSE_GAME = 106;
	public final static long RECEIVE_CLOSE_GAME = 106;
	
	protected boolean waitToReciveGameTime = false;
	protected long startTime,finishTime;
	protected long gameTimeMyDevice,gameTimeOtherDevice;
	protected CommHandler commHandler;
	protected long timeAir;
	
	protected IStateGame iStateGame;
	
	public Game(CommHandler commHandler,long timeAir){
		this.commHandler = commHandler;
		this.timeAir = timeAir;
		commHandler.setListenerDataRecevieGame(this);
	}
	public void startGame(){
		
	}
	protected void runningGame(){
		
	}
	protected void finishGame(){
		
	}
	public void dataReceiveBeforeGame(long data) {
		if(data == Game.RECEIVE_FINISH_GAME){
			Log.d("gal","the game is finished");
		}
		if(Game.RECEIVE_START_GAME == data){
			iStateGame.startGame(true);
		}
	}
	public void dataRecevieFinishGame(long data){
		Log.d("gal","Data when entering dataReceiveFinishGame: 	" + data);
		if(data == Game.RECEIVE_FINISH_GAME){
			Log.d("gal","the game is finished");
			return;
		}
		if(data == Game.RECEIVE_GAME_TIME){
			Log.d("gal","the game wait to receive game time");
			waitToReciveGameTime = true;
			return;
		}
		if(waitToReciveGameTime){
			Log.d("gal","the game receive game time");
			gameTimeOtherDevice = data;
			boolean resualt = checkWinning();
			iStateGame.finishGame(1,resualt);
			writeResultGame(resualt);
			waitToReciveGameTime = false;
		}
		if(data == Game.RECEIVE_ANOTHER_GAME){
			iStateGame.finishGame(2,false);
		}
		if(data == Game.RECEIVE_CLOSE_GAME){
			iStateGame.finishGame(3,false);
		}
	}
	protected boolean checkWinning()
	{
		// receive game time from other device 
		// check if device is winner
		boolean isWinner = true;
		if(gameTimeMyDevice > gameTimeOtherDevice)
		{
			isWinner = false;
		}
		
		return isWinner;
	}
	protected void resetGame(){
		Log.d("gal","inti resetGame");
		commHandler.setStateGame(GAME_STATES.NOT_YET);
		startTime = 0;
		finishTime = 0;
		gameTimeMyDevice = 0;
		gameTimeOtherDevice = 0;
	}
	protected void writeResultGame(boolean isWinner)
	{
		String filename = "GameResult.txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		try {
		    PrintWriter pw = new PrintWriter(new FileOutputStream(file,true));
		    pw.println("the type of game is:" + this.toString());
		    pw.println("the game time of this device is:" + gameTimeMyDevice);
		    pw.println("the game time of other device is:" + gameTimeOtherDevice);
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
	
	interface IStateGame
	{
		void startGame(boolean start);
		void myStatusGame(int status);
		void competitorStatusGame(int status);
		void finishGame(int state,boolean winner);
	}
	public void setListenerStateGame(IStateGame iStartGame)
	{
		this.iStateGame = iStartGame;
	}
	static public enum GAME_STATES{
		SYNC,
		NOT_YET,
		RUNNING,
		FINISHED
	}
}
