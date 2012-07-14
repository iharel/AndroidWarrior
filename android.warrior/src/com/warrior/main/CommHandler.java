package com.warrior.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;


import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class CommHandler extends AsyncTask<Void, Long, Void>{

	private MainActivity main;
	BluetoothSocket socket;
	DataInputStream inStream;
	DataOutputStream outStream;
	boolean running = false;
	public CommHandler(MainActivity main,BluetoothSocket socket) throws IOException
	{
		 //System.nanoTime()
		 this.main = main;
		this.socket = socket;
		inStream = new DataInputStream(socket.getInputStream());
		outStream = new DataOutputStream(socket.getOutputStream());
	}
	public boolean getRunning()
	{
		return running;
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
		socket.close();
	}
	protected Void doInBackground(Void... obj) {
		running = true;
		byte[] buffer = new byte[1024];
		while (running) {
			try {
				Long receiveValue = inStream.readLong();
				main.setReceiveTime(main.getTime());
				Log.d("gal", "received message in time:  " + String.valueOf(main.getReceiveTime()));
				Log.d("gal","received message is: " + String.valueOf(receiveValue));

				publishProgress(receiveValue);
			} catch (IOException e) {
				Log.d("gal",e.getMessage());
			} catch (Exception e) {
				Log.d("gal",e.getMessage());
			}
		}
		running = false;
		return null;
	}
	protected void onProgressUpdate(Long... values) {
		main.dataReceive(values[0]);
	}
}
