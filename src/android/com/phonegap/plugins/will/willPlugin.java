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
		    if (ACTION_ADD_WILL_ENTRY.equals(action)) { 
			    	Intent intent = new Intent(this,"com.wacom.toolsconfigurator.MAIN");
					view.getContext().startActivity(intent);
					
					
					/*callbackContext.error(this.cordova.getActivity().getApplicationContext().getPackageName());*/
					return false;
					
					/*Intent intentOpen = new Intent(WILL_INTENT);
					intentOpen.addCategory(Intent.CATEGORY_DEFAULT);
					intentOpen.setPackage(this.cordova.getActivity().getApplicationContext().getPackageName());
				   this.cordova.startActivityForResult((CordovaPlugin) this, intentOpen, 0);
				   return true;*/
			   
		    /*JSONObject arg_object = args.getJSONObject(0);
		    Intent calIntent = new Intent(Intent.ACTION_EDIT)
		        .setType("vnd.android.cursor.item/event")
		        .putExtra("beginTime", arg_object.getLong("startTimeMillis"))
		        .putExtra("endTime", arg_object.getLong("endTimeMillis"))
		        .putExtra("title", arg_object.getString("title"))
		        .putExtra("description", arg_object.getString("description"))
		        .putExtra("eventLocation", arg_object.getString("eventLocation"));
		 
		       this.cordova.getActivity().startActivity(calIntent);
		       callbackContext.success();*/
		        //Intent intentOpen = new Intent(WILL_INTENT);
		        //intentOpen.addCategory(Intent.CATEGORY_DEFAULT);
		        // avoid calling other phonegap apps
		        //intentOpen.setPackage(this.cordova.getActivity().getApplicationContext().getPackageName());

		        //this.cordova.startActivityForResult((CordovaPlugin) this, intentOpen, 0);
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

