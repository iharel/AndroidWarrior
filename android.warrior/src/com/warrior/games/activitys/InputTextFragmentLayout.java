package com.warrior.games.activitys;


import com.androidWarrior.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.test.PerformanceTestCase.Intermediates;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TimePicker;

public class InputTextFragmentLayout extends Fragment {

	private EditText et;
	private Button butAccept,butCancel;
	private IClickButton onClickButton;
	private Context context; 
	private LinearLayout llMain;
	private LayoutInflater inflater;
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_activity_input_text,
	        container, false);
	    return view;
	}
	private void buildViewReference()
	{
		et = (EditText)llMain.findViewById(R.id.et);
		butAccept = (Button)llMain.findViewById(R.id.butUploadPost);
		butCancel = (Button)llMain.findViewById(R.id.butResume);
	}
	private void buildViewEvents(){
		butAccept.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String text = et.getText().toString();
				onClickButton.selectOption(TYPE_CLICK.accept,text);
			}
		});
		butCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onClickButton.selectOption(TYPE_CLICK.cancel,null);
			}
		});
	}
	public boolean isShowing(){
		return isShowing();
	}
	public void finish(){
		finish();
	}
	
	public interface IClickButton {
		void selectOption(TYPE_CLICK type,String text);
	}
	public void setOnClickButton(IClickButton onClickButton){
		this.onClickButton = onClickButton;
	}
	public enum TYPE_CLICK{
		accept,
		cancel
	}
}
