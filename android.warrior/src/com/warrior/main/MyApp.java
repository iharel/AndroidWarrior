package com.warrior.main;

import android.app.Application;

public class MyApp extends Application {

	private CommHandler commHandler;
	private boolean isServer;
	private long timeAir;
	public void setCommHndler(CommHandler commHandler)
	{
		this.commHandler = commHandler;
	}
	public CommHandler getCommHndler()
	{
		return commHandler;
	}
	public void setIsServer(boolean isServer)
	{
		this.isServer = isServer;
	}
	public boolean getIsSever()
	{
		return isServer;
	}
	public void setTimeAir(long timeAir)
	{
		this.timeAir = timeAir;
	}
	public long getTimeAir()
	{
		return timeAir;
	}
}
