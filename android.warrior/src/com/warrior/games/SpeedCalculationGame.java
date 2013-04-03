package com.warrior.games;

import java.util.Timer;

import android.os.AsyncTask;
import android.util.Log;

import com.warrior.bluetooth.Sync;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.main.MyApp;

public class SpeedCalculationGame extends Game {

	public static final int MAX_ANSWER_COUNT = 10;
	private int myAnswerCount = 0;
	private Exercise currentExercise;
	
	public SpeedCalculationGame(MyApp app) {
		super(app);
		// TODO Auto-generated constructor stub
	}
	public void start() {
		super.start();
		new Run();
	}
	class Run extends AsyncTask<Void,Integer,Void>{
		public Run()
		{
			Log.d("gal","in SpeedTestGame - start2");
			this.execute();
			Log.d("gal","in SpeedTestGame - start3");

		}
		protected Void doInBackground(Void... arg0) {
			Log.d("gal","into doInBackground of RunningGame");
			startTime = Sync.getSystemTime();
			while(myAnswerCount < MAX_ANSWER_COUNT)
			{
				// check if the other device is finished the game
				if(otherDeviceFinishedGame){
					break;
				}
				
				if(isClose){
					break;
				}	
			}
			return null;
		}
		protected void onPostExecute(Void result) {
			if(otherDeviceFinishedGame || !isClose){
				sendFinish();
			}
		}
	}
	public boolean addCounterClick(){
		boolean retValue = false;
		if(state == GAME_STATES.RUNNING){
			if(myAnswerCount >= MAX_ANSWER_COUNT){
				myAnswerCount = MAX_ANSWER_COUNT;
			}
			else if(myAnswerCount <= MAX_ANSWER_COUNT){
				myAnswerCount++;
				commHandler.writeToRmoteDevice((long)myAnswerCount);
				iStateGame.myStatusGame(myAnswerCount);
				retValue = true;
			}
			
		}
		return retValue;
	}
	
	public void dateRecevieRunningGame(long data) {
		super.dateRecevieRunningGame(data);  
		if(data >= 0 && data < MAX_ANSWER_COUNT){
			int competitorAnswerCount = (int)data;
			iStateGame.competitorStatusGame(competitorAnswerCount);
		}
	}
	public boolean checkAnswer(int answer){
		// every answer wrong add 300 milSec to game time of device
		if(currentExercise.getRightAnswer() == answer){
			return true;
		}
		gameTimeMyDevice += 300;
		return false;
	}
	public int[] getResults(){
		return currentExercise.getResults();
	}
	public int getRightAnswer(){
		return currentExercise.getRightAnswer();
	}
	public String getExercise(){
		return currentExercise.getExercise();
	}
	public void createExercise(){
		currentExercise = new Exercise();
	}
	public String toString(){
		return "speed calculation " + Sync.getDateTime();
	}
	public void reset() {
		super.reset();
		myAnswerCount = 0;
	}
	
	
	public class Exercise{
		private int[] results; 
		private static final int MAX_LOTTERY_NUMBER = 100;
		private static final int MAX_NUMBER_TO_EXERCISE = 10;
		private String strExercise = "";
		private int rightAnswer = 0;
		
		public Exercise() {
			results = new int[4];
			createResults();
		}
		public String getExercise() {
			return strExercise;
		}
		public int[] getResults(){
			return results;
		}
		public int getRightAnswer(){
			return rightAnswer;
		}
		
		private void createResults(){
			int temp = 0;
			// number of right answer
			int rightAnswerNumber = lotteryNumber(4);
			
			rightAnswer = createRightAnswer();
			for (int i = 0; i < results.length; i++) {
				if(i== rightAnswerNumber){ 
					results[i] = rightAnswer;
					continue;
				}
				while(true){
					temp = lotteryNumber(MAX_LOTTERY_NUMBER);
					if(!isFoundAnswer(temp)){
						results[i] = temp;
						break;
					}
				}
			}
		}
		private boolean isFoundAnswer(int answer){
			boolean isFound = false;
			for (int j = 0; j < results.length; j++) {
				if(results[j] == answer){
					isFound = true;
					break;
				}
			}
			return isFound;
		}
		private int createRightAnswer(){
			int num1,num2,result = 0;
			int action = lotteryNumber(4);
			num1 = lotteryNumber(MAX_NUMBER_TO_EXERCISE);
			num2 = lotteryNumber(MAX_NUMBER_TO_EXERCISE);
			switch(action){
				case 0:{
					strExercise = num1 + "+" + num2;
					result = num1+num2;
					break;
				}
				case 1:{
					strExercise = num1 + "-" + num2;
					result = num1-num2;
					break;
				}
				case 2:{
					strExercise = num1 + "*" + num2;
					result = num1*num2;
					break;
				}
				default:{
					while(num1%num2 != 0){
						num1 = lotteryNumber(MAX_NUMBER_TO_EXERCISE);
						num2 = lotteryNumber(MAX_NUMBER_TO_EXERCISE);
					}
					strExercise = num1 + "/" + num2;
					result = num1/num2;
					break;
				}
			}
			return result;
		}
		private int lotteryNumber(int maxNumber){
			int num = (int)(Math.random() * maxNumber);
			if(num == 0){
				num = 1;
			}
			return num;
		}
		private int lotteryNumber(int percentNum,int maxNumber){
			int num = (int)(Math.random() * maxNumber);
			if(num == 0){
				num = 1;
			}
			num = (int)((num * percentNum)/100);
			return num;
		}
	}

}
