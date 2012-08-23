package com.warrior.games;

import com.warrior.games.Game.IStateGame;
import com.warrior.main.CommHandler;
import com.warrior.main.MyApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public abstract class GameActivity extends Activity implements  IStateGame{

	protected Game game;
	protected CommHandler commHandler;
	protected long timeAir;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApp app = (MyApp)this.getApplication();
		commHandler = app.getCommHndler();
		timeAir = app.getTimeAir();
	}
	protected void buildGame(){
		
	}
	public void startGame(boolean start) {
		
	}
	public void finishGame(int state,boolean isWinner) {
		
	}
	protected void showWinning(boolean isWinner){
		String str = "you won";
		if(!isWinner)
		{
			str = "you lost";
		}
		Toast.makeText(this,str , Toast.LENGTH_SHORT).show();
	}
	protected void resetActivityGame(){
		game.resetGame();
	}
	
}
