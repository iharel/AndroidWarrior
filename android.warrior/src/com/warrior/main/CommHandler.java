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
	Thread mainThread;
	public CommHandler(MainActivity main,BluetoothSocket socket) throws IOException
	{
		 //System.nanoTime()
		 this.main = main;
		this.socket = socket;
		inStream = socket.getInputStream();
		outStream = socket.getOutputStream();
		mainThread = Thread.currentThread();
		
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
		long closeConnection = 1;
		writeToRmoteDevice(convertLongToArrayBytes(closeConnection));
		socket.close();
		// write to remote device close connection
	}
	protected Void doInBackground(Void... obj) {
		running = true;
		byte[] buffer = new byte[1024];
		while (running) {
			try {
				inStream.read(buffer);
				if(convertArrayBytesToLong(buffer) == 1)
				{
					socket.close();
					break;
				}
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
		main.dataRecive(values[0]);
	}
	public byte[] convertLongToArrayBytes(long data)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeLong(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	private long convertArrayBytesToLong(byte[] data)
	{
		Long value = (long) 0;
		for (int i = 0; i < data.length; i++)
		{
		   value += ((long) data[i] & 0xffL) << (8 * i);
		}
		return value;
	}
}
