package com.warrior.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidWarrior.*;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.main.BluetoothHandler.IServerClosed;
import com.warrior.main.BluetoothHandler.BluetoothServerThread;
import com.warrior.main.Sync.IFinishSync;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener, IFinishSync {

	private Button butEnableBT,butDisableBT,butScan,butOpenServer;
	private ListView lv;
	private BluetoothHandler bt;
	private ArrayAdapter<String> devicesNamesAdapter;
	private List<BluetoothDevice>devicesList;
	private boolean isServer = true;
	private CommHandler commHandler;
	private Sync sync;
	private final static int RC_SELECT_GAME = 10;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bt = new BluetoothHandler(this);        
        devicesNamesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        devicesList = new ArrayList<BluetoothDevice>();
        buildViewReferences();
        buildIntentFilters();
        buildEventsListeners();
    }
    protected void onResume() {
    	 if(bt.isEnabled()){
         	setButtonsWhenBtEnabled();
         }
    	 else{
    		 setButtonsWhenBtDisabled();
    	 }
    	super.onResume();
    }
    private void buildViewReferences()
    {
    	  butEnableBT = (Button)findViewById(R.id.butEnable);
          butDisableBT = (Button)findViewById(R.id.butDisable);
          butScan = (Button)findViewById(R.id.butScan);
          butOpenServer = (Button)findViewById(R.id.butOpenServer);
          lv = (ListView)findViewById(R.id.lv);
          lv.setEmptyView((TextView)findViewById(R.id.tvEmpty));
    }
    private void buildIntentFilters()
    {
    	IntentFilter intentDeviceFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
  		IntentFilter intentConnectedBluetooth = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
  		IntentFilter intentDisConnectedBluetooth = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
  		IntentFilter intentStateBluetoothChanged = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
  		IntentFilter intentDiscoveryFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
  		IntentFilter intentDiscoveryStarted = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
  		registerReceiver(ReceiverBluetooth, intentDeviceFound); 
  		registerReceiver(ReceiverBluetooth, intentConnectedBluetooth);
  		registerReceiver(ReceiverBluetooth, intentDisConnectedBluetooth);
  		registerReceiver(ReceiverBluetooth, intentStateBluetoothChanged);
  		registerReceiver(ReceiverBluetooth, intentDiscoveryFinished);
  		registerReceiver(ReceiverBluetooth, intentDiscoveryStarted);
    }
    private void buildEventsListeners()
    {
    	   lv.setOnItemClickListener(this);
           butEnableBT.setOnClickListener(this);
           butDisableBT.setOnClickListener(this);
           butScan.setOnClickListener(this);
           butOpenServer.setOnClickListener(this);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode){
    		case RC_SELECT_GAME:{
    			onClick(butDisableBT);
    		}
    	}
    }
	public void onClick(View v) {
		if(v.equals(butEnableBT))
		{
			bt.enableBluetooth();
		}	
		else if(v.equals(butDisableBT))
		{
			if(commHandler != null){
				if(commHandler.getIsConnected()){
					
					try {
						commHandler.writeToRmoteDevice(CommHandler.DISCONNECTED);
						Thread.sleep(sync.getTimeAir());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return;
			}
			bt.disableBluetooth();
		}
		else if(v.equals(butScan))
		{
			// the bluetooth is start scanning 
			devicesNamesAdapter.clear();
			bt.startScanning();
		}
		else if(v.equals(butOpenServer))
		{
			try {
				butOpenServer.setEnabled(false);
				// create server
				BluetoothServerThread server = bt.createServer();
				// create listen 
				server.setListenerCloseServer(new IServerClosed() {
					public void serverClosed() {
						butOpenServer.setEnabled(true);
						Toast.makeText(MainActivity.this, "the server is closed", Toast.LENGTH_SHORT).show();
					}
				});
				// the server is open and wait to connect in new thread
				server.createListeningSocket();
			
				Toast.makeText(MainActivity.this, "the server is open", Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
	protected void onDestroy() {
		this.unregisterReceiver(ReceiverBluetooth);
		super.onDestroy();
	}
	// this is turn on the client side
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		try {
			isServer = false;
			// try to connect to server in new thread 
			// we need to pass to client the device we want to connect
			bt.createClient().connectionToServer(devicesList.get(position));
			Toast.makeText(this, devicesList.get(position).getName() + " and adapter: " + devicesNamesAdapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	// Create a BroadcastReceiver
	private final BroadcastReceiver ReceiverBluetooth = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
		    if (action.equals(BluetoothDevice.ACTION_FOUND)) {
		    	// the bluetooth found other device
		        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        // add the device to list of devices
		        devicesList.add(device);
		        // show the device found on activity
		        devicesNamesAdapter.add(device.getName() + "\n" + device.getAddress());
		        lv.setAdapter(devicesNamesAdapter);
		    }
		    
		    else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
		    {
		    	// the bluetooth is connected to other device
		    	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		    	try {
		    		if(isServer)
			    	{
			    		if(commHandler == null)
			    		{
			    			// wait socket of server different from null
				    		while(bt.getServerSocket() == null){}
				    		commHandler = new CommHandler(bt.getServerSocket());
			    		}
			    	}
			    	else
			    	{
			    		if(commHandler == null)
			    		{
				    		commHandler = new CommHandler( bt.getClientSocket());
			    		}
			    	}
		    		sync = new Sync(commHandler, isServer);
		    		sync.setListenerFinishSync(MainActivity.this);
		    		commHandler.startReceiveData();
		    		if(isServer){
		    			sync.startSync();
		    		}
		    	 } 
		    	 catch (IOException e) {
		    		Toast.makeText(context, e.getMessage() + device.getName(), Toast.LENGTH_SHORT).show();
				 }
		    	 catch (Exception e) {
		    		 Toast.makeText(context, e.getMessage() + device.getName(), Toast.LENGTH_SHORT).show();
		    	 }
		    	
			    Toast.makeText(context, "you connected to " + device.getName(), Toast.LENGTH_SHORT).show();
		    	setButtonsWhenBtConnected();
			   	devicesNamesAdapter.clear();
		    }
		    else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
		    {
		    	// the bluetooth is disconnected from other device
		    	try {
					commHandler.closeConnection();
					commHandler = null;
			    	Toast.makeText(context,"the connection is closed",Toast.LENGTH_SHORT).show();
			    	setButtonsWhenBtEnabled();
			    	devicesNamesAdapter.clear();
			    	bt.disableBluetooth();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		   
		    else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
		    {
		    	// to bluetooth there are 4 modes
		    	// turning off, off, turning on, on
		    	// we use in two state turning off and turning on
		    	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
		    	switch (state) {
		            case BluetoothAdapter.STATE_TURNING_OFF:
		            	setButtonsWhenBtDisabled();
			    		devicesList.clear();
			    		devicesNamesAdapter.clear();
			    		// show message the the bluetooth is disable
			    		Toast.makeText(MainActivity.this,"the bluetooth is disable",Toast.LENGTH_SHORT).show();
		                break;
		            case BluetoothAdapter.STATE_TURNING_ON:
		            	setButtonsWhenBtEnabled();
		            	// show message the the bluetooth is enable
			    		Toast.makeText(context,"the bluetooth is enable",Toast.LENGTH_SHORT).show();
		                break;
	            }
		    }
		    else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
		    	// show message the scan is finished
		    	Toast.makeText(context, "the scan is finished",Toast.LENGTH_SHORT).show();
		    	butScan.setEnabled(true);
			}
		    else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
		    {
		    	// show message the scan is started
		    	Toast.makeText(context, "the scan is started",Toast.LENGTH_SHORT).show();
		    	butScan.setEnabled(false);
		    }
		}
		
	};
	private void setButtonsWhenBtEnabled()
	{
		butDisableBT.setEnabled(true);
		butScan.setEnabled(true);
		butOpenServer.setEnabled(true);
		butEnableBT.setEnabled(false);
	}
	private void setButtonsWhenBtDisabled()
	{
		butEnableBT.setEnabled(true);
		butDisableBT.setEnabled(false);
		butScan.setEnabled(false);
		butOpenServer.setEnabled(false);
	}
	private void setButtonsWhenBtConnected()
	{
	    butOpenServer.setEnabled(false);
	   	butScan.setEnabled(false);
	}
	public void finishSync() {
		// the sync is over the game move to activity game
		Toast.makeText(this, "the sync is finished", Toast.LENGTH_SHORT).show();
		MyApp app = (MyApp)this.getApplication();
		app.setCommHndler(commHandler);
		app.setIsServer(isServer);
		app.setTimeAir(sync.getTimeAir());
		commHandler.setStateGame(GAME_STATES.NOT_YET);
		Intent iSelectGame = new Intent(this,SelectGameActivity.class);
		startActivityForResult(iSelectGame, RC_SELECT_GAME);
		sync.resetSync();
	}
}