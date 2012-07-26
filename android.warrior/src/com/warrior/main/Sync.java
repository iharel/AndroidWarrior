package com.warrior.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import com.warrior.main.CommHandler.IDataRecevieOfSync;
import android.os.Environment;
import android.util.Log;

public class Sync implements IDataRecevieOfSync {
	
	private String[] arrAirTime = new String[MAX_PACKEAGES];
	private String[] arrTotalTime = new String[MAX_PACKEAGES];
	private long avgAirTime = 0,avgTotalRoundTrip = 0;
	private ServerSync server;
	private ClientSync client;
	private boolean isServer;
	private CommHandler commHandler;
	private final static int MAX_PACKEAGES = 200;
	public final static long SYNC_FINISH = 101;
	private IEndSync iEndSync;
	
	public Sync(CommHandler commHandler, boolean isServer)
	{
		this.commHandler = commHandler;
		this.isServer = isServer;
		server = new ServerSync();
		client = new ClientSync();
		commHandler.setListenerDataRecevieOfSync(this);
	}
	
	public void startSync()
	{
		server.serverStartSync();
	}
	public static long getTime()
	{
		return System.currentTimeMillis();
	}
	public void dataRecevieOfSync(Long[] values) {
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
		private Sync_State syncState = Sync_State.NOT_START;
		private long sendTime = 0;
		
		public void serverStartSync()
		{
			// send the first package to client
			syncState = Sync_State.RUNNING;
			sendTime = getTime();
			commHandler.writeToRmoteDevice(sendTime);
		}
		
		public void syncRunning(Long[] values)
		{
			long deltaInClientSide = values[0];
			long receiveTime = values[1];
			switch(syncState)
			{
				case NOT_START:
				{
					// do nothing. Main activity will send initial time stamp
					break;
				}
				case RUNNING: 
				{
					
					long deltaNsRoundTrip = (receiveTime-sendTime);
					long airTimeTotal = deltaNsRoundTrip - deltaInClientSide;

					arrAirTime[counterReceiveData] = "air time is:" + airTimeTotal;
					arrTotalTime[counterReceiveData] = "round trip is:" + deltaNsRoundTrip;
					avgAirTime += airTimeTotal;
					avgTotalRoundTrip += deltaNsRoundTrip;
					Log.d("gal","the number of package is " + counterReceiveData);
					if(counterReceiveData >= MAX_RECEIVE_DATA)
					{
						writeToSdcard();
					    counterReceiveData = 0;
					    commHandler.writeToRmoteDevice(SYNC_FINISH);
					    iEndSync.endSync();
					    break;
					}
					sendTime = getTime();
					commHandler.writeToRmoteDevice(sendTime);
					counterReceiveData++;
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
				counterSendData = 0;
				iEndSync.endSync();
				return;
			}
			long sendTime=getTime();
			long deltaInsideClient = sendTime - receiveTime;
			commHandler.writeToRmoteDevice(deltaInsideClient);
			counterSendData++;
		}
	}
	private void writeToSdcard()
	{
		String filename = "Results.txt";
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
				writeToSdcard();
			} catch (IOException e1) {
				Log.d("gal",e.getMessage());
				e1.printStackTrace();
			}
		}
	}
	public long getTimeAir()
	{
		long airTime = (avgAirTime/MAX_PACKEAGES/2); 
		return airTime;
	}
	interface IEndSync
	{
		void endSync();
	}
	public void setListenerEndSync(IEndSync iEndSync)
	{
		this.iEndSync = iEndSync;
	}
}
enum Sync_State
{
	NOT_START,
	RUNNING,
	FINISH
}

