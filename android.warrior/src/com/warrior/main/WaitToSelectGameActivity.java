package com.warrior.main;

import com.androidWarrior.R;
import com.warrior.games.Game;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.games.activitys.SpeedCalculationGameActivity;
import com.warrior.games.activitys.SpeedClickGameActivity;
import com.warrior.games.activitys.SpeedTestPhoneGameActivity;
import com.warrior.main.MyApp.APP_STATES;
import com.warrior.main.MyApp.ISelectGame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WaitToSelectGameActivity extends MyActivity implements ISelectGame{

	private boolean waitToSelectGame = true;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wait_to_select_game);
		MyApp app = (MyApp)this.getApplication();
		app.setDataReceiveSelectGameListener(this);
	}
	protected void onResume() {
		super.onResume();
		waitToSelectGame = true;
		MyApp app = (MyApp)this.getApplication();
		app.setState(APP_STATES.SELECT_GAME);
		new WaitSelectGame().execute();
	}
	public void onBackPressed() {
		super.onBackPressed();
		MyApp app = (MyApp)getApplication();
		app.closeConnection();
	}
	public class WaitSelectGame extends AsyncTask<Void,Void,Boolean>{
		protected Boolean doInBackground(Void... arg0) {
			while(waitToSelectGame){}
			return waitToSelectGame;
		}
	}
	public void dataReceiveSelectGame(Long data) {
		if(data == Game.SPEED_TEST_PHONE){
			waitToSelectGame = false;
			Intent iGame = new Intent(WaitToSelectGameActivity.this
					,SpeedTestPhoneGameActivity.class);
			startActivityForResult(iGame,MyApp.SEND_BLUETOOTH_DISCONNECTED);
		}
		else if(data == Game.SPEED_CLICK){
			waitToSelectGame = false;
			Intent iGame = new Intent(WaitToSelectGameActivity.this
					,SpeedClickGameActivity.class);
			startActivityForResult(iGame,MyApp.SEND_BLUETOOTH_DISCONNECTED);
		}
		else if(data == Game.SPEED_CALCULATION){
			waitToSelectGame = false;
			
			Intent iGame = new Intent(WaitToSelectGameActivity.this
					,SpeedCalculationGameActivity.class);
			startActivityForResult(iGame,MyApp.SEND_BLUETOOTH_DISCONNECTED);
		}
	}
}
