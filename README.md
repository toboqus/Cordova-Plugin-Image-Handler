#Cordova Image Handler Plugin
A plugin for the manipulation of images in Android and iOS developed by [@yorke543](https://github.com/yorke543) and [@bitgapp](https://github.com/bitgapp)

##How to use

This plugin provides a number of methods that can be used to manipulate images with the cordova framework. Installation is a simple process of running:

`cordova plugin add https://github.com/yorke543/Cordova-Image-Handler.git`

After the plugin has been installed succesfully, it is a simple case of calling the methods described below:

###base64ToJpg

```javascript
var cb = function(error, value){
   if(error){
      //handle error
      return;
    }

   //returns filepath
   var filePath = value;

}

window.ImageHandler.base64ToJpg(base64string, destDirectory, destFilename, cb);
```

###resize

```javascript
var maxSize = 640,
    cb = function(error, value){
       if(error){
          //handle error
          return;
        }

       //returns filepath
       var filePath = value;
    };

window.ImageHandler.resize(fileLocation, maxsize, cb);
```


###thumbnail

```javascript
var cb = function(error, value){
   if(error){
      //handle error
      return;
    }

   //returns filepath
   var filePath = value;

}

window.ImageHandler.thumbnail(fileLocation, destDirectory, destFilename, size, cb);
```

###rotate

```javascript
var cb = function(error, value){
   if(error){
      //handle error
      return;
    }

   //returns filepath
   var filePath = value;

}

window.ImageHandler.rotate(fileLocation, cb);
```
