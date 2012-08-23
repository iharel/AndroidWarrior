package com.warrior.games;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

public class SpeedTestPhoneServer extends GameActivity{
	private Button butStart;
	private ProgressBar myPbGame,competitorPbGame; 
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speed_test_phone_server);
		
		
		butStart = (Button)findViewById(R.id.butStart);
		myPbGame = (ProgressBar)findViewById(R.id.myPbGame);
		competitorPbGame = (ProgressBar)findViewById(R.id.competitorPbGame);
		buildGame();
		
		butStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				commHandler.writeToRmoteDevice(SpeedTestGame.SEND_START_GAME);
				try {
					Thread.sleep(timeAir);
				} catch (InterruptedException e) {
					Log.d("gal",e.getMessage());
				}
				game.startGame();
				butStart.setEnabled(false);
			}
		});
	}
	protected void buildGame(){
		game = null;
		game = new SpeedTestGame(commHandler,timeAir);
		game.setListenerStateGame(this);
	}
	public void startGame(boolean start) {
		
	}
	public void myStatusGame(int status) {
		myPbGame.setProgress(status);
	}
	public void competitorStatusGame(int status) {
		competitorPbGame.setProgress(status);
	}
	
	
	protected Dialog onCreateDialog(int id, Bundle args) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("yow want to another game");
    	builder.setTitle("game");
    	// create buttons for dialog with inner class
    	builder.setPositiveButton("yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				commHandler.writeToRmoteDevice(SpeedTestGame.SEND_ANOTHER_GAME);
				resetActivityGame();
			}
		});
    	builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				commHandler.writeToRmoteDevice(SpeedTestGame.SEND_CLOSE_GAME);
				finish();
			}
		});
    	return  builder.create();
    }
	protected void resetActivityGame(){
		game.resetGame();
		myPbGame.setProgress(0);
		competitorPbGame.setProgress(0);
		butStart.setEnabled(true);
	}
	public void finishGame(int state, boolean isWinner) {
		this.showWinning(isWinner);
		showDialog(2);
	}
}
