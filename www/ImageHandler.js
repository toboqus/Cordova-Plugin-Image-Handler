(function(global){

	"use strict";

	/**
	* @name isEmpty
	* @description will return true if the value
	* is of 0 length, null or an empty string.
	*/
	var isEmpty = function isEmpty(str) {
		return (str.length === 0 || str === null || str === "");
	};

	/**
	* @name base64ToJpg
	* @param {String} base64string
	* @param {String} destDirectory
	* @param {String} destFilename
	* @param {function} cb
	* @description This function will save a base64 encoded string into a jpg image
	* to be stored in the directory with the filename provided. The callback will then be
	* fired with either a path to the newely created file or an error.
	*/
	var base64ToJpg = function base64ToJpg(base64string, destDirectory, destFilename, cb){

		if(isEmpty(base64string) || isEmpty(destDirectory) || isEmpty(destFilename)){
			cb("One or more parameters are undefined or empty.");
			return;
		}


		//do the magic here
		 cordova.exec(
		 	 function(winParam) {
	 			cb(null, winParam);
	 		 },
             function(error) {
             	cb(error);
             },
             "ImageHandler",
             "base64ToJpg",
             [base64string, destDirectory, destFilename]);

	};


	/**
	* @name resize
	* @param {string} fileLocation
	* @param {integer} maxsize
	* @param {function} cb
	* @description This function will resize an image to a height/width
	* of a maximum size, whilst maintaining the aspect ratio of the image.
	*/
	var resize = function resize(fileLocation, maxsize, cb) {

		if(isEmpty(fileLocation) || isEmpty(maxsize)){
			cb("One or more parameters are undefined or empty.");
			return;
		}


		//do magic here
		cordova.exec(
		 	 function(winParam) {
	 	 		cb(null, winParam);
	 		 },
             function(error) {
             	cb(error);
             },
             "ImageHandler",
             "resize",
             [fileLocation, maxsize]);

	};


	/**
	* @name thumbnail
	* @param {String} fileLocation
	* @param {String} destDirectory
	* @param {String} destFilename
	* @param {integer} size
	* @param {function} cb
	* @description This function will generate a square jpg thumbnail in the directory specified 
	* with a specific height/width.
	*/
	var thumbnail = function thumbnail(fileLocation, destDirectory, destFilename, size, cb) {

		if(isEmpty(fileLocation) || isEmpty(destDirectory) || isEmpty(destFilename) || isEmpty(size)){
			cb("One or more parameters are undefined or empty.");
			return;
		}


		//do magic here
		cordova.exec(
		 	 function(winParam) {
	 	 		cb(null, winParam);
	 		 },
             function(error) {
             	cb(error);
             },
             "ImageHandler",
             "thumbnail",
             [fileLocation, destDirectory, destFilename, size]);

	};


	global.ImageHandler = {
		base64ToJpg:base64ToJpg,
		resize:resize,
		thumbnail:thumbnail
	}
	
	
})(this)