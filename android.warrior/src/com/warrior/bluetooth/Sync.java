package com.warrior.bluetooth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import com.warrior.main.MyApp;
import com.warrior.main.MyApp.APP_STATES;
import com.warrior.main.MyApp.IDataReceiveSync;

import android.os.Environment;
import android.util.Log;

public class Sync implements IDataReceiveSync {
	
	private String[] arrAirTime = new String[MAX_PACKEAGES + 1];
	private String[] arrTotalTime = new String[MAX_PACKEAGES + 1];
	private long avgAirTime = 0,avgTotalRoundTrip = 0;
	private ServerSync server;
	private ClientSync client;
	private boolean isServer;
	private CommHandler commHandler;
	private final static int MAX_PACKEAGES = 49;
	public final static long SYNC_FINISH = 101;
	private IFinishSync iFinishSync;
	private long timeAir = 0;
	
	public Sync(MyApp app){
		this.commHandler = app.getCommHandler();
		this.isServer = app.isSever();
		server = new ServerSync();
		client = new ClientSync();
		app.setDataRecevieSyncListener(this);
	}
	
	public void startSync(){
		server.serverStartSync();
	}
	public void setListenerFinishSync(IFinishSync iFinishSync)
	{
		this.iFinishSync = iFinishSync;
	}
	public static long getSystemTime(){
		return System.currentTimeMillis();
	}
	public static String getDateTime(){
		return " date: " + Calendar.getInstance().getTime().toString();
	}
	public static void waitTime(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public long getTimeAir()
	{
		return timeAir; 
	}
	public void dataRecevieSync(Long[] values) {
		if(isServer)
		{
			server.syncRunning(values);
		}
		else
		{
			client.syncRunning(values);
		}
		
	}
	private class ServerSync
	{
		private int counterReceiveData = 0;
		private final static int MAX_RECEIVE_DATA = MAX_PACKEAGES;
		private Sync_State syncState = Sync_State.NOT_YET;
		private long sendTime = 0;
		
		public void serverStartSync()
		{
			// send the first package to client
			syncState = Sync_State.RUNNING;
			sendTime = getSystemTime();
			commHandler.writeToRmoteDevice(sendTime);
		}
		
		public void syncRunning(Long[] values)
		{
			long deltaInClientSide = values[0];
			long receiveTime = values[1];
			switch(syncState)
			{
				case NOT_YET:
				{
					// do nothing. Main activity will send initial time stamp
					break;
				}
				case RUNNING: 
				{
					counterReceiveData++;
					long deltaNsRoundTrip = (receiveTime-sendTime);
					long airTimeTotal = deltaNsRoundTrip - deltaInClientSide;
					arrAirTime[counterReceiveData] = "air time is:" + airTimeTotal;
					arrTotalTime[counterReceiveData] = "round trip is:" + deltaNsRoundTrip;
					avgAirTime += airTimeTotal;
					avgTotalRoundTrip += deltaNsRoundTrip;
					if(counterReceiveData >= MAX_RECEIVE_DATA)
					{
						writeResultSync();
						iFinishSync.finishSync();
						timeAir = (avgAirTime/(MAX_PACKEAGES*2));
					}
					sendTime = getSystemTime();
					commHandler.writeToRmoteDevice(sendTime);
					break;

				}
			}
		}
	}
	private class ClientSync
	{
		private int counterSendData = 0;
		private final static int MAX_SEND_DATA = MAX_PACKEAGES;
		
		public void syncRunning(Long[] values)
		{
			long receiveTime = values[1];
			if(counterSendData >= MAX_SEND_DATA)
			{
				timeAir = (avgAirTime/(MAX_PACKEAGES/2));
				iFinishSync.finishSync();
				return;
			}
			long sendTime=getSystemTime();
			long deltaInsideClient = sendTime - receiveTime;
			commHandler.writeToRmoteDevice(deltaInsideClient);
			counterSendData++;
		}
	}
	public void resetSync()
	{
		server.counterReceiveData = 0;
		server.syncState = Sync_State.NOT_YET;
		client.counterSendData = 0;
		avgAirTime = 0;
		avgTotalRoundTrip = 0;
	}
	private void writeResultSync()
	{
		String filename = "SyncResults.txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		try {
		    PrintWriter pw = new PrintWriter(new FileOutputStream(file));
		    for(int i=0;i<MAX_PACKEAGES;i++)
		    {
		    	pw.println("number round is:" + i);
		    	pw.println(arrAirTime[i]);
		    	pw.println(arrTotalTime[i]);
		    	pw.println("********************************************************");
		    }
		    pw.println("********************************************************");
		    pw.println("the air time average is:" + avgAirTime/MAX_PACKEAGES);
		    pw.println("the toatl ronud trip average is:" + avgTotalRoundTrip/MAX_PACKEAGES);
		    pw.println("********************************************************");
		    pw.flush();
		    pw.close();
		} catch (FileNotFoundException e) {
		    try {
				file.createNewFile();
				writeResultSync();
			} catch (IOException e1) {
				Log.d("gal",e.getMessage());
				e1.printStackTrace();
			}
		}
	}
	public interface IFinishSync
	{
		void finishSync();
	}
	private enum Sync_State
	{
		NOT_YET,
		RUNNING,
		FINISH
	}
}


