package net.alexyorke.imagehandler;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;


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
     * This method will attempt to save a base64 string to a jpg file with the
     * given directory and imageName
     * @param callbackContext callback to respond with
     * @param args arguments in this case are: base64Image, directory, imageName
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
            if(directory == null || imageName == null){
                callbackContext.error("Could not parse the parameters");
                return;
            }
            mFilePath = directory + imageName + ".jpg";

        }catch(JSONException e){
            e.printStackTrace();
            callbackContext.error("Could not parse the parameters");
            return;
        }

        //does not contain predata - reject
        if(!isValidImage(base64Image) || !isValidExtension(getBase64Extension(base64Image))){
            callbackContext.error("base64 string is not a valid image");
            return;
        }

        try{
            String imageDataBytes = base64Image.substring(base64Image.indexOf(",") + 1);
            Bitmap finalImage = whiteBackground(Base64ToBitmap(imageDataBytes));

            File directoryLoc = new File(URI.create(directory));
            File file = new File(URI.create(mFilePath));

            //ensure that directory structure exists
            if (!directoryLoc.exists()) {
                directoryLoc.mkdirs();
            }
            //create file if it doesnt exist
            if (!file.exists()){
                file.createNewFile();
            }

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                finalImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                finalImage.recycle();
            }

        } catch(Exception e) {
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


    //-------------------HELPER METHODS---------------------------

    /**
     *
     * @param base64Image base64 encoded string of the image
     * @return true if the image is valid, false if not
     */
    private boolean isValidImage(String base64Image){

        if(base64Image == null){
            return false;
        }

        String crop1 = base64Image.substring(base64Image.indexOf(":") + 1);
        String type = crop1.substring(0,crop1.indexOf("/"));

        return (type.equals("image"));

    }

    /**
     *
     * @param extension extension of the image
     * @return true if the extension is supported, or false if not
     */
    private boolean isValidExtension(String extension){

        String[] validExtensions = {"jpg", "jpeg", "png"};

        for(String ext : validExtensions){
            if(ext.equals(extension)){
                return true;
            }
        }

        return false;
    }

    /**
     * will return the extension of the image
     * @param base64Image the base64 encoded image
     * @return the extension of the image
     */
    private String getBase64Extension(String base64Image){

        String imageDataBytes = base64Image.substring(base64Image.indexOf("/") + 1);
        String ext = imageDataBytes.substring(0,imageDataBytes.indexOf(";"));

        return (isValidExtension(ext)) ? ext : null;
    }

    /**
     * will attempt to get a Bitmap from a base64 string
     * @param myImageData base64 string to turn into a bitmap image
     * @return Bitmap
     */
    private Bitmap Base64ToBitmap(String myImageData){
        byte[] imageAsBytes = Base64.decode(myImageData.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }


    /**
     * This method will overlay two bitmap images on top
     * of each other.
     *
     * @param bmp1 background image
     * @param bmp2 layer image
     * @return Bitmap
     */
    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        if(bmp1 == null || bmp2 == null){
            return null;
        }

        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    /**
     * This method will ensure that transparency is handled
     * when saving pngs to a jpg
     *
     * @param bmp1 image that needs to have a white background
     * @return Bitmap
     */
    private Bitmap whiteBackground(Bitmap bmp1){
        if(bmp1 == null){
            return null;
        }

        Bitmap background = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        background.eraseColor(Color.WHITE);
        Bitmap finalImage = overlay(background, bmp1);

        background.recycle();
        bmp1.recycle();

        return finalImage;
    }

}
