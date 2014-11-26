package com.phonegap.plugins.will;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import com.wacom.toolsconfigurator.MainActivity;

public class willPlugin  extends CordovaPlugin 	{
public static final int REQUEST_CODE = 0x0ba7c0df;
	public static final String ACTION_ADD_WILL_ENTRY = "addWillEntry"; 
	private static final String WILL_INTENT = "com.wacom.toolsconfigurator.MAIN";
	private static final String FILE = "file";
	public boolean loaded=false;
	private static final String CANCELLED = "cancelled";
	private CallbackContext callbackContext;
	private CordovaPlugin obj;
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
	obj = (CordovaPlugin) this;
	this.callbackContext = callbackContext;
		try {
		    if (ACTION_ADD_WILL_ENTRY.equals(action)) { 
			if (!loaded) {
				cordova.getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Context context = cordova.getActivity().getApplicationContext();
						Intent intent = new Intent(context,MainActivity.class);
						cordova.startActivityForResult(obj, intent, REQUEST_CODE);
						//cordova.getActivity().startActivity(intent);
					}

            });
		    	loaded=true;
			}

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
	
	    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put(FILE, intent.getStringExtra("FILE_RESULT"));
                    obj.put(CANCELLED, false);
                } catch (JSONException e) {
                    //Log.d(LOG_TAG, "This should never happen");
                }
                //this.success(new PluginResult(PluginResult.Status.OK, obj), this.callback);
                callbackContext.success(obj);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put(FILE, "");
                    obj.put(CANCELLED, true);
                } catch (JSONException e) {
                    //Log.d(LOG_TAG, "This should never happen");
                }
                //this.success(new PluginResult(PluginResult.Status.OK, obj), this.callback);
                callbackContext.success(obj);
            } else {
                //this.error(new PluginResult(PluginResult.Status.ERROR), this.callback);
                callbackContext.error("Unexpected error");
            }
        }
    }


}





