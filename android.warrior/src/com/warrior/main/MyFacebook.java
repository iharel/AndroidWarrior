package com.warrior.main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;

import com.androidWarrior.R;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog.FeedDialogBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class MyFacebook implements StatusCallback{

	private ISessionState iSessionState;
	private GraphUser user;
	public static final String PUBLISH_PERMISSION = "publish_actions";
	private static final String LOG_FACEBOOK = "facebook"; 
	
	public Session getCurrentSession(){
		return Session.getActiveSession();
	}
	public GraphUser getUser() {
		return user;
	}
	public boolean isOpendConnection() {
		return getCurrentSession().isOpened();
	}
	public boolean isReadyOpenConnection(){
		boolean isReady = false;
		if(getCurrentSession() == null ||
				!isOpendConnection()){
			isReady = true;
		}
		return isReady;
	}
	public void setStateLoginListener(ISessionState iSessionState){
		this.iSessionState = iSessionState;
	}
	public void openSession(Activity activity){
		// start Facebook Login
	    Session.openActiveSession(activity, true, this); 
	}
	public void closeSession(){
		if(getCurrentSession() == null){
			return;
		}
		getCurrentSession().closeAndClearTokenInformation();
		iSessionState.facebookCloseSession();
		user = null;
	}
	public void showUploadPostDialog(Activity activity){
		FeedDialogBuilder dialog = new FeedDialogBuilder(activity, getCurrentSession());
	    dialog.build().show();
	}
	public void addPermission(Activity activity,String permission){
		NewPermissionsRequest permissions = new NewPermissionsRequest
				(activity, Arrays.asList(permission));
		getCurrentSession().requestNewPublishPermissions(permissions);
	}
	public boolean isFoundPermission(String permissionSearch){
		boolean retValue = false;
		for (String permission : getCurrentSession().getPermissions()) {
			Log.d(LOG_FACEBOOK,permission);
			if(permissionSearch.equals(permission)){
				retValue = true;
				break;
			}
		}
		Log.d(LOG_FACEBOOK,"isFound=" + retValue);
		return retValue;
	}
	public void writePost(String post,final Activity activity){
		Request.executeStatusUpdateRequestAsync(getCurrentSession(), post ,new Callback() {
			public void onCompleted(Response response) {
				String text = "post uploaded";
				if(response.getError() != null){
					text = response.getError().getErrorMessage();
					Log.d(LOG_FACEBOOK,text);
				}
				Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
			}
		});
	}
	private void sesionsOpend(){
		  // make request to the /me API
	      Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {

	        // callback after Graph API response with user object
	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	        	if (user != null) {
	        		MyFacebook.this.user = user;
	        		iSessionState.facebookOpenSession(isOpendConnection(), null);
	        	}
	        }
	      });
	  }
    // callback when session changes state
    public void call(Session s, SessionState state, Exception exception) {
  	  Session.setActiveSession(s);
  	  if(exception != null){
  		  Log.d(LOG_FACEBOOK,"exception: " + exception.getMessage());
  		  iSessionState.facebookOpenSession(false,exception.getMessage());
  	  }
  	  switch(state){
	    	  case OPENING:{
	    		  Log.d(LOG_FACEBOOK,"opening");
	    		  break;
	    	  }
	    	  case OPENED:{
	    		  Log.d(LOG_FACEBOOK,"opend");
	    		  sesionsOpend();
	    		  break;
	    	  }
	    	  case CLOSED:{
	    		  Log.d(LOG_FACEBOOK,"closed");
	    		  break;
	    	  }
	    	  case CLOSED_LOGIN_FAILED:{
	    		  Log.d(LOG_FACEBOOK,"closed login falied");
	    		  break;
	    	  }
	    	  case CREATED:{
	    		  Log.d(LOG_FACEBOOK,"created");
	    		  break;
	    	  }
	    	  case OPENED_TOKEN_UPDATED:{
	    		  Log.d(LOG_FACEBOOK,"opened token uptdated");
	    		  break;
	    	  }
	    	  case CREATED_TOKEN_LOADED:{
	    		  Log.d(LOG_FACEBOOK,"created token loaded");
	    		  break;
	    	  }
  	  }
  	 
    }
	interface ISessionState{
		void facebookOpenSession(boolean isSuccess,String exception);
		void facebookCloseSession();
	}
	
	
	
	
	
	
	
	static public void printHashKey(Context context,String packageName) {

	      try {
	          PackageInfo info = context.getPackageManager().getPackageInfo(packageName,
	                  PackageManager.GET_SIGNATURES);
	          for (Signature signature : info.signatures) {
	              MessageDigest md = MessageDigest.getInstance("SHA");
	              md.update(signature.toByteArray());
	              Log.d("TEMPTAGHASH KEY:",
	                      Base64.encodeToString(md.digest(), Base64.DEFAULT));
	              
	          }
	      } catch (NameNotFoundException e) {
	    	  Log.d(LOG_FACEBOOK,e.getMessage());

	      } catch (NoSuchAlgorithmException e) {
	    	  Log.d(LOG_FACEBOOK,e.getMessage());
	      }

	  }

	
}
