package com.phonegap.plugins.will;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.content.Intent;


public class willPlugin  extends CordovaPlugin 	{
	public static final String ACTION_ADD_WILL_ENTRY = "addWillEntry"; 
	private static final String WILL_INTENT = "android.intent.action.MAIN";
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		try {

		    callbackContext.error("test");
		    return false;
		} catch(Exception e) {
		    System.err.println("Exception: " + e.getMessage());
		    callbackContext.error("test" + e.getMessage());
		    return false;
		} 

	}

}

