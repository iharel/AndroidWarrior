package com.warrior.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class CommHandler extends AsyncTask<Void, Long, Void>{

	private BluetoothSocket socket;
	private DataInputStream inStream;
	private DataOutputStream outStream;
	public final static long DISCONNECTED = 100;
	private IDataRecevieOfGame iDataRecevieOfGame;
	private IDataRecevieOfSync iDataRecevieOfSync;
	Sync_State syncState = Sync_State.NOT_START;
	
	public CommHandler(BluetoothSocket socket) throws IOException
	{
		this.socket = socket;
		inStream = new DataInputStream(socket.getInputStream());
		outStream = new DataOutputStream(socket.getOutputStream());
	}
	public void writeToRmoteDevice(Long data) 
	{
		try {
			outStream.writeLong(data);
		} catch (IOException e) {
			Log.d("gal",e.getMessage());
		}
	}
	public void writeToRmoteDevice(int data) 
	{
		try {
			outStream.writeInt(data);
		} catch (IOException e) {
			Log.d("gal",e.getMessage());
		}
	}
	public void closeConnection() throws IOException
	{
		this.cancel(true);
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
		System.gc();
	}
	protected Void doInBackground(Void... obj) {
		switch (syncState) {
			case NOT_START:
			{
				syncState = Sync_State.RUNNING;
				sync();
				syncState = Sync_State.FINISH;
				break;
			}
			case FINISH:
			{
				new Game().execute();
				break;
			}
		}
		return null;
	}
	private void sync()
	{
		while (true) {
			try {
				Long receiveValue = inStream.readLong();
				Long receiveTime = Sync.getTime();
				if(receiveValue == DISCONNECTED)
				{
					writeToRmoteDevice(DISCONNECTED);
					this.closeConnection();
					break;
				}
				if(receiveValue == Sync.SYNC_FINISH)
				{
					publishProgress(receiveValue,receiveTime);
					break;
				}
				
				publishProgress(receiveValue,receiveTime);
			} catch (IOException e) {
				Log.d("gal",e.getMessage());
			} catch (Exception e) {
				Log.d("gal",e.getMessage());
			}
		}
	}
	protected void onProgressUpdate(Long... values) {
		iDataRecevieOfSync.dataRecevieOfSync(values);
		/*
		switch (syncState) {
			case RUNNING:
			{
				iDataRecevieOfSync.dataRecevieOfSync(values);
				break;
			}

			case FINISH:
			{
				break;
			}
		}*/
		
	}
	class Game extends AsyncTask<Void, Integer, Void>
	{
		protected Void doInBackground(Void... arg0) {
			while (true) {
				try {
					int receiveValue = inStream.readInt();
					if(receiveValue == DISCONNECTED)
					{
						writeToRmoteDevice(DISCONNECTED);
						closeConnection();
						break;
					}
					
					publishProgress(receiveValue);
				} catch (IOException e) {
					Log.d("gal",e.getMessage());
				} catch (Exception e) {
					Log.d("gal",e.getMessage());
				}
			}
			return null;
		}
		protected void onProgressUpdate(Integer... values) {
			iDataRecevieOfGame.dataRecevie(values[0]);
		}
		
	}
	public interface IDataRecevieOfGame {
		void dataRecevie(int data);
	}
	public void setListenerDataRecevie(IDataRecevieOfGame iDataRecevieOfGame)
	{
		this.iDataRecevieOfGame = iDataRecevieOfGame;
	}
	public interface IDataRecevieOfSync {
		void dataRecevieOfSync(Long[] values);
	}
	public void setListenerDataRecevieOfSync(IDataRecevieOfSync iDataRecevieOfSync)
	{
		this.iDataRecevieOfSync = iDataRecevieOfSync;
	}
}
