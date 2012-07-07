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
import android.bluetooth.BluetoothServerSocket;
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
import com.warrior.bluetooth.CommHandler;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener,IDataRecive {

	private Button butEnable,butDisable,butScan,butTransmit;
	private ListView lv;
	private Bluetooth bt;
	private TextView tv;
	private ArrayAdapter<String> devicesNamesAdapter;
	private List<BluetoothDevice>devicesList;
	private final static int RESULT_ENABLE_BT = 1;
	private boolean isServer = true;
	private Integer state = 0;
	private Long T0;
	private Long T3 = (long) 0;
	private CommHandler commHandler;
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
        tv = (TextView)findViewById(R.id.tv);
        lv = (ListView)findViewById(R.id.lv);
        lv.setEmptyView((TextView)findViewById(R.id.tvEmpty));
        
        lv.setOnItemClickListener(this);
        butEnable.setOnClickListener(this);
        butDisable.setOnClickListener(this);
        butScan.setOnClickListener(this);
        butTransmit.setOnClickListener(this);
        
        IntentFilter iDeviceFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		IntentFilter iConnectedBluetooth = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
		IntentFilter iDisConnectedBluetooth = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		IntentFilter iStateBluetoothChanged = new IntentFilter(bt.getInstanceAdapter().ACTION_STATE_CHANGED);
		IntentFilter iDiscoveryFinished = new IntentFilter(bt.getInstanceAdapter().ACTION_DISCOVERY_FINISHED);
        registerReceiver(ReceiverBluetooth, iDeviceFound); 
        registerReceiver(ReceiverBluetooth, iConnectedBluetooth);
        registerReceiver(ReceiverBluetooth, iDisConnectedBluetooth);
        registerReceiver(ReceiverBluetooth, iStateBluetoothChanged);
        registerReceiver(ReceiverBluetooth, iDiscoveryFinished);
    }
	public void onClick(View v) {
		if(v.equals(butEnable))
		{
			butDisable.setEnabled(true);
			butScan.setEnabled(true);
			butEnable.setEnabled(false);
			bt.enableBluetooth();
		}	
		else if(v.equals(butDisable))
		{
			try {
				commHandler.closeConnection();
				bt.disableBluetooth();
				devicesList.clear();
				butDisable.setEnabled(false);
				butScan.setEnabled(false);
				butTransmit.setEnabled(false);
				butEnable.setEnabled(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d("gal",e.getMessage());
			}
			
		}
		else if(v.equals(butScan))
		{
			devicesNamesAdapter.clear();
			bt.startScanning();
			Toast.makeText(this,"the bluetooth is scanning",Toast.LENGTH_SHORT).show();
		}
		else if(v.equals(butTransmit))
		{
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
				
				commHandler.writeToRmoteDevice(bos.toByteArray());
				state = 1; // sent T0 to client device
			}
			else
			{
				 //shouldn't have gotten here. 
			}
		}
	}
	protected void onDestroy() {
		bt.disableBluetooth();
		this.unregisterReceiver(ReceiverBluetooth);
		super.onDestroy();
	}
	// this is turn on the client side
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		if(bt.getInstanceClient() != null)
		{
			if(!bt.getInstanceClient().getRunning())
			{
				bt.getInstanceClient().connectionToServer(devicesList.get(position));
				isServer = false;
			}
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
		    	 try {
			    	if(isServer)
			    	{
			    		while(bt.getInstanceServer().getSocket() == null){}
			    		commHandler = new CommHandler(MainActivity.this, bt.getInstanceServer().getSocket());
			    		if(!commHandler.getRunning())
			    		{
			    			commHandler.execute();
			    		}
			    		butTransmit.setEnabled(true);
			    	}
			    	else
			    	{
			    		commHandler = new CommHandler(MainActivity.this, bt.getInstanceClient().getSocket());
			    		if(!commHandler.getRunning())
			    		{
			    			commHandler.execute();
			    		}
			    	}
		    	 } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
		    }
		    else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
		    {
		    	Toast.makeText(context,"the connection is closed",Toast.LENGTH_SHORT).show();
		    }
		    else if(bt.getInstanceAdapter().ACTION_STATE_CHANGED.equals(action))
		    {
		    	if(bt.getInstanceAdapter().isEnabled())
		    	{
		    		if(!bt.getInstanceServer().getRunning())
	    			{
		    			try {
							bt.getInstanceServer().createListen();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
		    		Toast.makeText(context,"the bluetooth is enable",Toast.LENGTH_SHORT).show();
		    	}
		    	else
		    	{
		    		Toast.makeText(MainActivity.this,"the bluetooth is disable",Toast.LENGTH_SHORT).show();
		    	}
		    }
		    else if(bt.getInstanceAdapter().ACTION_DISCOVERY_FINISHED.equals(action))
			{
		    	Toast.makeText(context, "the discovery is finished",Toast.LENGTH_SHORT).show();
			}
		}
		
	};
	public void dataRecive(byte[] data) {
		long value = convertBytesToLong(data);
		tv.setText(String.valueOf(value));
		if (isServer)
		{
			switch(state)
			{
				case 0:
				{
					// do nothing. Main activity will send initial time stamp
					Log.d("gal","is server" + String.valueOf(state));
					break;
				}
				case 1: 
				{
					Long deltaNs = (T3-T0) - value;
					Long deltaNsRoundTrip = (T3-T0);
					String serverText = "Delta found by server: " + String.valueOf(deltaNs) + 
							". Delta roundTrip was " + deltaNsRoundTrip + " delta on Client side was: " + value;
					Toast.makeText(this, serverText , Toast.LENGTH_SHORT).show();
					state = 2;
					break;

				}
			}
		}
		else // CLIENT SIDE: 
		{
			Log.d("gal", "is client and the state is " + String.valueOf(state));
			switch(state)
			{
				case 0:
				{	
					Long tSend = System.nanoTime();
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(bos);
					
					try {
						dos.writeLong(tSend - T3);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					commHandler.writeToRmoteDevice(bos.toByteArray());
					Long tDelta = tSend - T3;
					String clientText = "sent in Client the delta: " + tDelta + 
							" and tSend, tReceive were: " + tSend + " , " + T3;
					Toast.makeText(this, clientText , Toast.LENGTH_SHORT).show();
					break;

				}
				case 1: 
				{
					Log.d("gal","is client" + String.valueOf(state));
					break;

				}
			}

			
		}
	}
	private long convertBytesToLong(byte[] data)
	{
		Long value = (long) 0;
		for (int i = 0; i < data.length; i++)
		{
		   value += ((long) data[i] & 0xffL) << (8 * i);
		}
		return value;
	}
}