#Cordova Image Handler Plugin
A plugin  developed by [@romankisil](https://github.com/romankisil) and [@tabokus](https://github.com/tabokus) for the manipulation of images in Android and iOS.

##How to use

This plugin provides a number of methods that can be used to manipulate images with the cordova framework. Installation is a simple process of running:

`cordova plugin add https://github.com/tabokus/Cordova-Plugin-Image-Handler.git`

After the plugin has been installed succesfully, it is a simple case of calling the methods described below:

###base64ToJpg

This function will convert a base64 encoded image string into a jpg file within the given directory with the specified filename.

```javascript

var options = {
	   	base64string:"data:image/png;base64,iVBORw0KGg.....", //full string excluded
		destDirectory:cordova.file.externalRootDirectory, //cordova file plugin can be used
		destFilename:"new-image"// exclude the file extension
	}

var cb = function(error, value){
   if(error){
      //handle error - This is a friendly string explaining what went wrong
      return;
    }

   //returns filepath in the format 'file:///...' if successful
   var filePath = value;

}

window.ImageHandler.base64ToJpg(options, cb);
```

###resize

This function will resize a given image to a maximum width/heigh whilst maintaining its aspect ratio. If the destDirectory is omitted, it will save the image in the current directory. if the destFilename is omitted, it will save the image with the currentFilename. if both destFilename and destDirectory are omitted, it will replace the selected image.

```javascript
var options = {
		currentDirectory: cordova.file.externalRootDirectory,
		currentFilename: "originalImage",
		maxSize: 640, //pixels
		destDirectory: cordova.file.externalRootDirectory,
		destFilename: "resizedImage"
	};
	
var cb = function cb(error, value){
	if(error){
	  //handle error
	  return;
	}

       //returns filepath of resized image: file:///storage/emulated/0/resizedImage.jpg (android)
       var filePath = value;
    };

window.ImageHandler.resize(options, cb);
```

###thumbnail

This function will create a thumbnail from a given image (a square image with a width and height being defined in the options object). If the destDirectory is omitted, it will save the image in the current directory. if the destFilename is omitted, it will save the image with the currentFilename appended by "-thumb".

```javascript

var options = {
		currentDirectory: cordova.file.externalRootDirectory,
		currentFilename: "originalImage",
		destDirectory: cordova.file.externalRootDirectory,
		destFilename: "originalImage-thumb",
		thumbSize: 200 //200px X 200px
	};

var cb = function(error, value){
   if(error){
      //handle error
      return;
    }

   //returns filepath of resized image: file:///storage/emulated/0/originalImage-thumb.jpg (android)
   var filePath = value;

}

window.ImageHandler.thumbnail(options, cb);
```

###rotate

This function will rotate an image either 'ANTICLOCKWISE' or 'CLOCKWISE' by a degree of 90/180/270. if the destFilename and destDirectory is the same it will override the original image, else it will take on the value of the currentFilename/currentDirectory if undefined.

```javascript

var options = {
		currentDirectory: cordova.file.externalRootDirectory,
		currentFilename: "originalImage",
		destDirectory: cordova.file.externalRootDirectory,
		destFilename: "originalImage-rotated",
		direction: "ANTICLOCKWISE",
		degrees: 90
	};
	
var cb = function(error, value){
   if(error){
      //handle error
      return;
    }

   //returns filepath of resized image: file:///storage/emulated/0/originalImage-rotated.jpg (android)
   var filePath = value;

}

window.ImageHandler.rotate(options, cb);
```
##Contributing
Is there something missing, something broken or something just completely wrong? feel free to fork, fix and pull! 
