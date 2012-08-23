package com.warrior.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import com.warrior.main.Bluetooth.IServerClosed;
import com.warrior.main.Bluetooth.Server;
import com.warrior.main.Sync.IFinishSync;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener, IFinishSync {

	private Button butEnable,butDisable,butScan,butOpenServer;
	private ListView lv;
	private Bluetooth bt;
	private ArrayAdapter<String> devicesNamesAdapter;
	private List<BluetoothDevice>devicesList;
	private boolean isServer = true;
	private CommHandler commHandler;
	private Sync sync;
	private final static int RC_SELECT_GAME = 10;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bt = new Bluetooth(this);        
        devicesNamesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        devicesList = new ArrayList<BluetoothDevice>();
        buildViewReferece();
        buildIntentFilter();
        BuildEvents();
    }
    protected void onResume() {
    	 if(bt.isEnabled()){
         	buttonsForBtEnabled();
         }
    	 else{
    		 buttonsForBtDisabled();
    	 }
    	super.onResume();
    }
    private void buildViewReferece()
    {
    	  butEnable = (Button)findViewById(R.id.butEnable);
          butDisable = (Button)findViewById(R.id.butDisable);
          butScan = (Button)findViewById(R.id.butScan);
          butOpenServer = (Button)findViewById(R.id.butOpenServer);
          lv = (ListView)findViewById(R.id.lv);
          lv.setEmptyView((TextView)findViewById(R.id.tvEmpty));
    }
    private void buildIntentFilter()
    {
    	IntentFilter iDeviceFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
  		IntentFilter iConnectedBluetooth = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
  		IntentFilter iDisConnectedBluetooth = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
  		IntentFilter iStateBluetoothChanged = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
  		IntentFilter iDiscoveryFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
  		IntentFilter iDiscoveryStarted = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
  		registerReceiver(ReceiverBluetooth, iDeviceFound); 
  		registerReceiver(ReceiverBluetooth, iConnectedBluetooth);
  		registerReceiver(ReceiverBluetooth, iDisConnectedBluetooth);
  		registerReceiver(ReceiverBluetooth, iStateBluetoothChanged);
  		registerReceiver(ReceiverBluetooth, iDiscoveryFinished);
  		registerReceiver(ReceiverBluetooth, iDiscoveryStarted);
    }
    private void BuildEvents()
    {
    	   lv.setOnItemClickListener(this);
           butEnable.setOnClickListener(this);
           butDisable.setOnClickListener(this);
           butScan.setOnClickListener(this);
           butOpenServer.setOnClickListener(this);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode){
    		case RC_SELECT_GAME:{
    			onClick(butDisable);
    		}
    	}
    }
	public void onClick(View v) {
		if(v.equals(butEnable))
		{
			bt.enableBluetooth();
		}	
		else if(v.equals(butDisable))
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
			bt.startScanning();
			devicesNamesAdapter.clear();
		}
		else if(v.equals(butOpenServer))
		{
			try {
				butOpenServer.setEnabled(false);
				// create server
				Server server = bt.createServer();
				// create listen 
				server.setListenerCloseServer(new IServerClosed() {
					public void serverClosed() {
						butOpenServer.setEnabled(true);
						Toast.makeText(MainActivity.this, "the server is closed", Toast.LENGTH_SHORT).show();
					}
				});
				// the server is open and wait to connect in new thread
				server.createListen();
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
		    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
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
				    		while(bt.getSocketServer() == null){}
				    		commHandler = new CommHandler(bt.getSocketServer());
			    		}
			    	}
			    	else
			    	{
			    		if(commHandler == null)
			    		{
				    		commHandler = new CommHandler( bt.getSocketClient());
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
		    	buttonsForBtConnected();
			   	devicesNamesAdapter.clear();
		    }
		    else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
		    {
		    	// the bluetooth is disconnected from other device
		    	try {
					commHandler.closeConnection();
					commHandler = null;
			    	Toast.makeText(context,"the connection is closed",Toast.LENGTH_SHORT).show();
			    	buttonsForBtEnabled();
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
		            	buttonsForBtDisabled();
			    		devicesList.clear();
			    		devicesNamesAdapter.clear();
			    		// show message the the bluetooth is disable
			    		Toast.makeText(MainActivity.this,"the bluetooth is disable",Toast.LENGTH_SHORT).show();
		                break;
		            case BluetoothAdapter.STATE_TURNING_ON:
		            	buttonsForBtEnabled();
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
	private void buttonsForBtEnabled()
	{
		butDisable.setEnabled(true);
		butScan.setEnabled(true);
		butOpenServer.setEnabled(true);
		butEnable.setEnabled(false);
	}
	private void buttonsForBtDisabled()
	{
		butEnable.setEnabled(true);
		butDisable.setEnabled(false);
		butScan.setEnabled(false);
		butOpenServer.setEnabled(false);
	}
	private void buttonsForBtConnected()
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