package com.warrior.bluetooth;


import java.io.IOException;
import java.util.UUID;

import com.warrior.main.MyLog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewDebug.FlagToString;
import android.widget.Toast;

public class BluetoothHandler {
	private BluetoothAdapter btAdapter;
	private Context context;
	private final static int ALWAYS_DISCOVERY = 0;
	private final static UUID UUID_RFCOMM_GENERIC = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothSocket sSocket;
	private BluetoothSocket cSocket; 
	private ICreateServerSocket iCreateSocket;

	public BluetoothHandler(Context context)
	{
		this.context = context;
		btAdapter= BluetoothAdapter.getDefaultAdapter();

		if(btAdapter == null){
			// TODO: the device is not support in bluetooth
		}
	}
	public void setCreatedServerSocketListener(ICreateServerSocket iCreateSocket){
		this.iCreateSocket = iCreateSocket;
	}
	public BluetoothAdapter getAdapterInstance(){
		return btAdapter;
	}
	public BluetoothSocket getServerSocket(){
		return sSocket;
	}
	public BluetoothSocket getClientSocket(){
		return cSocket;
	}
	public boolean isEnabled()
	{
		return btAdapter.isEnabled();
	}
	public void setEnable()
	{
		// turn on the bluetooth 
		if (!btAdapter.isEnabled())
		{
			// Enabling bluetooth + making phone discoverable: 
			Intent iEnableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			iEnableBluetooth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			iEnableBluetooth.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,ALWAYS_DISCOVERY);
			context.startActivity(iEnableBluetooth);
			// We could easily use: btAdapter.enable(); but not recommended.
		}
	}
	public void setDisable()
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
	public void resetSockets(){
		cSocket = null;
		sSocket = null;
	}
	public BluetoothServerThread createServer(){
		return new BluetoothServerThread();
	}
	public BluetoothClientThread createClient(){
		return new BluetoothClientThread();
	}
	public void makeDiscoverable(){
    	Intent iDiscoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    	iDiscoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, ALWAYS_DISCOVERY);
    	iDiscoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(iDiscoverableIntent);
	}
	public void registerBluetoothDisconnected(BroadcastReceiver broadcastReceiver){
		IntentFilter i = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		context.registerReceiver(broadcastReceiver, i);
	}
	public void registerBluetoothConnected(BroadcastReceiver broadcastReceiver){
		IntentFilter i = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
		context.registerReceiver(broadcastReceiver, i);
	}
	public void registerBluetoothDeviceFound(BroadcastReceiver broadcastReceiver){
		IntentFilter i = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(broadcastReceiver, i);
	}
	public void registerBluetoothStateChanged(BroadcastReceiver broadcastReceiver){
		IntentFilter i = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		context.registerReceiver(broadcastReceiver, i);
	}
	public void registerBluetoothDiscoveryStart(BroadcastReceiver broadcastReceiver){
		IntentFilter i = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		context.registerReceiver(broadcastReceiver, i);
	}
	public void registerBluetoothDiscoveryFinished(BroadcastReceiver broadcastReceiver){
		IntentFilter i = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		context.registerReceiver(broadcastReceiver, i);
	}
	public void registerAllIntentsFilter(BroadcastReceiver broadcastReceiver){
  		registerBluetoothConnected(broadcastReceiver);
  		registerBluetoothDisconnected(broadcastReceiver);
  		registerBluetoothDeviceFound(broadcastReceiver);
  		registerBluetoothDiscoveryFinished(broadcastReceiver);
  		registerBluetoothDiscoveryStart(broadcastReceiver);
  		registerBluetoothStateChanged(broadcastReceiver);
	}
	// this is server side
	public class BluetoothServerThread extends AsyncTask<Void, Void, Void>
	{
		private BluetoothServerSocket serverSocket;
		final static String NAME = "warrior";
		public void startListeningSocket()throws Exception
		{
			if(!btAdapter.isEnabled())
			{
				Log.d(MyLog.BLUETOOTH,"BT is not enabled when trying to create a listening socket");
				throw new Exception();
			}
			Log.d(MyLog.BLUETOOTH,"creating listening socket");
			serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(NAME, UUID_RFCOMM_GENERIC);
			execute();
		}
		protected Void doInBackground(Void... params) {
			try {
				// the server wait to connected 
				sSocket = serverSocket.accept();
				Log.d(MyLog.BLUETOOTH,"Got socket request from client");

				// iharel: serverSocket = null;
				if (sSocket != null) {
					// cancel the scanning because is slowly the connection
					publishProgress();
				}
				Log.d(MyLog.BLUETOOTH, "after serverSocketClose");
			} catch (IOException e) {// if serverSocket.accept() threw an exception or cancelDiscovery, we want to call the serverClosed method
				Log.d(MyLog.BLUETOOTH,"Exception when trying to create listening socket - " + e.getMessage());
				publishProgress(); 
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Void... values) {
			iCreateSocket.createdServerSocket(sSocket);
		}
		public void close() {
			// TODO Auto-generated method stub
			try {
				btAdapter.cancelDiscovery();
				serverSocket.close();
				serverSocket = null;
				Log.d(MyLog.BLUETOOTH,"server close");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(MyLog.BLUETOOTH,"Excpetion: " + e.getMessage());
				e.printStackTrace();
			}
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
				if(device == null){
					throw new NullPointerException();
				}
				// cancel the scanning because is slowly the connection
				btAdapter.cancelDiscovery();
				cSocket = device.createRfcommSocketToServiceRecord(UUID_RFCOMM_GENERIC);
				Log.d(MyLog.BLUETOOTH,"client before try to connect");
				cSocket.connect();
				Log.d(MyLog.BLUETOOTH,"client try to connect");
				
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
	
	
	
	
	public interface ICreateServerSocket{
		void createdServerSocket(BluetoothSocket bluetoothServerSocket);
	}
}
