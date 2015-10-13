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

    public static final String JPG_EXT = ".jpg";
    public static final String THUMBNAIL = "-thumb";


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
        }else if(action.equals("thumbnail")){
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

        }catch(JSONException e){
            e.printStackTrace();
            callbackContext.error("Could not parse the parameters");
            return;
        }

        if(directory == null || imageName == null){
            callbackContext.error("Could not parse the parameters");
            return;
        }

        //add / end of directory
        directory = formatDirectory(directory);
        mFilePath = constructImagePath(directory, imageName);

        //does not contain predata - reject
        if(!isValidImage(base64Image) || !isValidExtension(getBase64Extension(base64Image))){
            callbackContext.error("base64 string is not a valid image");
            return;
        }

        try{
            String imageDataBytes = base64Image.substring(base64Image.indexOf(",") + 1);
            Bitmap finalImage = whiteBackground(Base64ToBitmap(imageDataBytes));

            if(finalImage == null){
                callbackContext.error("Could not save image");
                return;
            }

            File directoryLoc = new File(URI.create(directory));
            File file = new File(URI.create(mFilePath));

            if (!directoryLoc.exists()) {
                directoryLoc.mkdirs(); //ensure that directory structure exists
            }

            if (!file.exists()){
                file.createNewFile();//create file if it doesnt exist
            }

            if(!saveImage(finalImage, file)){
                callbackContext.error("Could not save image");
                return;
            }

        } catch(Exception e) {
            e.printStackTrace();
            callbackContext.error("Could not save image");
            return;
        }

        callbackContext.success(mFilePath); // Thread-safe.
    }

    private void resize(CallbackContext callbackContext, JSONArray args){
        String currentDirectory
                , currentFilename
                , currImagePath
                , destDirectory
                , destFilename
                , destImagePath;
        int maxSize;

        try{
            currentDirectory = args.getString(0);
            currentFilename = args.getString(1);
            maxSize = args.getInt(2);
            destDirectory = args.getString(3);
            destFilename = args.getString(4);

        }catch(JSONException e){
            e.printStackTrace();
            callbackContext.error("Could not parse the parameters");
            return;
        }

        if(currentDirectory == null
                || currentFilename == null
                || maxSize <= 0){
            callbackContext.error("Could not parse the parameters");
            return;
        }

        currentDirectory = formatDirectory(currentDirectory);
        destDirectory = formatDirectory((destDirectory == null) ? currentDirectory : destDirectory);
        destFilename = (destFilename == null) ? currentFilename : destFilename;

        try {
            destImagePath = constructImagePath(destDirectory, destFilename);
            currImagePath = constructImagePath(currentDirectory, currentFilename);
            File currImage = new File(URI.create(currImagePath));

            //check if the image exists
            if (!currImage.exists()) {
                callbackContext.error("Image does not exist!");
                return;
            }

            Bitmap currentImage = BitmapFactory.decodeFile(currImage.getPath());
            if (currentImage == null) {
                callbackContext.error("Could not load image to be resized");
                return;
            }

            Bitmap resizedImage = getScaledBitmap(currentImage, maxSize);
            if (resizedImage == null) {
                callbackContext.error("Could not resize image");
                return;
            }

            File destImage = new File(URI.create(destImagePath));
            File destDirectoryLoc = new File(URI.create(destDirectory));

            if (!destDirectoryLoc.exists()) {
                destDirectoryLoc.mkdirs(); //ensure that directory structure exists
            }

            if (!destImage.exists()) {
                destImage.createNewFile();//create file if it doesnt exist
            }

            if (!saveImage(resizedImage, destImage)) {
                callbackContext.error("Could not save image");
                return;
            }

            callbackContext.success(destImagePath); // Thread-safe.

        }catch(Exception e){
            e.printStackTrace();
            callbackContext.error("Could not save image");
        }
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


    /**
     *
     * @param directory path of the directory
     * @param imageName name of the image without the extension
     * @return the filepath to the image
     */
    private String constructImagePath(String directory, String imageName){

        if(directory == null || imageName == null){
            return null;
        }

        return directory + imageName + JPG_EXT;
    }


    /**
     *
     * @param directory path of the directory
     * @return directory after formatting has taken place
     */
    private String formatDirectory(String directory){
        if(directory == null){
            return null;
        }

        return (directory.endsWith("/")) ? directory : directory+"/";
    }


    /**
     * This method will return a scaled bitmap. It will return null
     * if the supplied image is null, max size is less than or equal to zero
     * or the image is already too small.
     * @param image bitmap image to resize
     * @param maxSize the maximum size in either direction
     * @return resized bitmap
     */
    private Bitmap getScaledBitmap(Bitmap image, int maxSize){
        if(image == null
                || maxSize <= 0
                || (image.getHeight() < maxSize && image.getWidth() < maxSize)){
            return null;
        }

        int finalw, finalh;
        double factor = 1.0d;

        if(image.getWidth() > image.getHeight()){
            finalw = maxSize;
            factor = ((double)image.getHeight()/(double)image.getWidth());
            finalh = (int)(finalw * factor);
        }else{
            finalh = maxSize;
            factor = ((double)image.getWidth()/(double)image.getHeight());
            finalw = (int)(finalh * factor);
        }

        return Bitmap.createScaledBitmap(image, finalw, finalh, true);
    }


    /**
     * This method will save a bitmap image as a jpg in the specified
     * file. The file and directory structure must be set up before saving
     * @param image bitmap image to save
     * @param file file to save the image into
     * @return true if the image was able to save, false if not.
     */
    private boolean saveImage(Bitmap image, File file){

        if(image == null || file == null){
            return false;
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            image.recycle();
        }

        return true;
    }


}
