#Cordova Image Handler Plugin
A plugin for the manipulation of images in Android and iOS

##How to use

This plugin provides a number of methods that can be used to manipulate images with the cordova framework

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
