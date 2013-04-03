package com.warrior.main;


import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog.FeedDialogBuilder;
import com.warrior.bluetooth.BluetoothHandler;
import com.warrior.bluetooth.Sync;
import com.warrior.bluetooth.BluetoothHandler.BluetoothServerThread;
import com.warrior.bluetooth.Sync.IFinishSync;
import com.warrior.games.LaunchingGame;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.games.activitys.InputTextFragmentLayout;
import com.warrior.games.activitys.SpeedCalculationGameActivity;
import com.warrior.games.SpeedCalculationGame;
import com.warrior.main.MyApp.APP_STATES;
import com.warrior.main.MyApp.IBluetoothChanged;
import com.warrior.main.MyFacebook.ISessionState;

public class MainActivity extends MyActivity 
	implements OnItemClickListener, IFinishSync{

	private Button butBT,butScan;
	private ListView lv;
	private TextView tvUserName;
	private BluetoothHandler bt;
	private ArrayAdapter<String> devicesNamesAdapter;
	private List<BluetoothDevice>devicesList;
	private final static int ABOUT_ACTIVITY = 0,HELP_ACTIVITY = 1;
	private static final int REQUEST_CODE_NEXT_STAGE = 10;
	private static final int REQUEST_CODE_FACEBOOK_LOGIN = 64206;
	private MyApp app;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        app = (MyApp)getApplication();
        
        devicesNamesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        devicesList = new ArrayList<BluetoothDevice>();
        
        buildViewReferences();
        buildViewListeners();
		
	    if(app.getFacebook().isReadyOpenConnection()){
	    	app.getFacebook().openSession(this);
		}   
	    
        app.buildBluetoothHandler();
        bt = app.getBluetoothHandler();
        bt.registerBluetoothDeviceFound(ReceiverScanDeviceBluetooth);
        bt.registerBluetoothDiscoveryStart(ReceiverScanDeviceBluetooth);
        bt.registerBluetoothDiscoveryFinished(ReceiverScanDeviceBluetooth);
    }
    
    protected void onResume() {
    	super.onResume();
    	if(bt.isEnabled()){
    		setButtonsWhenBtEnabled();
        }
    	else{
    		setButtonsWhenBtDisabled();
    	}
    }
   
    private void buildViewReferences(){
    	butBT = (Button)findViewById(R.id.butBT);
        butScan = (Button)findViewById(R.id.butScan);
        tvUserName = (TextView)findViewById(R.id.tvUserName);
        lv = (ListView)findViewById(R.id.lv);
        lv.setEmptyView((TextView)findViewById(R.id.tvEmpty));
    }
    private void buildViewListeners(){
    	lv.setOnItemClickListener(this);
        butBT.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!bt.isEnabled()){
					bt.setEnable();
				}	
				else {
					bt.setDisable();
				}
			}
		});
        butScan.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// the bluetooth is start scanning 
				devicesNamesAdapter.clear();
				bt.startScanning();
			}
		});
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode){
	    	case REQUEST_CODE_FACEBOOK_LOGIN:{
	    		if(resultCode == -1){
	    			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	    		}
				break;
			}
    	}
    }
	public boolean onCreateOptionsMenu(Menu menu) {
		// create the optionMenu
	   	MenuInflater flate = getMenuInflater();
	   	flate.inflate(R.menu.option_menu, menu);
	   	return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		int order = item.getOrder();
	   	switch(order){
	    	case ABOUT_ACTIVITY:{
	    		Intent iAboutActivity = new Intent(this,AboutActivity.class);
		    	startActivity(iAboutActivity);
		   		break;
		   	}
		   	case HELP_ACTIVITY:{
		   		Intent iHelpActivity = new Intent(this,HelpActivity.class);
	    		startActivity(iHelpActivity);
	    		break;
		    }
	    }
	   	return super.onOptionsItemSelected(item);
	}
	// this is turn on the client side
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		try {
			app.setServer(false);
			// try to connect to server in new thread 
			// we need to pass to client the device we want to connect
			bt.createClient().connectToServer(devicesList.get(position));

		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			Log.d(MyLog.MAIN_ACTIVITY,"excetion: " + e.getMessage());
		}
	}
	@Override
	public void onBackPressed() {
		//unregisterReceiver(ReceiverScanDeviceBluetooth);
		app.unregisterBluetooth();
		app.getFacebook().closeSession();
		bt.setDisable();
		finish();
	}
	@Override
	public void bluetoothStateConnectionChanged(boolean isConnected,String error) {
		if(isConnected){
			app.getSync().setListenerFinishSync(this);
		   	devicesNamesAdapter.clear();
		}
		else{
			if(bt.isEnabled()){
				bt.setDisable();
			}
			Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
			setButtonsWhenBtEnabled();
			devicesNamesAdapter.clear();	
		}
	}
	@Override
	public void bluetoothStateChanged(int state) {
		super.bluetoothStateChanged(state);
		switch (state) {
		case BluetoothAdapter.STATE_OFF:
			setButtonsWhenBtDisabled();
			break;

		case BluetoothAdapter.STATE_ON:
        	// show message the the bluetooth is enable and change buttons
			setButtonsWhenBtEnabled();
			break;
		}
	}
	// Create a BroadcastReceiver

	private final BroadcastReceiver ReceiverScanDeviceBluetooth = new BroadcastReceiver() {
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
		    else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
		    	Toast.makeText(MainActivity.this, "scan has started", Toast.LENGTH_LONG).show();
		    }
		    else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
		    	Toast.makeText(MainActivity.this, "scan has finished", Toast.LENGTH_LONG).show();
		    }
		    
		}
		
	};
	private void setButtonsWhenBtEnabled(){
		butScan.setEnabled(true);
		butBT.setBackgroundDrawable(
				getResources().getDrawable(R.drawable.bluetooth_off));
		//Toast.makeText(this,"bluetooth has enabled",Toast.LENGTH_SHORT).show();
	}
	private void setButtonsWhenBtDisabled(){
		butScan.setEnabled(false);
		butBT.setBackgroundDrawable(
				getResources().getDrawable(R.drawable.bluetooth_on));
		devicesList.clear();
		devicesNamesAdapter.clear();
	}
	public void finishSync() {
		// the sync is over the application move to next stage
		// (server side selectGameActivity)
		// (client side WaitToSelectGameActivity)
		Sync sync = app.getSync();
		app.setTimeAir(sync.getTimeAir());
		if (app.isSever()) {
			Intent iSelectGameActivity = new Intent(this,SelectGameActivity.class);
			startActivityForResult(iSelectGameActivity, REQUEST_CODE_NEXT_STAGE);

		}
		else {
			Intent iWaitToSelectGameActivity = new Intent(this,WaitToSelectGameActivity.class);
			startActivityForResult(iWaitToSelectGameActivity, REQUEST_CODE_NEXT_STAGE);
			
		}
		Toast.makeText(this, "the sync is finished", Toast.LENGTH_SHORT).show();
	}
	public void facebookOpenSession(boolean isSuccess, String exception) {
		if(isSuccess){
			MyFacebook fb =app.getFacebook(); 
			tvUserName.setText("welcome " + fb.getUser().getName());
		}
		else{
			Toast.makeText(this, exception, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void facebookCloseSession() {
		tvUserName.setText("");
	}	

}