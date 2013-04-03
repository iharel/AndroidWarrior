package com.warrior.games.activitys;


import com.androidWarrior.R;

import android.content.Context;
import android.test.PerformanceTestCase.Intermediates;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TimePicker;

public class MenuGamePopup {

	private Context context;
	private PopupWindow pop;
	private Button butUploadPost,butResume,butRematch,butClose; 
	private LinearLayout llMain;
	private LayoutInflater inflater;
	private IClickMenuGame onClickButton;
	
	public MenuGamePopup(Context context)
	{
		this.context = context;
		buildPopupWindows();
		buildViewReference();
		buildViewEvents();
	}
	public void setOnClickButton(IClickMenuGame onClickButton){
		this.onClickButton = onClickButton;
	}
	private void buildPopupWindows()
	{
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		llMain  = (LinearLayout)inflater.inflate(R.layout.popup_menu_game,null);

		int width = WindowManager.LayoutParams.WRAP_CONTENT;
		int height = WindowManager.LayoutParams.WRAP_CONTENT;
		
		pop = new PopupWindow(llMain, width, height);
	}
	private void buildViewReference()
	{
		butUploadPost = (Button)llMain.findViewById(R.id.butUploadPost);
		butResume = (Button)llMain.findViewById(R.id.butResume);
		butRematch = (Button)llMain.findViewById(R.id.butRematch);
		butClose = (Button)llMain.findViewById(R.id.butClose);
	}
	private void buildViewEvents(){
		butUploadPost.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onClickButton.selectOption(TYPE_CLICK.uploadPost);
			}
		});
		butResume.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onClickButton.selectOption(TYPE_CLICK.resume);
			}
		});
		butRematch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onClickButton.selectOption(TYPE_CLICK.rematch);
			}
		});
		butClose.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onClickButton.selectOption(TYPE_CLICK.close);
			}
		});
	}
	public boolean isShowing(){
		return pop.isShowing();
	}
	public void show(){
		int horisontal = Gravity.BOTTOM;
		int locationX = Gravity.LEFT;
		int locationY = Gravity.CENTER_VERTICAL;
		pop.showAtLocation(inflater.inflate(R.layout.activity_main,null)
				,horisontal , locationX, locationY);
	}
	public void dismiss(){
		pop.dismiss();
	}
	
	public interface IClickMenuGame {
		void selectOption(TYPE_CLICK type);
	}
	public enum TYPE_CLICK{
		uploadPost,
		resume,
		rematch,
		close
	}
}
