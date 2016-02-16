package net.alexyorke.imagehandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        }else if(action.equals("timestamp")){
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    timestamp(callbackContext, args);
                }
            });
        }else{
            return false;
        }

        return true;

    }
    
    /**
     * This method will add a timestamp to the photo.
     * @param callbackContext callback to respond with
     * @param args arguments
     */
    private void timestamp(CallbackContext callbackContext, JSONArray args){
		    	String currentDirectory
		        , currentFilename
		        , currentImagePath
		        , destDirectory
		        , destFilename
		        , destImagePath;
		
		try{
		    currentDirectory = args.getString(0);
		    currentFilename = args.getString(1);
		    destDirectory = args.getString(2);
		    destFilename = args.getString(3);
		
		}catch(JSONException e){
		    e.printStackTrace();
		    callbackContext.error("Could not parse the parameters");
		    return;
		}
		
		if(currentDirectory.equals("null")
		        || currentFilename.equals("null")){
		    callbackContext.error("Could not parse the parameters");
		    return;
		}
		
		currentDirectory = formatDirectory(currentDirectory);
		destDirectory =
		        formatDirectory((destDirectory.equals("null")) ? currentDirectory : destDirectory);
		destFilename =  (destFilename.equals("null")) ? currentFilename : destFilename;
		
		try {
		    destImagePath = constructImagePath(destDirectory, destFilename);
		    currentImagePath = constructImagePath(currentDirectory, currentFilename);
		    File currImage = new File(URI.create(currentImagePath));
		
		    //check if the image exists
		    if (!currImage.exists()) {
		        callbackContext.error("Image does not exist!");
		        return;
		    }
		
		    //get the image
		    Bitmap currentImage = BitmapFactory.decodeFile(currImage.getPath());
		    if (currentImage == null) {
		        callbackContext.error("Could not load image to be resized");
		        return;
		    }
		
		    //resize the image
		    Bitmap timestampedImage = addTimeStamp(currentImage);
		    if (timestampedImage == null) {
		        callbackContext.error("Could not timestamp image");
		        return;
		    }
		
		    //get the file to save image into
		    File destImage = new File(URI.create(destImagePath));
		    if(!ConstructFileStructure(destImage)){
		        callbackContext.error("Could not create file structure");
		        return;
		    }
		
		    //save the image
		    if (!saveImage(resizedImage, destImage, imageQuality)) {
		        callbackContext.error("Could not save image");
		        return;
		    }
		
		    callbackContext.success(destImagePath); // Thread-safe.
		
		}catch(Exception e){
		    e.printStackTrace();
		    callbackContext.error("Could not save image");
		}
    }


    /**
     * This method will attempt to save a base64 string to a jpg file with the
     * given directory and imageName
     * @param callbackContext callback to respond with
     * @param args arguments in this case are: base64Image, directory, imageName
     */
    private void base64ToJpg(CallbackContext callbackContext, JSONArray args){

        int imageQuality = 100;
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

        if(directory.equals("null") || imageName.equals("null")){
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
                callbackContext.error("Could parse image");
                return;
            }

            File file = new File(URI.create(mFilePath));
            if(!ConstructFileStructure(file)){
                callbackContext.error("Could not create file structure");
                return;
            }

            if(!saveImage(finalImage, file, imageQuality)){
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

    /**
     * This method will resize an image to a maximum width/height whilst
     * maintaining the aspect ratio
     * @param callbackContext callback
     * @param args JSON arguments
     */
    private void resize(CallbackContext callbackContext, JSONArray args){
        String currentDirectory
                , currentFilename
                , currentImagePath
                , destDirectory
                , destFilename
                , destImagePath;
        int maxSize,
                imageQuality = 100;

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

        if(currentDirectory.equals("null")
                || currentFilename.equals("null")
                || maxSize <= 0){
            callbackContext.error("Could not parse the parameters");
            return;
        }

        currentDirectory = formatDirectory(currentDirectory);
        destDirectory =
                formatDirectory((destDirectory.equals("null")) ? currentDirectory : destDirectory);
        destFilename =  (destFilename.equals("null")) ? currentFilename : destFilename;

        try {
            destImagePath = constructImagePath(destDirectory, destFilename);
            currentImagePath = constructImagePath(currentDirectory, currentFilename);
            File currImage = new File(URI.create(currentImagePath));

            //check if the image exists
            if (!currImage.exists()) {
                callbackContext.error("Image does not exist!");
                return;
            }

            //get the image
            Bitmap currentImage = BitmapFactory.decodeFile(currImage.getPath());
            if (currentImage == null) {
                callbackContext.error("Could not load image to be resized");
                return;
            }

            if(currentImage.getWidth() < maxSize && currentImage.getHeight() < maxSize){
                callbackContext.error("Image is smaller than the specified size");
                return;
            }

            //resize the image
            Bitmap resizedImage = getScaledBitmap(currentImage, maxSize);
            if (resizedImage == null) {
                callbackContext.error("Could not resize image");
                return;
            }

            //get the file to save image into
            File destImage = new File(URI.create(destImagePath));
            if(!ConstructFileStructure(destImage)){
                callbackContext.error("Could not create file structure");
                return;
            }

            //save the image
            if (!saveImage(resizedImage, destImage, imageQuality)) {
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
        String currentDirectory
                , currentFilename
                , currentImagePath
                , destDirectory
                , destFilename
                , destImagePath;
        int thumbSize
                , imageQuality = 90;

        try{
            currentDirectory = args.getString(0);
            currentFilename = args.getString(1);
            thumbSize = args.getInt(2);
            destDirectory = args.getString(3);
            destFilename = args.getString(4);

        }catch(JSONException e){
            e.printStackTrace();
            callbackContext.error("Could not parse the parameters");
            return;
        }

        if(currentDirectory.equals("null")
                || currentFilename.equals("null")
                || thumbSize <= 0){
            callbackContext.error("Could not parse the parameters");
            return;
        }

        //format paths and append THUMBNAIL if not specified
        currentDirectory = formatDirectory(currentDirectory);
        destDirectory =
                formatDirectory((destDirectory.equals("null")) ? currentDirectory : destDirectory);
        destFilename =  (destFilename.equals("null")) ? currentFilename+THUMBNAIL : destFilename;

        try {
            destImagePath = constructImagePath(destDirectory, destFilename);
            currentImagePath = constructImagePath(currentDirectory, currentFilename);
            File currImage = new File(URI.create(currentImagePath));

            //check if the image exists
            if (!currImage.exists()) {
                callbackContext.error("Image does not exist!");
                return;
            }

            //load the image
            Bitmap currentImage = BitmapFactory.decodeFile(currImage.getPath());
            if (currentImage == null) {
                callbackContext.error("Could not load the image");
                return;
            }

            //get thumbnail image
            Bitmap thumbnailImage = getThumbnailBitmap(currentImage, thumbSize);
            if (thumbnailImage == null) {
                callbackContext.error("Could not create thumbnail");
                return;
            }

            //get file to save image to
            File destImage = new File(URI.create(destImagePath));
            if(!ConstructFileStructure(destImage)){
                callbackContext.error("Could not create file structure");
                return;
            }

            //save the image
            if (!saveImage(thumbnailImage, destImage, imageQuality)) {
                callbackContext.error("Could not save image");
                return;
            }

            callbackContext.success(destImagePath); // Thread-safe.

        }catch(Exception e){
            e.printStackTrace();
            callbackContext.error("Could not save image");
        }
    }

    /**
     * This method will rotate a specified image clockwise
     * If the destFilename does not exist, it will use the filename of the image
     * if the destDirectory does not exist, it will use the current directory and replace
     * the existing image if the filename is the same.
     * @param callbackContext callback
     * @param args arguments in JSON
     */
    private void rotate(CallbackContext callbackContext, JSONArray args){
        String currentDirectory
                , currentFilename
                , currentImagePath
                , destDirectory
                , destFilename
                , destImagePath;
        int finalRotation
                , imageQuality = 100;

        try{
            currentDirectory = args.getString(0);
            currentFilename = args.getString(1);
            destDirectory = args.getString(2);
            destFilename = args.getString(3);
            finalRotation = args.getInt(4);

        }catch(JSONException e){
            e.printStackTrace();
            callbackContext.error("Could not parse the parameters");
            return;
        }

        //checking for nulls as strings as null is parsed as a string
        if(currentDirectory.equals("null")
                || currentFilename.equals("null")
                || (finalRotation != 90 && finalRotation != 180 && finalRotation != 270)){
            callbackContext.error("Could not parse the parameters");
            return;
        }

        //format paths
        currentDirectory = formatDirectory(currentDirectory);
        destDirectory =
                formatDirectory((destDirectory.equals("null")) ? currentDirectory : destDirectory);
        destFilename =  (destFilename.equals("null")) ? currentFilename : destFilename;

        try {
            destImagePath = constructImagePath(destDirectory, destFilename);
            currentImagePath = constructImagePath(currentDirectory, currentFilename);
            File currImage = new File(URI.create(currentImagePath));

            //check if the image exists
            if (!currImage.exists()) {
                callbackContext.error("Image does not exist!");
                return;
            }

            //get the current image
            Bitmap currentImage = BitmapFactory.decodeFile(currImage.getPath());
            if (currentImage == null) {
                callbackContext.error("Could not load the image");
                return;
            }

            //rotate the image
            Bitmap rotatedImage = rotateBitmap(currentImage, finalRotation);
            if (rotatedImage == null) {
                callbackContext.error("Could not rotate image");
                return;
            }

            //create file to save into
            File destImage = new File(URI.create(destImagePath));
            if(!ConstructFileStructure(destImage)){
                callbackContext.error("Could not create file structure");
                return;
            }

            //save the image
            if (!saveImage(rotatedImage, destImage, imageQuality)) {
                callbackContext.error("Could not save image");
                return;
            }

            callbackContext.success(destImagePath); // Thread-safe.

        }catch(Exception e){
            e.printStackTrace();
            callbackContext.error("Could not save image");
        }

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
     * This method will create a thumbnail of a given image as a square
     * size X size.
     * @param image image to generate thumbnail from
     * @param size the determined size of the thumbnail
     * @return bitmap of the thumbnail.
     */
    private Bitmap getThumbnailBitmap(Bitmap image, int size){

        if(image == null
                || size <= 0
                || image.getWidth() < size
                || image.getHeight() < size){
            return null; //handle bad data
        }

        int resizedHeight, resizedWidth, x, y;
        double factor = 1.0d;

        if(image.getWidth() > image.getHeight()){
            factor = ((double)image.getWidth()/(double)image.getHeight());

            resizedHeight = size;
            resizedWidth = (int)(resizedHeight * factor);
            x = (resizedWidth-size)/2; //get offset
            y = 0;
        }else{
            factor = ((double)image.getHeight()/(double)image.getWidth());

            resizedWidth = size;
            resizedHeight = (int)(resizedWidth * factor);
            y = (resizedHeight-size)/2; //get offset
            x = 0;
        }

        Bitmap resizedImg = Bitmap.createScaledBitmap(image, resizedWidth, resizedHeight, true);

        Bitmap res = Bitmap.createBitmap(resizedImg, x, y, size, size);
        resizedImg.recycle();
        return res;
    }




    /**
     * This method will save a bitmap image as a jpg in the specified
     * file. The file and directory structure must be set up before saving
     * @param image bitmap image to save
     * @param file file to save the image into
     * @return true if the image was able to save, false if not.
     */
    private boolean saveImage(Bitmap image, File file, int quality){

        if(image == null || file == null || quality < 0 || quality > 100){
            return false;
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, quality, out);
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


    /**
     *
     * @param file image file
     * @return true if the directory structure now exists, false if not
     */
    private boolean ConstructFileStructure(File file){
        if(file == null){
            return false;
        }

        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        try{
            if(!file.exists()){
                file.createNewFile();
            }
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * will rotate a bitmap image by a given angle
     * @param source image to rotate
     * @param angle degrees in which to rotate
     * @return new bitmap of the rotated image
     */
    private static Bitmap rotateBitmap(Bitmap source, int angle){
        if(source == null){
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate((float)angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    
    /**
     *  will timestamp the image with the current time
     *  and date
     *  @param source image to timestamp
     */
    private Bitmap addTimeStamp(Bitmap source){
    	
    	if(source == null){
    		return null;
    	}
    	
    	
    	Bitmap bitmap = source.copy(source.getConfig(), true);
    	Canvas canvas = new Canvas(bitmap);
    	
    	int fontSize = source.getWidth()/20;
    	int strokeSize = fontSize/10;
    	
    	int x = 10;
    	int y = 10 + fontSize;
    	
    	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	paint.setColor(Color.WHITE);
    	paint.setTextSize(fontSize);
    	paint.setShadowLayer(strokeSize, 0, 0, Color.BLACK);
    
    	canvas.drawText(getTimeStamp(), x, y, paint);
    	
    	return bitmap;
    	
    }
    
    
    private String getTimeStamp(){
    	
    	 SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
         String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

         return currentTimeStamp;
    }
    


}
