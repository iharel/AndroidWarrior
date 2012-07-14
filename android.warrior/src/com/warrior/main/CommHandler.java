package com.warrior.main;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class CommHandler extends AsyncTask<Void, byte[], Void>{

	private MainActivity main;
	BluetoothSocket socket;
	InputStream inStream;
	OutputStream outStream;
	boolean running = false;
	public CommHandler(MainActivity main,BluetoothSocket socket) throws IOException
	{
		 //System.nanoTime()
		 this.main = main;
		this.socket = socket;
		inStream = socket.getInputStream();
		outStream = socket.getOutputStream();
	}
	public boolean getRunning()
	{
		return running;
	}
	public void writeToRmoteDevice(byte[] data)
	{
		try {
			for(int i=0;i<data.length;i++)
			{
				outStream.write(data[i]);
			}
		} catch (IOException e) {
			Toast.makeText(main, e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
	}
	public void closeConnection() throws IOException
	{
		socket.close();
	}
	protected Void doInBackground(Void... obj) {
		running = true;
		byte[] buffer = new byte[1024];
		while (running) {
			try {
				inStream.read(buffer);
				main.setReceiveTime(System.nanoTime());
				publishProgress(buffer);
			} catch (IOException e) {
				Log.d("gal",e.getMessage());
			} catch (Exception e) {
				Log.d("gal",e.getMessage());
			}
		}
		running = false;
		return null;
	}
	protected void onProgressUpdate(byte[]... values) {
		main.dataReceive(values[0]);
	}
}
