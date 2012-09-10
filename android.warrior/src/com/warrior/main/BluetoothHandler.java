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

public class BluetoothHandler {
	private BluetoothAdapter btAdapter;
	private Context context;
	private final static int ALWAYS_DISCOVERY = 0;
	private final static UUID UUID_RFCOMM_GENERIC = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private final static int TIME_OUT = 20000;
	private BluetoothSocket sSocket;
	private BluetoothSocket cSocket; 

	public BluetoothHandler(Context context)
	{
		this.context = context;
		btAdapter= BluetoothAdapter.getDefaultAdapter();

		if(btAdapter == null)
		{
			// TODO: the device is not support in bluetooth
		}
	}
	public void enableBluetooth()
	{
		// turn on the bluetooth 
		if (!btAdapter.isEnabled())
		{

			// Enabling bluetooth + making phone discoverable: 
			Intent iEnableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			iEnableBluetooth.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,ALWAYS_DISCOVERY);
			context.startActivity(iEnableBluetooth);
			// We could easily use: btAdapter.enable(); but not recommended.
		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		
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
		if((btAdapter.isEnabled()) && (!btAdapter.isDiscovering()))
		{
			btAdapter.startDiscovery();
		}
	}
	public boolean isEnabled()
	{
		return btAdapter.isEnabled();
	}
	// this is server side
	public class BluetoothServerThread extends AsyncTask<Void, Void, Void>
	{
		private BluetoothServerSocket serverSocket;
		final static String NAME = "warrior";
		private IServerClosed serverClosed;
		public void createListeningSocket()throws Exception
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
				// iharel: serverSocket = null;
				if (sSocket != null) {
					// cancel the scanning because is slowly the connection
					serverSocket.close();
					btAdapter.cancelDiscovery();
				}
			} catch (IOException e) {// if serverSocket.accept() threw an exception or cancelDiscovery, we want to call the serverClosed method
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
	public class BluetoothClientThread extends AsyncTask<Void, String, Void>
	{
		private BluetoothDevice device;
		
		public void connectToServer(BluetoothDevice device)throws Exception
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
	public BluetoothServerThread createServer()
	{
		return new BluetoothServerThread();
	}
	public BluetoothClientThread createClient()
	{
		return new BluetoothClientThread();
	}
	public BluetoothAdapter getAdapterInstance()
	{
		return btAdapter;
	}
	public BluetoothSocket getServerSocket()
	{
		return sSocket;
	}
	public BluetoothSocket getClientSocket()
	{
		return cSocket;
	}
	public void resetSockets()
	{
		cSocket = null;
		sSocket = null;
	}
	
	interface IServerClosed 
	{ // Interface forcing implementation of a method taking care of serverClose event. 
		void serverClosed();
	}
}
