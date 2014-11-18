package com.phonegap.plugins.will;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.content.Intent;


public class willPlugin  extends CordovaPlugin 	{
public static final int REQUEST_CODE = 0x0ba7c0df;
	public static final String ACTION_ADD_WILL_ENTRY = "addWillEntry"; 
	private static final String WILL_INTENT = "com.wacom.toolsconfigurator.MAIN";
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		try {
		    if (ACTION_ADD_WILL_ENTRY.equals(action)) { 
			
			cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    			    Intent intentScan = new Intent(WILL_INTENT);
				intentScan.addCategory(Intent.CATEGORY_DEFAULT);
				// avoid calling other phonegap apps
				intentScan.setPackage(this.cordova.getActivity().getApplicationContext().getPackageName());

				this.cordova.startActivityForResult((CordovaPlugin) this, intentScan, REQUEST_CODE);
                    //callbackContext.success(); // Thread-safe.
                }
            });

				return true;
		    }
		    callbackContext.error("Invalid action");
		    return false;
		} catch(Exception e) {
		    System.err.println("Exception: " + e.getMessage());
		    callbackContext.error(e.getMessage());
		    return false;
		} 

	}

}

