package com.warrior.games.activitys;

import com.androidWarrior.R;
import com.warrior.games.SpeedCalculationGame;
import com.warrior.games.SpeedCalculationGame.Exercise;
import com.warrior.games.SpeedClickGame;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.main.MyApp;

import android.os.Bundle;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpeedCalculationGameActivity extends GameActivity implements OnClickListener{
	TextView tvQuestion;
	private Button[] butsAnswers = new Button[4]; 
	private SpeedCalculationGame speedCalculationGame;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		speedCalculationGame = (SpeedCalculationGame)game;
	}
	protected void buildViewReference(){
		setContentView(R.layout.activity_spped_calculation);
		super.buildViewReference();
		butsAnswers[0] = (Button)findViewById(R.id.butAnswerA);
		butsAnswers[1] = (Button)findViewById(R.id.butAnswerB);
		butsAnswers[2] = (Button)findViewById(R.id.butAnswerC);
		butsAnswers[3] = (Button)findViewById(R.id.butAnswerD);
		tvQuestion= (TextView)findViewById(R.id.tvQuestion);
	}
	protected void buildViewEvents(){
		super.buildViewEvents();
		for (int i = 0; i < butsAnswers.length; i++) {
			butsAnswers[i].setOnClickListener(this);
		}
	}
	protected void buildGame(){
		MyApp app = (MyApp)this.getApplication();
		game = new SpeedCalculationGame(app);
		super.buildGame();
	}
	@Override
	public void startGame() {
		super.startGame();
		for (int i = 0; i < butsAnswers.length; i++) {
			butsAnswers[i].setEnabled(true);
		}
		nextExercise();
	}
	public void myStatusGame(int myStatus) {
		super.myStatusGame(myStatus * 10);
	}
	public void competitorStatusGame(int competitorStatus) {
		super.competitorStatusGame(competitorStatus * 10);
	}
	public void finishGame(boolean isWinner) {
		super.finishGame(isWinner);
		for (int i = 0; i < butsAnswers.length; i++) {
			butsAnswers[i].setEnabled(false);
		}
	}
	protected void resetGameActivity(){
		super.resetGameActivity();
		tvQuestion.setText("");
		for (int i = 0; i < butsAnswers.length; i++) {
			butsAnswers[i].setText("");
			
		}
	}
	public void onClick(View v) {
		Button but = (Button)v;
		if(!game.isPause()){
			speedCalculationGame.addCounterClick();
			int answer = Integer.parseInt(but.getText().toString());
			speedCalculationGame.checkAnswer(answer);
			nextExercise();
		}
	}
	private void nextExercise(){
		speedCalculationGame.createExercise();
		int[] results = speedCalculationGame.getResults();
		tvQuestion.setText(speedCalculationGame.getExercise());
		for (int i = 0; i < butsAnswers.length; i++) {
			butsAnswers[i].setText(String.valueOf(results[i]));
		}
	}
}
