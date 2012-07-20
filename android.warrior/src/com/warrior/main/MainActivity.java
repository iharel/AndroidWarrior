package com.warrior.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
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

import com.gal.bluetooth1.R;
import com.warrior.main.Bluetooth.IServerClosed;
import com.warrior.main.Bluetooth.Server;
import com.warrior.main.CommHandler.IDataRecevie;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener, IDataRecevie {

	private Button butEnable,butClose,butDisable,butScan,butSync,butOpenServer;
	private ListView lv;
	private Bluetooth bt;
	private TextView tv;
	private ArrayAdapter<String> devicesNamesAdapter;
	private List<BluetoothDevice>devicesList;
	private boolean isServer = true;
	private int state = 0;
	private Long sendTime;
	private Long receiveTime = (long) 0;
	private CommHandler commHandler;
	private int counterReceiveData = 0;
	private final static int MAX_RECEIVE = 49;
	private String[] arrAirTime = new String[50];
	private String[] arrTotalTime = new String[50];
	private long avgAirTime = 0,avgTotalRoundTrip = 0;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        bt = new Bluetooth(this);        
        devicesNamesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        devicesList = new ArrayList<BluetoothDevice>();
        buildViewReferece();
        buildIntentFilter();
        BuildEvents();
        if(bt.isEnabled())
        {
        	buttonsForBtEnabled();
        }
    }
    private void buildViewReferece()
    {
    	  butEnable = (Button)findViewById(R.id.butEnable);
          butClose = (Button)findViewById(R.id.butClose);
          butDisable = (Button)findViewById(R.id.butDisable);
          butScan = (Button)findViewById(R.id.butScan);
          butSync = (Button)findViewById(R.id.butSync);
          butOpenServer = (Button)findViewById(R.id.butOpenServer);
          tv = (TextView)findViewById(R.id.tv);
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
  		registerReceiver(ReceiverBluetooth, iDeviceFound); 
  		registerReceiver(ReceiverBluetooth, iConnectedBluetooth);
  		registerReceiver(ReceiverBluetooth, iDisConnectedBluetooth);
  		registerReceiver(ReceiverBluetooth, iStateBluetoothChanged);
  		registerReceiver(ReceiverBluetooth, iDiscoveryFinished);
    }
    private void BuildEvents()
    {
    	   lv.setOnItemClickListener(this);
           butEnable.setOnClickListener(this);
           butClose.setOnClickListener(this);
           butDisable.setOnClickListener(this);
           butScan.setOnClickListener(this);
           butSync.setOnClickListener(this);
           butOpenServer.setOnClickListener(this);
    }
	public void onClick(View v) {
		if(v.equals(butEnable))
		{
			bt.enableBluetooth();
		}	
		else if(v.equals(butDisable))
		{
			bt.disableBluetooth();
		}
		else if(v.equals(butClose))
		{
			commHandler.writeToRmoteDevice(commHandler.DISCONNECTED);
		}
		else if(v.equals(butScan))
		{
			bt.startScanning();
			devicesNamesAdapter.clear();
			Toast.makeText(this,"the bluetooth is scanning",Toast.LENGTH_SHORT).show();
		}
		else if(v.equals(butSync))
		{
			
			if (isServer)
			{
				
				for(int i=0;i<MAX_RECEIVE;i++)
				{
					sendTime = getTime();
					commHandler.writeToRmoteDevice(sendTime);
					Log.d("gal","time server sends data: " + sendTime);
					state = 1; // sent T0 to client device
				}
			}
			
		}
		else if(v.equals(butOpenServer))
		{
			try {
				butOpenServer.setEnabled(false);
				Server server = bt.getCreateServer();
				server.setListenerCloseServer(new IServerClosed() {
					public void serverClosed() {
						butOpenServer.setEnabled(true);
					}
				});
				server.createListen();
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
			bt.getCreateClient().connectionToServer(devicesList.get(position));
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	// Create a BroadcastReceiver
	private final BroadcastReceiver ReceiverBluetooth = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
		    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        devicesList.add(device);
		        devicesNamesAdapter.add(device.getName() + "\n" + device.getAddress());
		        lv.setAdapter(devicesNamesAdapter);
		    }
		    
		    else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
		    {
		    	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		    	try {
		    		if(isServer)
			    	{
			    		if(commHandler == null)
			    		{
				    		while(bt.getSocketServer() == null){}
				    		commHandler = new CommHandler(MainActivity.this, bt.getSocketServer());
				    		commHandler.execute();
				    		commHandler.setListenerDataRecevie(MainActivity.this);
				    		butSync.setEnabled(true);
			    		}
			    	}
			    	else
			    	{
			    		if(commHandler == null)
			    		{
				    		commHandler = new CommHandler(MainActivity.this, bt.getSocketClient());
				    		commHandler.setListenerDataRecevie(MainActivity.this);
				    		commHandler.execute();
			    		}
			    	}
		    	 } catch (IOException e) {
		    			Toast.makeText(context, e.getMessage() + device.getName(), Toast.LENGTH_SHORT).show();
				}
			    Toast.makeText(context, "you connected to " + device.getName(), Toast.LENGTH_SHORT).show();
		    	buttonsForBtConnected();
			   	devicesNamesAdapter.clear();
		    }
		    else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
		    {
		    	if(commHandler != null)
		    	{
		    		commHandler = null;
		    	}
		    	if(bt != null)
		    	{
		    		bt.resetSockets();
		    		bt = null;
		    	}
		    	System.gc();
		    	bt = new Bluetooth(MainActivity.this);
		    	Toast.makeText(context,"the connection is closed",Toast.LENGTH_SHORT).show();
		    	buttonsForBtEnabled();
		    	devicesNamesAdapter.clear();
		    }
		   
		    else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
		    {
		    	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
		    	switch (state) {
		            case BluetoothAdapter.STATE_OFF:
		            	buttonsForBtDisabled();
			    		devicesList.clear();
			    		Toast.makeText(MainActivity.this,"the bluetooth is disable",Toast.LENGTH_SHORT).show();
		                break;
		            case BluetoothAdapter.STATE_ON:
		            	buttonsForBtEnabled();
			    		Toast.makeText(context,"the bluetooth is enable",Toast.LENGTH_SHORT).show();
		                break;
	            }
		    }
		    else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
		    	Toast.makeText(context, "the scanning is finished",Toast.LENGTH_SHORT).show();
			}
		}
		
	};
	private void buttonsForBtEnabled()
	{
		butDisable.setEnabled(true);
		butScan.setEnabled(true);
		butOpenServer.setEnabled(true);
		butEnable.setEnabled(false);
		butSync.setEnabled(false);
	}
	private void buttonsForBtDisabled()
	{
		butEnable.setEnabled(true);
		butDisable.setEnabled(false);
		butScan.setEnabled(false);
		butSync.setEnabled(false);
		butClose.setEnabled(false);
		butOpenServer.setEnabled(false);
	}
	private void buttonsForBtConnected()
	{
		butClose.setEnabled(true);
	    butOpenServer.setEnabled(false);
	   	butDisable.setEnabled(false);
	   	butScan.setEnabled(false);
	}
	public void setReceiveTime(long time) {
		this.receiveTime = time;
	}
	public long getReceiveTime() {
		return this.receiveTime;
	}
	public long getTime() {
		return System.currentTimeMillis();// nanoTime();
	}
	private void writeToSdcard()
	{
		String filename = "Results.txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		try {
		    PrintWriter pw = new PrintWriter(new FileOutputStream(file));
		    for(int i=0;i<MAX_RECEIVE;i++)
		    {
		    	pw.println("number round is:" + i);
		    	pw.println(arrAirTime[i]);
		    	pw.println(arrTotalTime[i]);
		    	pw.println("********************************************************");
		    }
		    pw.println("********************************************************");
		    pw.println("the air time average is:" + avgAirTime/MAX_RECEIVE);
		    pw.println("the toatl ronud trip average is:" + avgTotalRoundTrip/MAX_RECEIVE);
		    pw.println("********************************************************");
		    pw.flush();
		    pw.close();
		} catch (FileNotFoundException e) {
		    try {
				file.createNewFile();
				writeToSdcard();
			} catch (IOException e1) {
				Log.d("gal",e.getMessage());
				e1.printStackTrace();
			}
		} catch (IOException e) {
			Log.d("gal",e.getMessage());
		}


	}
	public void dataRecevie(long data) {
		if (isServer)
		{
			switch(state)
			{
				case 0:
				{
					// do nothing. Main activity will send initial time stamp
					break;
				}
				case 1: 
				{
					long deltaInClientSide = data;
					Long deltaNsRoundTrip = (receiveTime-sendTime);
					Long airTimeTotal = deltaNsRoundTrip - deltaInClientSide;
					String serverText = "Delta found by server: " + String.valueOf(airTimeTotal) + 
							". Delta roundTrip was " + deltaNsRoundTrip + " delta on Client side was: " + deltaInClientSide;
					
					Log.d("gal","round trip is:" + deltaNsRoundTrip);
					Log.d("gal","air time is:" + airTimeTotal);
					arrAirTime[counterReceiveData] = "air time is:" + airTimeTotal;
					arrTotalTime[counterReceiveData] = "round trip is:" + deltaNsRoundTrip;
					avgAirTime += airTimeTotal;
					avgTotalRoundTrip += deltaNsRoundTrip;
					Log.d("gal","counter is " + counterReceiveData);
					if(counterReceiveData == MAX_RECEIVE)
					{
						writeToSdcard();
						Toast.makeText(this, serverText , Toast.LENGTH_SHORT).show();
					    counterReceiveData = 0;
					}
					counterReceiveData++;
					//state = 2;
					break;

				}
			}
		}
		else // CLIENT SIDE: 
		{
			switch(state)
			{
				case 0:

				{// received first sync message. receiveTime should contain time T1 (when T0 message was received) 	

					sendTime=getTime();
 					long deltaInsideClient = sendTime - receiveTime;
					
					commHandler.writeToRmoteDevice(deltaInsideClient);
					Long tDelta = sendTime - receiveTime;
					commHandler.writeToRmoteDevice(tDelta);
					String clientText = "sent in Client the delta: " + tDelta + 
							" and tSend, tReceive were: " + sendTime + " , " + receiveTime;
					Log.d("gal", "sent message to server in time:  " + String.valueOf(sendTime));

					//Toast.makeText(this, clientText , Toast.LENGTH_SHORT).show();
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
}