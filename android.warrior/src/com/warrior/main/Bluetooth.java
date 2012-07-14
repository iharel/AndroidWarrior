package com.warrior.main;

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
	MainActivity main = (MainActivity)context;


	public Bluetooth(Context context)
	{
		this.context = context;
		btAdapter= BluetoothAdapter.getDefaultAdapter();
		
		btServer = new Server();
		btClient = new Client();

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
			btAdapter.cancelDiscovery();
			btAdapter.disable();
		}
	}
	public void startScanning()
	{
		btAdapter.startDiscovery();
	}
	// this is server side
	public class Server implements Runnable
	{
		private BluetoothServerSocket serverSocket;
		final static String NAME = "warroir";
		BluetoothSocket socket; 
		boolean running = false;
		
		public void createListen()throws Exception
		{
			if(!btAdapter.isEnabled())
			{
				throw new Exception();
			}
			serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(NAME, UUID_RFCOMM_GENERIC);
			Thread threadServer=new Thread(this);
			threadServer.start();
		}
		public void run() {
			try {
				Log.d("gal","start server running");
				running = true;
				socket = serverSocket.accept();
				if (socket != null) {
					btAdapter.cancelDiscovery();
				}
			} catch (IOException e) {
				Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
			}
			catch (Exception e) {
				Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
			}
			running = false;
			Log.d("gal","end server running");
		}
		public BluetoothSocket getSocket()
		{
			return socket;
		}
		public boolean getRunning()
		{
			return running;
		}
		
	}
	public class Client implements Runnable
	{
		private BluetoothSocket socket;
		private BluetoothDevice device;
		boolean running = false;
		public BluetoothSocket getSocket()
		{
			return socket;
		}
		public boolean getRunning()
		{
			return running;
		}
		public void run() {
			try {
				btAdapter.cancelDiscovery();
				running = true;
				// create socket with other device
				if(device == null)
				{
					throw new NullPointerException();
				}
				socket = device.createRfcommSocketToServiceRecord(UUID_RFCOMM_GENERIC);
				socket.connect();
			} catch (IOException e) {
				// service discovery failed
				Log.d("gal",e.getMessage());
			}
			running = false;
			
		}
		public void connectionToServer(BluetoothDevice device)
		{
			this.device = device;
			Thread threadClient=new Thread(this);
			threadClient.start();
		}
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
