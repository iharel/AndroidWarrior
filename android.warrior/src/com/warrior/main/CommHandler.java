package com.warrior.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.warrior.games.Game.GAME_STATES;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class CommHandler{

	private BluetoothSocket socket;
	private DataInputStream inStream;
	private DataOutputStream outStream;
	private IDataRecevieGame iDataRecevieGame;
	private IDataRecevieSync iDataRecevieSync;
	private GAME_STATES state;
	private boolean isConnected = false;
	public final static long DISCONNECTED = 1000;
	
	public CommHandler(BluetoothSocket socket) throws IOException
	{
		this.socket = socket;
		inStream = new DataInputStream(socket.getInputStream());
		outStream = new DataOutputStream(socket.getOutputStream());
		state = GAME_STATES.SYNC;
		isConnected = true;
	}
	public boolean getIsConnected(){
		return isConnected;
	}
	public void setStateGame(GAME_STATES state){
		this.state = state;
	}
	public void writeToRmoteDevice(Long data) 
	{
		try {
			outStream.writeLong(data);
			outStream.flush();
			Log.d("gal","send data: " + data);

		} catch (IOException e) {
			Log.d("gal",e.getMessage());
		} catch (NullPointerException e) {
			Log.d("gal",e.getMessage());
		}
	
	}
	public void closeConnection() throws IOException
	{
		Log.d("gal","into connection close");
		if(inStream != null)
		{
			inStream.close();
			inStream = null;
			
		}
		if(outStream != null)
		{
			outStream.close();
			outStream = null;
		}
		if(socket != null)
		{
			socket.close();
			socket = null;
		}
		isConnected = false;
	}
	public void startReceiveData()
	{
		new ReceiveData().execute();
	}
	class ReceiveData extends AsyncTask<Void, Long, Void>{

		protected Void doInBackground(Void... params) {
			Long receiveValue,receiveTime;
			while (isConnected) {
				try {
					receiveValue = inStream.readLong();
					receiveTime = Sync.getSystemTime();
					publishProgress(receiveValue,receiveTime);
					Log.d("gal","the receiveValue is:" + receiveValue);
				 } catch (IOException e) {
					Log.d("gal",e.getMessage());
				}
			}
			try{
				closeConnection();
			} catch (Exception e) {
				Log.d("gal",e.getMessage());
			}
			return null;
		}
		protected void onProgressUpdate(Long... values) {
			long receiveValue = values[0];
			if(receiveValue == DISCONNECTED)
			{
				CommHandler.this.writeToRmoteDevice(DISCONNECTED);
				isConnected = false;
				return;
			}
			switch(state){
				case SYNC:{
					iDataRecevieSync.dataRecevieSync(values);
					break;
				}
				case NOT_YET:{
					iDataRecevieGame.dataReceiveBeforeGame(receiveValue);
					Log.d("gal","in following state: NOT_YET" );
					break;
				}
				case RUNNING:{
					iDataRecevieGame.dataRecevieInGame(receiveValue);
					Log.d("gal","in following state: RUNNING" );
					break;
				}
				case FINISHED:{
					iDataRecevieGame.dataRecevieFinishGame(receiveValue);
					Log.d("gal","in following state: FINISHED" );
					break;
				}
			}
			
		}
	}
	public interface IDataRecevieSync {
		void dataRecevieSync(Long[] values);
	}
	public void setListenerDataRecevieSync(IDataRecevieSync iDataRecevieSync)
	{
		this.iDataRecevieSync = iDataRecevieSync;
	}
	
	public interface IDataRecevieGame {
		void dataReceiveBeforeGame(long data);
		void dataRecevieInGame(long data);
		void dataRecevieFinishGame(long data);
	}
	public void setListenerDataRecevieGame(IDataRecevieGame iDataRecevieGame)
	{
		this.iDataRecevieGame = iDataRecevieGame;
	}
	public GAME_STATES getStateGame() {
		// TODO Auto-generated method stub
		return this.state;
	}
}
