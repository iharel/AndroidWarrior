package com.warrior.games.activitys;

import com.androidWarrior.R;
import com.warrior.bluetooth.Sync;
import com.warrior.games.Game;
import com.warrior.games.Game.ITimerGame;
import com.warrior.games.LaunchingGame;
import com.warrior.games.MusicGame;
import com.warrior.games.TimerGame;
import com.warrior.games.LaunchingGame.ILaunchingGameMode;
import com.warrior.games.LaunchingGame.LAUNCHING_GAME_MODE;
import com.warrior.games.SpeedTestPhoneGame;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.main.MyActivity;
import com.warrior.main.MyFacebook;
import com.warrior.main.MyApp;
import com.warrior.main.MyApp.APP_STATES;
import com.warrior.games.Game.IStateGame;
import com.warrior.games.activitys.MenuGamePopup.IClickMenuGame;
import com.warrior.games.activitys.MenuGamePopup.TYPE_CLICK;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class GameActivity extends MyActivity 
	implements  IStateGame,IClickMenuGame,ILaunchingGameMode, ITimerGame{

	protected Game game;
	private MenuGamePopup menuPopup;
	private MusicGame music;
	private TextView tvLaunchingGameMode,tvTimerGame;
	private View container;
	protected ProgressBar myPbStatus,competitorPbStatus;
	private static int RED;
	private static int GREEN;
	private static int YELLOW;
	private static int BLACK;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		menuPopup = new MenuGamePopup(this);
		menuPopup.setOnClickButton(this);
		music = new MusicGame(this);
		buildViewReference();
		buildViewEvents();
		buildGame();
		resetGameActivity();
		RED = getResources().getColor(R.color.red);
		GREEN = getResources().getColor(R.color.green);
		YELLOW = getResources().getColor(R.color.yellow);
		BLACK = getResources().getColor(R.color.black);
	}
	protected void buildViewReference(){
		container = findViewById(R.id.container);
		tvLaunchingGameMode = (TextView)findViewById(R.id.tvLaunchingGameMode);
		tvTimerGame = (TextView)findViewById(R.id.tvTimerGame);
		myPbStatus= (ProgressBar)findViewById(R.id.myPbStatus);
		competitorPbStatus= (ProgressBar)findViewById(R.id.competitorPbStatus);
	}
	protected void buildViewEvents(){
		
	}
	protected void onPause() {
		if(game.getState() == GAME_STATES.RUNNING){
			game.setPause(true);
		}
		super.onPause();
	}
	protected void onResume() {
		super.onResume();
		if(game.getState() == GAME_STATES.RUNNING){
			if(!menuPopup.isShowing()){
				menuPopup.show();
			}
		}
	}
	protected void buildGame(){
		MyApp app = (MyApp)getApplication();
		app.setState(APP_STATES.RUN_GAME);
		game.setTimerGameListener(this);
		game.setStateGameListener(this);
	}
	public void readyGame(){
		Log.d("gal","game is ready");
		LaunchingGame jumpGame = new LaunchingGame();
		jumpGame.setListenerIJumpGame(this);
		jumpGame.startJump();
	}
	public void startGame() {

	}
	public void pauseGame() {
		music.pause();
	}
	public void resumeGame() {
		if(menuPopup.isShowing()){
			menuPopup.dismiss();
		}
		music.resume();
	}
	protected void showWinningAfterFinish(boolean isWinner){
		if(isWinner){
			container.setBackgroundColor(GREEN);
			myPbStatus.setProgress(myPbStatus.getMax());
		}
		else{
			container.setBackgroundColor(RED);
			competitorPbStatus.setProgress(myPbStatus.getMax());
		}
	}
	public void myStatusGame(int myStatus) {
		int competitorStatus = competitorPbStatus.getProgress();
		myPbStatus.setProgress(myStatus);
		showWinningOnline(myStatus, competitorStatus);
	}
	public void competitorStatusGame(int competitorStatus) {
		int myStatus = myPbStatus.getProgress();
		competitorPbStatus.setProgress(competitorStatus);
		showWinningOnline(myStatus, competitorStatus);
	}
	public void rematchGame() {
		resetGameActivity();
	}
	protected void resetGameActivity(){
		myPbStatus.setProgress(0);
		competitorPbStatus.setProgress(0);;
		container.setBackgroundColor(BLACK);
		tvTimerGame.setText("00:00:00");
		MyApp app = (MyApp)getApplication();
		if(app.isSever()){
			game.setReady(true);
		}
	}
	public void finishGame(boolean isWinner) {
		showWinningAfterFinish(isWinner);
		if(isWinner){
			menuPopup.show();
		}
	}
	public void closeGame(){
		music.stop();
		setResult(RESULT_OK);
		this.finish();
	}
	public void onBackPressed() {
		if(game.getState() == GAME_STATES.RUNNING){
			if(!game.isPause()){
				game.setPause(true);
				menuPopup.show();
			}
		}
		
		else if(game.getState() == GAME_STATES.FINISHED){
			if(!menuPopup.isShowing() && game.isWinner()){
				menuPopup.show();
			}
		}
	}
	public void selectOption(TYPE_CLICK type) {
		switch(type){
			case uploadPost:{
				if(game.getState() == GAME_STATES.FINISHED){
					MyApp app = (MyApp)getApplication();
					MyFacebook fb = app.getFacebook();
					if(fb.isOpendConnection()){
						//fb.writePost("gal lavie");
						fb.showUploadPostDialog(GameActivity.this);
					}
				}
				break;
			}
			case resume:{
				if(game.getState() == GAME_STATES.RUNNING){
					menuPopup.dismiss();
					game.setPause(false);
				}
				break;
			}
			case rematch:{
				if(game.getState() == GAME_STATES.FINISHED){
					game.setRematch(true);
					menuPopup.dismiss();
				}
				break;
			}
			case close:{
				menuPopup.dismiss();
				game.setClose(true);
				break;
			}
		}
	}
	protected void showWinningOnline(int myStatus,int competitorStatus){
		if(competitorStatus > myStatus){
			container.setBackgroundColor(RED);
			
		}
		else if(competitorStatus < myStatus){
			container.setBackgroundColor(GREEN);
		}
		else{
			container.setBackgroundColor(YELLOW);
		}
	}
	
	public void gameTimeChanged(String time, TimerGame timer) {
		tvTimerGame.setText(time);
	}
	@Override
	public void launchingGameModeChanged(LAUNCHING_GAME_MODE mode) {
		switch (mode) {
			case ready:{
				music.start();
				tvLaunchingGameMode.setTextColor(getResources().getColor(R.color.red));
				tvLaunchingGameMode.setText("ready");
				tvLaunchingGameMode.setVisibility(View.VISIBLE);
				break;
			}
			case set:{
				tvLaunchingGameMode.setText("set");
				tvLaunchingGameMode.setTextColor(getResources().getColor(R.color.yellow));
				break;
			}
			case go:{
				tvLaunchingGameMode.setText("go");
				tvLaunchingGameMode.setTextColor(getResources().getColor(R.color.green));
				game.start();
				break;
			}
			case afterGo:{
				tvLaunchingGameMode.setVisibility(View.GONE);
				break;
			}
		}
	}
}
