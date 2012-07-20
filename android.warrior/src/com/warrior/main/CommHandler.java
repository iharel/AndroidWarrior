package com.warrior.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class CommHandler extends AsyncTask<Void, Long, Void>{

	private MainActivity main;
	private BluetoothSocket socket;
	private DataInputStream inStream;
	private DataOutputStream outStream;
	public final static long DISCONNECTED = 100;
	private IDataRecevie iDataRecevie;
	
	public CommHandler(MainActivity main,BluetoothSocket socket) throws IOException
	{
		this.main = main;
		this.socket = socket;
		inStream = new DataInputStream(socket.getInputStream());
		outStream = new DataOutputStream(socket.getOutputStream());
	}
	public void writeToRmoteDevice(Long data)
	{
		try {
			outStream.writeLong(data);

		} catch (IOException e) {
			Toast.makeText(main, e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
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
		while (!isCancelled()) {
			try {
				Long receiveValue = inStream.readLong();
				main.setReceiveTime(main.getTime());
				if(receiveValue == DISCONNECTED)
				{
					writeToRmoteDevice(DISCONNECTED);
					this.closeConnection();
					break;
				}
				
				Log.d("gal", "received message in time:  " + String.valueOf(main.getReceiveTime()));
				Log.d("gal","received message is: " + String.valueOf(receiveValue));
				publishProgress(receiveValue);
			} catch (IOException e) {
				Log.d("gal",e.getMessage());
			} catch (Exception e) {
				Log.d("gal",e.getMessage());
			}
		}
		return null;
	}
	protected void onProgressUpdate(Long... values) {
		iDataRecevie.dataRecevie(values[0]);
	}
	public interface IDataRecevie {
		void dataRecevie(long data);
	}
	public void setListenerDataRecevie(IDataRecevie iDataRecevie)
	{
		this.iDataRecevie = iDataRecevie;
	}
}
