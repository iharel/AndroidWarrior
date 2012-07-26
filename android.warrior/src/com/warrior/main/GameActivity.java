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

public class GameActivity extends Activity implements IDataRecevieOfGame {

	private CommHandler commHandler;
	private Button butStart,butExit;
	private ProgressBar pbGame,pbWaitStart;
	private long receiveData = 0;
	public final static int SEND_START = 102;
	public final static int RECEIVE_START = 102;
	public final static int SEND_WIN = 103;
	public final static int RECEIVE_LOSE = 104; 
	private Game_State gameState;
	long timeAir;
	private boolean isServer;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		MyApp app = (MyApp)this.getApplication();
		commHandler = app.getCommHndler();
		if(commHandler != null)
		{
			Log.d("gal","the commHandler is null");
		}
		timeAir = savedInstanceState.getLong("timeAir");
		isServer = savedInstanceState.getBoolean("isServer");
		Log.d("gal","isServer:" + String.valueOf(isServer));
		Log.d("gal","timeAir:" + String.valueOf(timeAir));
		commHandler.setListenerDataRecevie(this);
		buildViewReference();
		buildEvents();
	}
	public void buildViewReference()
	{
		butStart = (Button)findViewById(R.id.butStart);
		butExit = (Button)findViewById(R.id.butExit);
		pbGame = (ProgressBar)findViewById(R.id.pbGame);
		pbWaitStart = (ProgressBar)findViewById(R.id.pbWaitStart);
	}
	private void buildEvents()
	{
		if(isServer)
		{
			butStart.setEnabled(true);
			butStart.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					commHandler.writeToRmoteDevice(SEND_START);
					try {
						Thread.sleep(timeAir);
					} catch (InterruptedException e) {
						Log.d("gal",e.getMessage());
					}
					new Game().startGame();
					butStart.setEnabled(false);
				}
			});
		}
		else
		{
			new WaitStart().execute();
		}
		butExit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				commHandler.writeToRmoteDevice(CommHandler.DISCONNECTED);
				finish();
			}
		});
	}
	class Game extends AsyncTask<Void,Integer,Void>{
		
		public void startGame()
		{
			gameState = Game_State.RUNNING;
			pbGame.setVisibility(View.VISIBLE);
			execute();
		}
		protected Void doInBackground(Void... arg0) {
			int counter = 1;
			for(int i=0;i<1000000;i++)
			{
				if(receiveData == RECEIVE_LOSE)
				{
					break;
				}
				counter++;
				if(counter == 10000)
				{
					counter = 1;
					publishProgress(counter);
				}
			}
			checkDeviceWinning();
			butStart.setEnabled(true);
			return null;
		}
		
		protected void onProgressUpdate(Integer... values) {
			int i = pbGame.getProgress();
			pbGame.setProgress(++i);
		}
		private void checkDeviceWinning()
		{
			switch (gameState) {
			case RUNNING:
			{
				// I lose
				Toast.makeText(GameActivity.this, "I lost", Toast.LENGTH_SHORT).show();
				break;
			}
			case FINISH:
			{
				// I win
				try {
					Thread.sleep(timeAir);
					commHandler.writeToRmoteDevice(SEND_WIN);
					Toast.makeText(GameActivity.this, "I winning", Toast.LENGTH_SHORT).show();
				} catch (InterruptedException e) {
					Log.d("gal",e.getMessage());
				}
				break;
			}
		}
		}
	}
	public class WaitStart extends AsyncTask<Void,Void,Boolean>{
		protected Boolean doInBackground(Void... arg0) {
			// wait to start game
			while(receiveData == RECEIVE_START){}
			return true;
		}
		protected void onPostExecute(Boolean result) {
			if(result)
			{
				new Game().startGame();
				pbWaitStart.setVisibility(View.GONE);
			}
		}
		
	}
	private enum Game_State
	{
		NOT_START,
		RUNNING,
		FINISH
	}
	public void dataRecevie(int data) {
		receiveData = data;
	}
}
