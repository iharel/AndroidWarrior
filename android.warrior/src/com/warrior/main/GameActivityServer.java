package com.warrior.main;

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
import com.warrior.main.CommHandler.IDataRecevieOfGame;
import com.warrior.main.Game.IEndGame;
import com.warrior.main.Game.IReceiveGameTime;
import com.warrior.main.Game.IStatusGame;

public class GameActivityServer extends Activity implements IEndGame, IStatusGame, IReceiveGameTime {

	private CommHandler commHandler;
	private Button butStart;
	private ProgressBar pbGame;
	public final static int SEND_START = 102;
	public final static int RECEIVE_START = 102;
	public final static int SEND_DEVICE_WIN = 103;
	public final static int RECEIVE_DEVICE_LOSE = 103;
	public final static int CLOSE_ACTIVITY = 104; 
	private Game game;
	private long timeAir;
	long gameTimeServer,gameTimeClient;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_server);
		
		MyApp app = (MyApp)this.getApplication();
		commHandler = app.getCommHndler();
		timeAir = app.getTimeAir();
			
		butStart = (Button)findViewById(R.id.butStart);
		pbGame = (ProgressBar)findViewById(R.id.pbGame);
			
		game = new Game(commHandler,timeAir);
		game.setListenerEndGame(this);
		game.setListenerStatusGame(this);
		game.setListenerReceiveGametime(this);
		
		butStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
					commHandler.writeToRmoteDevice(SEND_START);
					try {
						Thread.sleep(timeAir);
					} catch (InterruptedException e) {
						Log.d("gal",e.getMessage());
					}
					game.startGame();
					pbGame.setVisibility(View.VISIBLE);
					butStart.setEnabled(false);
				}
			});
	}
	public void endGame(boolean winner,long gameTime) {
		gameTimeServer = gameTime;
		Toast.makeText(this, game.checkWinning(winner), Toast.LENGTH_SHORT).show();
		finish();
	}
	public void statusGame(int status) {
		pbGame.setProgress(status);
	}
	public void receiveGametime(long time) {
		gameTimeClient = time;
		long result = gameTimeServer - gameTimeClient;
		if(result>0)
		{
			Toast.makeText(this, game.checkWinning(true), Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(this, game.checkWinning(false), Toast.LENGTH_SHORT).show();
		}
	}
}
