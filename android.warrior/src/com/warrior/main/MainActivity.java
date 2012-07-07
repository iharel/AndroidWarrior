package com.warrior.main;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RemoteViews.ActionException;
import android.widget.TextView;
import android.widget.Toast;

import com.gal.bluetooth1.R;
import com.warrior.bluetooth.Bluetooth;
import com.warrior.bluetooth.Bluetooth.Socket;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {

	private Button butEnable,butDisable,butScan,butTransmit;
	private ListView lv;
	private Bluetooth bt;
	private ArrayAdapter<String> devicesNamesAdapter;
	private List<BluetoothDevice>devicesList;
	private boolean registerToReceiver = false;
	private final static int RESULT_ENABLE_BT = 1;
	private boolean isServer = true;
	private Integer state = 0;
	public Long T0;
	public Long T3;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        devicesNamesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        devicesList = new ArrayList<BluetoothDevice>();

        bt = new Bluetooth(this);
        butEnable = (Button)findViewById(R.id.butEnable);
        butDisable = (Button)findViewById(R.id.butDisable);
        butScan = (Button)findViewById(R.id.butScan);
        butTransmit = (Button)findViewById(R.id.butTransmit);
        lv = (ListView)findViewById(R.id.lv);
        lv.setEmptyView((TextView)findViewById(R.id.tvEmpty));
        
        lv.setOnItemClickListener(this);
        butEnable.setOnClickListener(this);
        butDisable.setOnClickListener(this);
        butScan.setOnClickListener(this);
        butTransmit.setOnClickListener(this);
    }
    protected void onStop() {
    	onClick(butDisable);
    	super.onStop();
    }
	public void onClick(View v) {
		if(v.equals(butEnable))
		{
			IntentFilter iDeviceFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			IntentFilter iConnectedBluetooth = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
			IntentFilter iStateBluetoothChanged = new IntentFilter(bt.getInstanceAdapter().ACTION_STATE_CHANGED);
	        this.registerReceiver(ReceiverBluetooth, iDeviceFound); 
	        this.registerReceiver(ReceiverBluetooth, iConnectedBluetooth);
	        this.registerReceiver(ReceiverBluetooth, iStateBluetoothChanged);
	        registerToReceiver = true;
			
			butDisable.setEnabled(true);
			butScan.setEnabled(true);
			butTransmit.setEnabled(true);
			butEnable.setEnabled(false);
			// this is turn on the server side
			bt.enableBluetooth();
		}	
		else if(v.equals(butDisable))
		{
			bt.disableBluetooth();
			if(registerToReceiver == true)
			{
				this.unregisterReceiver(ReceiverBluetooth);
				registerToReceiver = false;
			}
			
			Toast.makeText(this,"the bluetooth is disable",Toast.LENGTH_SHORT).show();
			devicesList.clear();
			
			butDisable.setEnabled(false);
			butScan.setEnabled(false);
			butTransmit.setEnabled(false);
			butEnable.setEnabled(true);
		}
		else if(v.equals(butScan))
		{
			devicesNamesAdapter.clear();
			bt.startScanning();
			Toast.makeText(this,"the bluetooth is scanning",Toast.LENGTH_SHORT).show();
		}
		else if(v.equals(butTransmit))
		{
			//send string
			String data = "gal lavie";
			byte[] dataBytes = data.getBytes();
			// send integer
			//byte[] dataBytes = {5};
			if (isServer)
			{
				T0 = System.nanoTime();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				try {
					dos.writeLong(T0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				bt.getInstanceSocket().writeToDevice(bos.toByteArray());
				state = 1; // sent T0 to client device
			}
			else
			{
				// shouldn't have gotten here. 
			}
			bt.getInstanceSocket().writeToDevice(dataBytes);
		}
	}
	// this is turn on the client side
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		// get device from devices list
		BluetoothDevice device = devicesList.get(position);
		if(bt.getInstanceClient().getStatus() != Status.RUNNING)
		{
			bt.getInstanceClient().execute(device);
			isServer = false;
		}
	}
	// Create a BroadcastReceiver
	private final BroadcastReceiver ReceiverBluetooth = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
	        // When discovery finds a device
		    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		    	// Get the BluetoothDevice object from the Intent
		        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        devicesList.add(device);
		        // Add the name and address to an array adapter to show in a ListView
		        devicesNamesAdapter.add(device.getName() + "\n" + device.getAddress());
		        lv.setAdapter(devicesNamesAdapter);
		    }
		    else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
		    {
		    	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		    	Toast.makeText(context, "you connected to " + device.getName(), Toast.LENGTH_SHORT).show();
		    	butTransmit.setEnabled(true);
		    }
		    else if(bt.getInstanceAdapter().ACTION_STATE_CHANGED.equals(action))
		    {
		    	if(bt.getInstanceAdapter().isEnabled())
		    	{
		    		if(bt.getInstanceServer().getStatus() == Status.PENDING)
	    			{
	    				bt.getInstanceServer().execute();
	    			}
		    		Toast.makeText(MainActivity.this,"the bluetooth is enable",Toast.LENGTH_SHORT).show();
		    	}
		    }
		}
	};
	public int getState()
	{
		return this.state;
	}
	public void setState(int state)
	{
		this.state = state;
	}
	public boolean getIsServer()
	{
		return this.isServer;
	}
	public void setIsServer(boolean isServer)
	{
		this.isServer = isServer;
	}
}