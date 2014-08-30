package com.parse.starter;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;
import com.parse.PushService;
// import com.parse.ParseAnalytics;

/**
 * Base class for those who need to maintain global application state. 
 * This is name in the AndroidManifest.xml's <application> tag, 
 * which will cause that class to be instantiated for you when 
 * the process for your application/package is created.
 * 
 * @author timothy
 *
 */
public class ParseApplication extends Application {

	private static final String DEBUG_TAG = "ParseApplication";
	// parse.com keys
	private static String APPLICATION_ID = "Sn81TbbAZ1KgHHlzTf7bCG31MEYPOQgYksDFTv9A";
	private static String CLIENT_KEY = "Psp0lZgiRQgp9TooMtmue7Rb2zQGCqysLDKu5Orx";
	
	final Context context = this;
	
	@Override
	public void onCreate() {
		super.onCreate();
		// initialization code here
		Parse.enableLocalDatastore(context); // enables the pinInBackground() method.
		Parse.initialize(this, APPLICATION_ID, CLIENT_KEY);
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		//specify a Activity to be used for all push notifications by default
		PushService.setDefaultPushCallback(this, ParseStarterProjectActivity.class);
		// To make all objects private by default, remove the next line.
		defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);
		Log.i(DEBUG_TAG, "finished setup");
	}

}
