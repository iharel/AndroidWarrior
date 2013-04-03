package com.warrior.games;

import java.util.IllegalFormatCodePointException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LaunchingGame implements Runnable {

	private static final int MAX_CONTER_SECOEND = 4;
	private static final int TIME_SLEEP = 1000;
	private ILaunchingGameMode iLaunchingGameMode;
	private Message message;
	private Handler handler;
	private static final String KEY_STATE_JUMP_GAME = "stateJumpGame";
	public LaunchingGame(){
		buildHandler();
	}
	private void buildHandler(){
		handler = new Handler(){
			 public void handleMessage(Message msg) {
		            int stateJumpGame = msg.getData().getInt(KEY_STATE_JUMP_GAME,0);
		            switch(stateJumpGame){
			            case 0:{
			            	iLaunchingGameMode.launchingGameModeChanged(LAUNCHING_GAME_MODE.ready);
			            	break;
			            }
			            case 1:{
			            	iLaunchingGameMode.launchingGameModeChanged(LAUNCHING_GAME_MODE.set);
			            	break;
			            }
			            case 2:{
			            	iLaunchingGameMode.launchingGameModeChanged(LAUNCHING_GAME_MODE.go);
			            	break;
			            }
			            case 3:{
			            	iLaunchingGameMode.launchingGameModeChanged(LAUNCHING_GAME_MODE.afterGo);
			            	break;
			            }
		            }
		        }
		};
	}
	public void startJump(){
		Thread thread = new Thread(this);
		thread.start();
	}
	public void run() {
		for(int i=0;i<MAX_CONTER_SECOEND;i++){
			try {
				message = handler.obtainMessage();
				Bundle bundle = new Bundle();
				bundle.putInt(KEY_STATE_JUMP_GAME, i);
				message.setData(bundle);
				message.sendToTarget();
				Thread.sleep(TIME_SLEEP);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public interface ILaunchingGameMode{
		void launchingGameModeChanged(LAUNCHING_GAME_MODE mode);
	}
	public void setListenerIJumpGame(ILaunchingGameMode iLaunchingGameMode){
		this.iLaunchingGameMode = iLaunchingGameMode;
	}
	public enum LAUNCHING_GAME_MODE{
		ready,
		set,
		go,
		afterGo;
	}
	
}
