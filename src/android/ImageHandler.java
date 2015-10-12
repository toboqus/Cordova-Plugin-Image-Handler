package net.alexyorke.imagehandler;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


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


    /**
     *
     */
    private void base64ToJpg(CallbackContext callbackContext, JSONArray args){

        String base64Image;
        String directory;
        String imageName;
        String mFilePath;

        try {
            base64Image = args.getString(0);
            directory = args.getString(1);
            imageName = args.getString(2);
        }catch(JSONException e){
            e.printStackTrace();
            callbackContext.error("Could not parse the parameters");
            return;
        }

        //does not contain predata - reject
        if(base64Image.length() < 22){
            callbackContext.error("base64 string is not formatted correctly");
            return;
        }

        try{
            String imageDataBytes = base64Image.substring(base64Image.indexOf(",") + 1);
            InputStream is = new ByteArrayInputStream(Base64.decode(imageDataBytes, Base64.DEFAULT));
            Bitmap image= BitmapFactory.decodeStream(is);

            if (!new File(directory).exists()) {
                callbackContext.error("Directory: "+directory+" does not exist!");
                return;
            }

            mFilePath = directory + "/" + imageName + ".jpg";
            File file = new File(mFilePath);
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            is.close();
            image.recycle();
            stream.flush();
            stream.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            callbackContext.error("Could not save image");
            return;
        }

        callbackContext.success(mFilePath); // Thread-safe.
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
