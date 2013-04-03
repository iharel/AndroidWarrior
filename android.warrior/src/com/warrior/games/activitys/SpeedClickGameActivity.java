package com.warrior.games.activitys;

import com.androidWarrior.R;
import com.warrior.games.Game.ITimerGame;
import com.warrior.games.SpeedClickGame;
import com.warrior.games.TimerGame;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.main.MyApp;

import android.os.Bundle;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpeedClickGameActivity extends GameActivity {
	private Button myButGame;
	
	private SpeedClickGame speedClickGame;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		speedClickGame = (SpeedClickGame)game;
	}
	protected void buildViewReference(){
		setContentView(R.layout.activity_speed_click);
		super.buildViewReference();
		myButGame = (Button)findViewById(R.id.myButGame);
	}
	protected void buildViewEvents(){
		super.buildViewEvents();
		myButGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!game.isPause()){
					speedClickGame.addCounterClick();
				}
			}
		});
	}
	protected void buildGame(){
		MyApp app = (MyApp)this.getApplication();
		game = new SpeedClickGame(app);
		super.buildGame();
	}
	@Override
	public void startGame() {
		myButGame.setEnabled(true);
	}
	public void myStatusGame(int myStatus) {
		super.myStatusGame(myStatus * 10);
	}
	public void competitorStatusGame(int competitorStatus) {
		super.competitorStatusGame(competitorStatus * 10);
	}
	@Override
	public void finishGame(boolean isWinner) {
		super.finishGame(isWinner);
		myButGame.setEnabled(false);
	}
	protected void resetGameActivity(){
		super.resetGameActivity();
	}
}
