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
import com.warrior.main.Game.IStartGame;
import com.warrior.main.Game.IStatusGame;

public class GameActivityClient extends Activity implements IEndGame, IStatusGame, IStartGame {

	private ProgressBar pbGame,pbWaitStart;
	public final static int RECEIVE_START = 102;
	public final static int SEND_DEVICE_WIN = 103;
	public final static int CLOSE_ACTIVITY = 104; 
	private CommHandler commHandler;
	private Game game;
	private boolean isStart = false;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_client);
		
		MyApp app = (MyApp)this.getApplication();
		commHandler = app.getCommHndler();
		long timeAir = app.getTimeAir();
			
		pbGame = (ProgressBar)findViewById(R.id.pbGame);
		pbWaitStart = (ProgressBar)findViewById(R.id.pbWaitStart);
			
		game = new Game(commHandler,timeAir);
		game.setListenerEndGame(this);
		game.setListenerStatusGame(this);
		game.setListenerStartGame(this);
		
		new WaitStart().execute();
	}
	public void endGame(boolean winner,long gameTime) {
		commHandler.writeToRmoteDevice(gameTime);
		Toast.makeText(this, game.checkWinning(winner), Toast.LENGTH_SHORT).show();
		finish();
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
				game.startGame();
				pbWaitStart.setVisibility(View.GONE);
				pbGame.setVisibility(View.VISIBLE);
			}
		}
	}
	public void startGame(boolean start) {
		isStart = start;
	}
	public void setIsStart(boolean isStart)
	{
		this.isStart = isStart;
	}
	public void statusGame(int status) {
		pbGame.setProgress(status);
	}
}
