package com.warrior.main;


import java.io.IOException;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

public class Bluetooth {
	private BluetoothAdapter btAdapter;
	private Context context;
	private final static int ALWAYS_DISCOVERY = 0;
	private final static UUID UUID_RFCOMM_GENERIC = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private final static int TIME_OUT = 20000;
	private BluetoothSocket sSocket;
	private BluetoothSocket cSocket; 

	public Bluetooth(Context context)
	{
		this.context = context;
		btAdapter= BluetoothAdapter.getDefaultAdapter();

		if(btAdapter == null)
		{
			// the device is not support in bluetooth
		}
	}
	public void enableBluetooth()
	{
		// turn on the bluetooth 
		Intent iEnableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
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
		if(!btAdapter.isDiscovering())
		{
			btAdapter.startDiscovery();
		}
	}
	public boolean isEnabled()
	{
		return btAdapter.isEnabled();
	}
	// this is server side
	public class Server extends AsyncTask<Void, Void, Void>
	{
		private BluetoothServerSocket serverSocket;
		final static String NAME = "warroir";
		private IServerClosed serverClosed;
		public void createListen()throws Exception
		{
			if(!btAdapter.isEnabled())
			{
				throw new Exception();
			}
			serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(NAME, UUID_RFCOMM_GENERIC);
			execute();
		}
		protected Void doInBackground(Void... params) {
			try {
				// the server wait to connected with time out of 20 sec
				sSocket = serverSocket.accept(TIME_OUT);
				serverSocket = null;
				if (sSocket != null) {
					// cancel the scanning because is slowly the connection
					btAdapter.cancelDiscovery();
				}
			} catch (IOException e) {
				publishProgress();
				this.cancel(true);
			}
			return null;
		}
		protected void onProgressUpdate(Void... values) {
			serverClosed.serverClosed();
		}
		public void setListenerCloseServer(IServerClosed serverClosed)
		{
			this.serverClosed = serverClosed;
		}
	}
	public class Client extends AsyncTask<Void, String, Void>
	{
		private BluetoothDevice device;
		
		public void connectionToServer(BluetoothDevice device)throws Exception
		{
			this.device = device;
			// turn on the new thread  
			this.execute();
		}
		protected Void doInBackground(Void... params) {
			try {
				if(device == null)
				{
					throw new NullPointerException();
				}
				// cancel the scanning because is slowly the connection
				btAdapter.cancelDiscovery();
				cSocket = device.createRfcommSocketToServiceRecord(UUID_RFCOMM_GENERIC);
				cSocket.connect();
			} catch (IOException e) {
				publishProgress(e.getMessage());
			} catch (NullPointerException e) {
				publishProgress(e.getMessage());
			}
			return null;
		}
		protected void onProgressUpdate(String... values) {
			Toast.makeText(context,values[0] ,Toast.LENGTH_SHORT).show();
		}
	}
	public Server createServer()
	{
		return new Server();
	}
	public Client createClient()
	{
		return new Client();
	}
	public BluetoothAdapter getInstanceAdapter()
	{
		return btAdapter;
	}
	public BluetoothSocket getSocketServer()
	{
		return sSocket;
	}
	public BluetoothSocket getSocketClient()
	{
		return cSocket;
	}
	public void resetSockets()
	{
		cSocket = null;
		sSocket = null;
	}
	interface IServerClosed {
		void serverClosed();
	}
}
