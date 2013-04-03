package com.warrior.main;

import java.net.ServerSocket;

import com.warrior.bluetooth.BluetoothHandler;
import com.warrior.bluetooth.CommHandler;
import com.warrior.bluetooth.Sync;
import com.warrior.bluetooth.BluetoothHandler.BluetoothServerThread;
import com.warrior.bluetooth.BluetoothHandler.ICreateServerSocket;
import com.warrior.bluetooth.CommHandler.IDateReceive;
import com.warrior.games.Game.GAME_STATES;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class MyApp extends Application implements IDateReceive,ICreateServerSocket{

	private MyFacebook fb;
	private CommHandler commHandler;
	private boolean isServer;
	private long timeAir;
	private BluetoothHandler bt;
	private Sync sync;
	public final static int RECEIVE_BLUETOOTH_DISCONNECTED = 10,SEND_BLUETOOTH_DISCONNECTED = 10;
	private IDataRecevieGame iDataReceiveGame;
	private IDataReceiveSync iDataRecevieSync;
	private ISelectGame iSelectGame;
	private IBluetoothChanged iBluetoothChanged;
	private APP_STATES state;
	private BluetoothServerThread server;
	
	
	public MyApp(){
		fb = new MyFacebook();
	}
	
	public long getTimeAir()
	{
		return timeAir;
	}
	public APP_STATES getState(){
		return state;
	}
	public CommHandler getCommHandler()
	{
		return commHandler;
	}
	public MyFacebook getFacebook(){
		return fb;
	}
	public BluetoothHandler getBluetoothHandler(){
		return bt;
	}
	public Sync getSync() {
		return sync;
	}
	public void setBluetoothChangedListener(IBluetoothChanged iBluetoothChanged) {
		this.iBluetoothChanged = iBluetoothChanged;
	}
	public void setDataReceiveSelectGameListener(ISelectGame iSelectGame){
		this.iSelectGame = iSelectGame;
	}

	public void setDataRecevieSyncListener(IDataReceiveSync iDataReceiveSync){
		this.iDataRecevieSync = iDataReceiveSync;
	}
	public void setDataRecevieGameListener(IDataRecevieGame iDataRecieveGame){
		this.iDataReceiveGame = iDataRecieveGame;
	}
	public void setCommHndler(CommHandler commHandler){
		this.commHandler = commHandler;
	}
	public void setBluetoothHandler(BluetoothHandler bt){
		this.bt = bt;
	}
	public void setServer(boolean isServer){
		this.isServer = isServer;
	}
	public boolean isSever(){
		return isServer;
	}
	public void setTimeAir(long timeAir){
		this.timeAir = timeAir;
	}
	public void setState(APP_STATES state) {
		this.state = state;
	}
	public void buildBluetoothHandler(){
		bt = new BluetoothHandler(this); 
		registerBluetooth();
	    if(bt.isEnabled()){
	    	bt.makeDiscoverable();
	    }
	}
	private void registerBluetooth(){
		bt.registerBluetoothConnected(BluetootChangedhReceiver);
		bt.registerBluetoothDisconnected(BluetootChangedhReceiver);
		bt.registerBluetoothStateChanged(BluetootChangedhReceiver);
	}
	public void unregisterBluetooth(){
		unregisterReceiver(BluetootChangedhReceiver);
	}
	private void bluetoothConeccted(){
		//register application to receive data 
		if(commHandler != null){
			commHandler.setDateReceiveListener(this);
			commHandler.startReceiveDataListener();
		}
		setState(APP_STATES.SYNC);
		sync = new Sync(this);
		
		if(isSever()){
			sync.startSync();
		}
		// call to current activity with message bluetooth connected 
		iBluetoothChanged.bluetoothStateConnectionChanged(true,null);
	}
	public void createdServerSocket(BluetoothSocket bluetoothServerSocket) {
		// this method called when client connect to server
		// create CommHandler class  for connection with other device
		if(commHandler == null)
		{
			Log.d(MyLog.APP, "created server socket");
			try {
	    		commHandler = new CommHandler(bluetoothServerSocket);
	    		isServer = true;
	    		bluetoothConeccted();
			} catch (Exception e) {
				iBluetoothChanged.bluetoothStateConnectionChanged(false,e.getMessage());
				Log.d(MyLog.APP, "exception " + e.getMessage());
			}
		}
	}
	public BroadcastReceiver BluetootChangedhReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
		    if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
		    	closeConnection();
		    	iBluetoothChanged.bluetoothStateConnectionChanged(false,"connection closed");
		    	Log.d("MyApp","disconnected");
		    }
		    else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
		    {
		    	// code of server side not write here because when this method called by Bluetooth
		    	// method bt.getServerSocket() return null
		    	// solution for problem create interface call ICreatedServerSocket
		    	// code of sever found in createdServerSocket method
		    	
		    	// code of client side when device connected to server
		    	try {
		    		if(!isServer){
		    			if(commHandler == null){
		    				// create CommHandler class  for connection with other device
				    		commHandler = new CommHandler( bt.getClientSocket());
				    		// this is client side we need colse a server
				    		server.close();
			    		}
			    	}
		    		bluetoothConeccted();
		    	 } 
		    	 catch (Exception e) {
		    		Log.d(MyLog.APP,"exception " + e.getMessage());
		    	 }
		    }
		    else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
		    {
		    	// to bluetooth there are 4 modes
		    	// turning off, off, turning on, on
		    	// we use in two state turning off and turning on
		    	int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
		    	Log.d(MyLog.APP,"action state changed = " + state);
		    	switch (state) {
		            case BluetoothAdapter.STATE_TURNING_OFF:
			    		closeConnection();
			    		server = null;
			    		bt.resetSockets();
			    		// becuase sync create after create connection with other device 
			    		if(sync != null){
			    			sync.resetSync();
			    		}
		                break;
		            case BluetoothAdapter.STATE_ON:
		            	try {
			            	// create server
							server = bt.createServer();
							bt.setCreatedServerSocketListener(MyApp.this);
							// the server is opened and wait to connect in new thread
							server.startListeningSocket();
							Log.d(MyLog.APP,"server wait");
							iBluetoothChanged.bluetoothStateChanged(state);
		            	} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            	break;
	            }
		    	iBluetoothChanged.bluetoothStateChanged(state);
		    }
		}
	};
	public void closeConnection(){
		if(commHandler != null){
			commHandler.writeToRmoteDevice(CommHandler.DISCONNECTED);
	    	commHandler.closeConnection();
			commHandler = null;
		}
	}
	public void dataReceive(Long[] values){
		Long receiveValue1 = values[0]; 
		if(receiveValue1 == CommHandler.DISCONNECTED)
		{
			commHandler.writeToRmoteDevice(CommHandler.DISCONNECTED);
			return;
		}
		switch(state){
			case SYNC:{
				iDataRecevieSync.dataRecevieSync(values);
				Log.d(MyLog.APP,"in follwiong state SYNC");
				break;
			}
			case SELECT_GAME:{
				iSelectGame.dataReceiveSelectGame(receiveValue1);
				Log.d(MyLog.APP,"in follwiong state SELECT_GAME");
				break;
			}
			case RUN_GAME:{
				iDataReceiveGame.dataReceiveGame(receiveValue1);
				Log.d(MyLog.APP,"in following state: NOT_YET" );
				break;
			}
		
		}
	}
	public interface IBluetoothChanged{
		void bluetoothStateConnectionChanged(boolean isConnected,String error);
		void bluetoothStateChanged(int state);
	}
	public interface ISelectGame{
		void dataReceiveSelectGame(Long typeGame);
	}
	public interface IDataReceiveSync {
		void dataRecevieSync(Long[] values);
	}	
	public interface IDataRecevieGame {
		void dataReceiveGame(long data);
	}
	static public enum APP_STATES{
		SYNC,
		SELECT_GAME,
		RUN_GAME
	}
	
}
