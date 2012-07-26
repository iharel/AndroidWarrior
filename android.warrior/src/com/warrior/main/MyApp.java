package com.warrior.main;

import android.app.Application;

public class MyApp extends Application {

	private CommHandler commHandler;
	private boolean isServer;
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
}
