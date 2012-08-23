package com.warrior.games;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.SyncState;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidWarrior.*;
import com.warrior.games.Game.IStateGame;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.main.CommHandler;
import com.warrior.main.MyApp;

public class SpeedTestPhoneClient extends GameActivity{

	private ProgressBar myPbGame,competitorPbGame,pbWaitStart;
	private boolean isStart = false;
	private final static int RECEIVE_FINISH_GAME = 1,RECEIVE_ANOTHER_GAME = 2,RECEIVE_NOT_ANOTHER_GAME = 3;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speed_test_phone_client);
			
		myPbGame = (ProgressBar)findViewById(R.id.myPbGame);
		competitorPbGame = (ProgressBar)findViewById(R.id.competitorPbGame);
		pbWaitStart = (ProgressBar)findViewById(R.id.pbWaitStart);
		buildGame(timeAir);
		
		new WaitStart().execute();
	}
	public class WaitStart extends AsyncTask<Void,Void,Boolean>{
		protected Boolean doInBackground(Void... arg0) {
			// wait to start game
			while(!isStart){}
			return true;
		}
		protected void onPostExecute(Boolean result) {
			if(result)
			{
				Log.d("gal","into onPostExecute of WaitStart");
				game.startGame();
				pbWaitStart.setVisibility(View.GONE);
				myPbGame.setVisibility(View.VISIBLE);
				competitorPbGame.setVisibility(View.VISIBLE);
			}
		}
	}
	private void buildGame(long timeAir){
		game = new SpeedTestGame(commHandler,timeAir);
		game.setListenerStateGame(this);
	}
	public void startGame(boolean isStart) {
		this.isStart = isStart;
	}
	public void myStatusGame(int status) {
		myPbGame.setProgress(status);
	}
	public void competitorStatusGame(int status) {
		competitorPbGame.setProgress(status);
	}
	public void finishGame(int state,boolean isWinner) {
		switch(state){
			case RECEIVE_FINISH_GAME:{
				this.showWinning(isWinner);		
				break;
			}
			case RECEIVE_ANOTHER_GAME:{
				resetActivityGame();
				break;
			}
			case RECEIVE_NOT_ANOTHER_GAME:{
				finish();
				break;
			}
		}
	}

	protected void resetActivityGame(){
		Log.d("gal","entering resetActivityGame");
		game.resetGame();
		isStart = false;
		new WaitStart().execute();
		myPbGame.setProgress(0);
		myPbGame.setVisibility(View.GONE);
		competitorPbGame.setProgress(0);
		competitorPbGame.setVisibility(View.GONE);
		pbWaitStart.setVisibility(View.VISIBLE);
	}
}
