package net.alexyorke.imagehandler;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;


public class ImageHandler extends CordovaPlugin {

    public static final String LOG_TAG = ImageHandler.class.getSimpleName();

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {

        if(action.equals("base64ToJpg")){
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    base64ToJpg(callbackContext, args);
                }
            });
        }else if(action.equals("resize")){
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    resize(callbackContext, args);
                }
            });
        }else if(action.equals("thumbnails")){
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    thumbnail(callbackContext, args);
                }
            });
        }else if(action.equals("rotate")){
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    rotate(callbackContext, args);
                }
            });
        }else{
            return false;
        }

        return true;

    }


    private void base64ToJpg(CallbackContext callbackContext, JSONArray args){

        try {
            for (int i = 0; i < args.length(); i++) {
                Log.d(LOG_TAG, args.getString(i));
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        callbackContext.success(); // Thread-safe.
    }

    private void resize(CallbackContext callbackContext, JSONArray args){
        callbackContext.success(); // Thread-safe.
    }

    private void thumbnail(CallbackContext callbackContext, JSONArray args){
        callbackContext.success(); // Thread-safe.
    }

    private void rotate(CallbackContext callbackContext, JSONArray args){
        callbackContext.success(); // Thread-safe.
    }

}
