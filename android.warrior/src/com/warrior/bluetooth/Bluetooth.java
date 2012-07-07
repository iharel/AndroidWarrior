package com.warrior.bluetooth;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.UUID;

import com.gal.bluetooth1.R;
import com.warrior.main.MainActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.sax.StartElementListener;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SlidingDrawer;
import android.widget.Toast;

public class Bluetooth {
	private BluetoothAdapter btAdapter;
	private Context context;
	private final static int ALWAYS_DISCOVERY = 0;
	private final UUID UUID_RFCOMM_GENERIC = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	Server btServer;
	Client btClient;
	Socket btSocket;
	MainActivity main = (MainActivity)context;


	public Bluetooth(Context context)
	{
		this.context = context;
		btAdapter= BluetoothAdapter.getDefaultAdapter();
		btServer = new Server();
		btClient = new Client();
		btSocket = new Socket();

		if(btAdapter == null)
		{
			// the device is not support in bluetooth
		}
	}
	public void enableBluetooth()
	{
		Intent iEnableBluetooth = new Intent(btAdapter.ACTION_REQUEST_DISCOVERABLE);
		iEnableBluetooth.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,ALWAYS_DISCOVERY);
		context.startActivity(iEnableBluetooth);
	}
	public void disableBluetooth()
	{
		if(btAdapter.isEnabled())
		{
			btAdapter.disable();
			btAdapter.cancelDiscovery();
			if(btServer.getStatus() == Status.RUNNING)
			{
				btServer.closeConnection();
			}
			if(btClient.getStatus() == Status.RUNNING)
			{
				btClient.closeConnection();
				btClient.cancel(true);
			}
			if(btSocket.getStatus() == Status.RUNNING)
			{
				btSocket.closeConnection();
				btSocket.cancel(true);
			}
		}
	}
	public void startScanning()
	{
		btAdapter.startDiscovery();
	}
	// this is server side
	public class Server extends AsyncTask<Void, Void, Void>
	{
		private BluetoothServerSocket serverSocket;
		final static String NAME = "warroir";
		BluetoothSocket socket = null; 
		protected void onPreExecute(){
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(NAME, UUID_RFCOMM_GENERIC);
			} catch (IOException e) {
				Toast.makeText(context, "you have problam in bluetooth", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		protected Void doInBackground(Void... arg0) {
			try {
				socket = serverSocket.accept();
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					btAdapter.cancelDiscovery();
					btSocket.execute(socket);
				}
			} catch (IOException e) {
				Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
			}
			catch (Exception e) {
				Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
			}
			return null;
		}
		public void closeConnection()
		{
			try {
				if(socket != null)
				{
					socket.close();
					Toast.makeText(context, "the connection is colsed by the server", Toast.LENGTH_SHORT).show();
				}
			} catch (IOException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}
	public class Client extends AsyncTask<BluetoothDevice, Void, Boolean>
	{
		private BluetoothSocket socket;
		protected Boolean doInBackground(BluetoothDevice... devices) {
			boolean retValue = true;
			try {
				// create socket with other device
				socket = devices[0].createRfcommSocketToServiceRecord(UUID_RFCOMM_GENERIC);
				btAdapter.cancelDiscovery();
				socket.connect();

			} catch (IOException e) {
				// service discovery failed
				Log.d("gal",e.getMessage());
			}
			btSocket.execute(socket);
			return retValue;
		}
		protected void onPostExecute(boolean result) {
			if(result)
			{
				Log.d("gal","start onPostExecute");

				return;
			}
			Toast.makeText(context,"the connection faild", Toast.LENGTH_SHORT).show();

		}
		protected void onCancelled() {
			Log.d("gal","the client is died");
		}
		public void closeConnection()
		{
			try {
				if(socket != null)
				{
					socket.close();
					Toast.makeText(context, "the connection is colsed by the client", Toast.LENGTH_SHORT).show();
				}
			} catch (IOException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
				return;
			}
		}

	}
	// this is connection between server and client
	public class Socket extends AsyncTask<BluetoothSocket,String,Void>
	{
		private InputStream inStream;
		private OutputStream outStream;
		private boolean running = true;
		private BluetoothSocket socket;
		protected Void doInBackground(BluetoothSocket... sockets) {
			socket = sockets[0];
			byte[] buffer = new byte[1024];  // buffer store for the stream
			btServer.cancel(true);
			btClient.cancel(true);
			try {
				inStream = socket.getInputStream();
				outStream = socket.getOutputStream();
				while (running) {
					inStream.read(buffer);
					// get string
					main.T3 = System.nanoTime();
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(bos);
					try {
						dos.writeLong(main.T3);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String data = new String(buffer);
					publishProgress(data);

					// get integer
					//int i = buffer[0];
					//publishProgress(String.valueOf(i));
				}
			} catch (IOException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		protected void onProgressUpdate(long... value) {
			if (main.getIsServer())
			{
				switch(main.getState())
				{
					case 0:
					{
						// do nothing. Main activity will send initial time stamp
						break;

					}
					case 1: 
					{
						Long deltaNs = (main.T3-main.T0) - value[0];
						Long deltaNsRoundTrip = (main.T3-main.T0);
						String serverText = "Delta found by server: " + String.valueOf(deltaNs) + ". Delta roundTrip was " + deltaNsRoundTrip + " delta on Client side was: " + value[0];
						Toast.makeText(context, serverText , Toast.LENGTH_SHORT).show();
						main.setState(2);
						break;
	
					}
				}
			}
			else // CLIENT SIDE: 
			{
				switch(main.getState())
				{
					case 0:
					{
						Long tSend = System.nanoTime();
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(bos);
						try {
							dos.writeLong(tSend - main.T3);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						writeToDevice(bos.toByteArray());
						Long tDelta = tSend - main.T3;
						String clientText = "sent in Client the delta: " + tDelta + " and tSend, tReceive were: " + tSend + " , " + main.T3;
						Toast.makeText(context, clientText , Toast.LENGTH_SHORT).show();
						break;

					}
					case 1: 
					{
						
						break;
	
					}
				}

				
			}

			

		}
		protected void onCancelled() {
			running = false;
		}
		public void closeConnection()
		{
			try {
				if(socket != null)
				{
					socket.close();
					Toast.makeText(context, "the connection is colsed", Toast.LENGTH_SHORT).show();
				}
			} catch (IOException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
				return;
			}
		}
		public void writeToDevice(byte[] data)
		{
			try {
				for(int i=0;i<data.length;i++)
				{
					outStream.write(data[i]);
				}
			} catch (IOException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}
	public Socket getInstanceSocket()
	{
		return  btSocket;
	}
	public Server getInstanceServer()
	{
		return btServer;
	}
	public Client getInstanceClient()
	{
		return btClient;
	}
	public BluetoothAdapter getInstanceAdapter()
	{
		return btAdapter;
	}
}
