package com.warrior.main;

import com.warrior.main.CommHandler.IDataRecevieOfGame;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

class Game extends AsyncTask<Void,Integer,Boolean> implements IDataRecevieOfGame{
		
	public final static int SEND_DEVICE_WIN = 103;
	public final static int RECEIVE_DEVICE_LOST = 103;
	private int receiveData = 0;
	private IEndGame iEndGame;
	private IStatusGame iStatusGame;
	private IStartGame iStartGame;
	private IReceiveGameTime iReceiveGameTime;
	private CommHandler commHandler;
	private long timeAir;
	private boolean isWinner = true;
	private final static int MAX_COUNTER = 1000000000;
	private long startTime,endTime,gameTime;
	
	public Game(CommHandler commHandler,long timeAir)
	{
		this.commHandler = commHandler;
		this.timeAir = timeAir;
		commHandler.setListenerDataRecevieOfGame(this);
	}
	public void startGame()
	{
		execute();
	}
	protected Boolean doInBackground(Void... arg0) {
		int counter = 1;
		int statusGame = 0;
		startTime = Sync.getTime();
		for(int i=0;i<MAX_COUNTER;i++)
		{
			if(receiveData == RECEIVE_DEVICE_LOST)
			{
				isWinner = false;
				break;
			}
			counter++;
			if(counter == (MAX_COUNTER/100))
			{
				counter = 1;
				publishProgress(++statusGame);
			}
		}
		endTime = Sync.getTime();
		gameTime = startTime - endTime;
		if(isWinner)
		{
			try {
				commHandler.writeToRmoteDevice(SEND_DEVICE_WIN);
				Thread.sleep(timeAir);
			} catch (InterruptedException e) {
				Log.d("gal",e.getMessage());
			}
		}
		return isWinner;
	}
		
	protected void onProgressUpdate(Integer... values) {
		iStatusGame.statusGame(values[0]);
	}
	protected void onPostExecute(Boolean result) {
		iEndGame.endGame(result,gameTime);
	}
	public String checkWinning(boolean winner)
	{
		String retValue;
		if(winner)
		{
			retValue = "I won";
		}
		else
		{
			retValue = "I lost";
		}
		return retValue;
	}
	public void dataRecevieOfGame(int data) {
		receiveData = data;
		switch(receiveData)
		{
			case GameActivityClient.RECEIVE_START:
			{
				iStartGame.startGame(true);
				break;
			}
			case GameActivityServer.RECEIVE_DEVICE_LOSE:
			{
				isWinner = false;
				break;
			}
			/*
			default:
			{
				// receive game time from client 
				// pass the time game of client to activity server for check winning 
				iReceiveGameTime.receiveGametime(receiveData);
			}*/
		}
	}
	interface IEndGame
	{
		void endGame(boolean winner,long gameTime);
	}
	public void setListenerEndGame(IEndGame iEndGame)
	{
		this.iEndGame = iEndGame;
	}
	
	interface IStartGame
	{
		void startGame(boolean start);
	}
	public void setListenerStartGame(IStartGame iStartGame)
	{
		this.iStartGame = iStartGame;
	}
	
	interface IStatusGame
	{
		void statusGame(int status);
	}
	public void setListenerStatusGame(IStatusGame iStatusGame)
	{
		this.iStatusGame = iStatusGame;
	}
	
	interface IReceiveGameTime
	{
		void receiveGametime(long time);
	}
	public void setListenerReceiveGametime(IReceiveGameTime iReceiveGameTime)
	{
		this.iReceiveGameTime = iReceiveGameTime;
	}
}



