package com.warrior.games.activitys;

import com.androidWarrior.R;
import com.warrior.games.Game.GAME_STATES;
import com.warrior.games.SpeedTestPhoneGame;
import com.warrior.main.MyApp;

import android.os.Bundle;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpeedTestPhoneGameActivity extends GameActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	protected void buildViewReference(){
		setContentView(R.layout.activity_speed_test_phone);
		super.buildViewReference();
	}
	protected void buildGame(){
		MyApp app = (MyApp)this.getApplication();
		game = new SpeedTestPhoneGame(app);
		super.buildGame();
	}

}
