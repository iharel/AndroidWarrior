package com.warrior.bluetooth;

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

public class CommHandler
{

	private BluetoothSocket socket;
	private DataInputStream inStream;
	private DataOutputStream outStream;
	private IDateReceive iDateReceive;
	private boolean isConnected = false;
	
	public final static long DISCONNECTED = 10000;

	public CommHandler(BluetoothSocket socket) throws IOException
	{
		this.socket = socket;
		inStream = new DataInputStream(socket.getInputStream());
		outStream = new DataOutputStream(socket.getOutputStream());
		isConnected = true;
	}
	public void setDateReceiveListener(IDateReceive iDateReceive) {
		this.iDateReceive = iDateReceive;
	}
	public void writeToRmoteDevice(Long data) 
	{
		try {
			outStream.writeLong(data);
			outStream.flush();
			Log.d("gal","send data: " + data);

		} catch (IOException e) {
			Log.d("gal","IO exeption when writing to remote device " + e.getMessage());
		} catch (NullPointerException e) {
			Log.d("gal","Null pointer exeption when writing to remote device " + e.getMessage());
		}

	}
	public void closeConnection()
	{
		try {

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
		} catch (IOException e) {
			Log.d("gal","Exception while trying to close connection: " + e.getMessage());// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void startReceiveDataListener()
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
				Log.d("gal","Closing the receive data connection " + e.getMessage());
			}
			return null;
		}
		protected void onProgressUpdate(Long... values) {
			iDateReceive.dataReceive(values);

		}
	}
	
	public interface IDateReceive{
		void dataReceive(Long[] values);
	}
	

}
