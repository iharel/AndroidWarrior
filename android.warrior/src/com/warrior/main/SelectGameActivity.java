package com.warrior.main;

import com.androidWarrior.R;
import com.warrior.bluetooth.CommHandler;
import com.warrior.bluetooth.Sync;
import com.warrior.games.Game;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.games.activitys.GameActivity;
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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SelectGameActivity extends MyActivity implements OnClickListener {
	
	private Button butSpeedTestPhone,butSpeedClick,butSpeedCalculation;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_game);
		buildViewReference();
		buildViewEvent();
	}
	private void buildViewReference(){
		butSpeedTestPhone = (Button)findViewById(R.id.butSpeedTestPhone);
		butSpeedClick = (Button)findViewById(R.id.butSpeedClick);
		butSpeedCalculation = (Button)findViewById(R.id.butSpeedCalculation);
	}
	private void buildViewEvent(){
		butSpeedTestPhone.setOnClickListener(this);
		butSpeedCalculation.setOnClickListener(this);
		butSpeedClick.setOnClickListener(this);
	}
	public void onClick(View v) {
		Intent iGame = null;
		long typeGame = 0;
		switch(v.getId()){
			case R.id.butSpeedTestPhone:{
				iGame = new Intent(SelectGameActivity.this,SpeedTestPhoneGameActivity.class);
				typeGame = Game.SPEED_TEST_PHONE;
				break;
			}
			case R.id.butSpeedClick:{
				iGame = new Intent(SelectGameActivity.this,SpeedClickGameActivity.class);
				typeGame = Game.SPEED_CLICK;
				break;
			}
			case R.id.butSpeedCalculation:{
				iGame = new Intent(SelectGameActivity.this,SpeedCalculationGameActivity.class);
				typeGame = Game.SPEED_CALCULATION;
				break;
			}
		}
		selectGame(iGame, typeGame);
	}
	private void selectGame(Intent iGame,long typeGame ){
		MyApp app = (MyApp)SelectGameActivity.this.getApplication();
		CommHandler commHandler = app.getCommHandler();
		commHandler.writeToRmoteDevice(typeGame);
		Sync.waitTime(app.getTimeAir());
		app.setState(APP_STATES.RUN_GAME);
		startActivityForResult(iGame,MyApp.SEND_BLUETOOTH_DISCONNECTED);
	}
	protected void onResume() {
		super.onResume();
		MyApp app = (MyApp) getApplication();
		app.setState(APP_STATES.SELECT_GAME);
	}
	public void onBackPressed() {
		super.onBackPressed();
		MyApp app = (MyApp)getApplication();
		app.closeConnection();
	}
}
