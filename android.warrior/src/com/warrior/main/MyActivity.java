package com.warrior.main;

import com.warrior.main.MyApp.IBluetoothChanged;
import com.warrior.main.MyFacebook.ISessionState;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MyActivity extends Activity implements IBluetoothChanged, ISessionState{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApp app = (MyApp)getApplication();
		app.getFacebook().setStateLoginListener(this);
	}
	@Override
	protected void onResume() {
		super.onResume();
		MyApp app = (MyApp)getApplication();
		app.setBluetoothChangedListener(this);
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == MyApp.RECEIVE_BLUETOOTH_DISCONNECTED){
			bluetoothStateConnectionChanged(false,null);
		}
	}
	public void bluetoothStateConnectionChanged(boolean isConnected,String error) {
		if(!isConnected){
			setResult(MyApp.RECEIVE_BLUETOOTH_DISCONNECTED);
			finish();
		}
	}
	@Override
	public void bluetoothStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void facebookOpenSession(boolean isSuccess, String exception) {
		if(isSuccess){
			MyApp app = (MyApp)getApplication();
			MyFacebook fb =app.getFacebook(); 
			// add new permission for publish post on wall 
			if(!fb.isFoundPermission(MyFacebook.PUBLISH_PERMISSION)){
				fb.addPermission(this, MyFacebook.PUBLISH_PERMISSION);
			}
		}
		else{
			Toast.makeText(this, exception, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void facebookCloseSession() {
		
	}	


}
