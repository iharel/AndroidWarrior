package com.warrior.games;

import com.warrior.main.CommHandler;
import com.warrior.main.CommHandler.IDataRecevieGame;

public class SpeedClick  extends Game implements IDataRecevieGame {

	private int counterClick = 1;
	
	public SpeedClick(CommHandler commHandler, long timeAir) {
		super(commHandler, timeAir);
	}
	
	public int getCounterClick(){
		return counterClick;
	}
	public void addCounterClick(){
		counterClick++;
	}
	public void dataRecevieInGame(long data) {
		// TODO Auto-generated method stub
		
	}
}
