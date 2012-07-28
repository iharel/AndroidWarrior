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
	private boolean isEndSync = false;
	
	public CommHandler(BluetoothSocket socket) throws IOException
	{
		this.socket = socket;
		inStream = new DataInputStream(socket.getInputStream());
		outStream = new DataOutputStream(socket.getOutputStream());
	}
	public void setIsEndSync(boolean endSync)
	{
		this.isEndSync = endSync;
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
		sync();
		Log.d("gal","before game start");
		new Game().execute();
		Log.d("gal","the end of thread sync");
		return null;
	}
	private void sync()
	{
		while (true) {
			try {
				if(isEndSync)
				{
					return;
				}
				Long receiveValue = inStream.readLong();
				Long receiveTime = Sync.getTime();
				if(receiveValue == DISCONNECTED)
				{
					writeToRmoteDevice(DISCONNECTED);
					this.closeConnection();
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
			Log.d("gal","the data is:" + values[0]);
			iDataRecevieOfGame.dataRecevieOfGame(values[0]);
		}
		
	}
	public interface IDataRecevieOfGame {
		void dataRecevieOfGame(int data);
	}
	public void setListenerDataRecevieOfGame(IDataRecevieOfGame iDataRecevieOfGame)
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
