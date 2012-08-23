package com.warrior.main;

import com.androidWarrior.R;
import com.warrior.games.SpeedTestPhoneClient;
import com.warrior.games.SpeedTestPhoneServer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SelectGameActivity extends Activity{
	
	private boolean isServer;
	private Button butSpeedTestPhone,butBack;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_game);
		
		MyApp myApp = (MyApp)getApplication();
		isServer = myApp.getIsSever();
		
		butSpeedTestPhone = (Button)findViewById(R.id.butSpeedTestPhone);
		butBack = (Button)findViewById(R.id.butBack);
		butSpeedTestPhone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(isServer)
				{
					Intent iGame = new Intent(SelectGameActivity.this,SpeedTestPhoneServer.class);
					startActivity(iGame);
				}
				else
				{
					Intent iGame = new Intent(SelectGameActivity.this,SpeedTestPhoneClient.class);
					startActivity(iGame);
				}
			}
		});
		butBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}
