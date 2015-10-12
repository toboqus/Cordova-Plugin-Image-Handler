(function(){

	"use strict";

	var ImageHandler = {};

	/**
	* @name isEmpty
	* @description will return true if the value
	* is of 0 length, null or an empty string.
	*/
	var isEmpty = function isEmpty(str) {
		return (str.length === 0 || str === null || str === "");
	};


	/**
	* @name isValid
	* @returns {boolean}
	* @description will loop through correct values and if value
	* is contained within, it will return true
	*/
	var isValid = function isValid(correctArray, value){

		for(var valueArray of correctArray){
			if(value === valueArray){
				return true;
			}
		}

		return false;;
	}


	/**
	* @name decorate
	* @param {object} template
	* @param {object} object
	* @return {object}
	*/
	var decorate = function decorate(template, object){
		var result = {};
		
		for(var key in template){
			if(object.hasOwnProperty(key)){
				result[key] = object[key];
			}else{
				result[key] = template[key];
			}
		}

		return result;
	}

	/**
	* @name base64ToJpg
	* @param {object} options
	* @param {function} cb
	* @description This function will save a base64 encoded string into a jpg image
	* to be stored in the directory with the filename provided. The callback will then be
	* fired with either a path to the newely created file or an error.
	*/
	imageHandler.base64ToJpg = function base64ToJpg(options, cb){

		//define expected parameters to decorate with
		var template = {
			base64string:null,
			destDirectory:null,
			destFilename:null
		}

		var result = decorate(template, options);

		if(isEmpty(result.base64string) 
			|| isEmpty(result.destDirectory) 
			|| isEmpty(result.destFilename)){

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
             [result.base64string, result.destDirectory, result.destFilename]);

	};


	/**
	* @name resize
	* @param {object} options
	* @param {function} cb
	* @description This function will resize an image to a height/width
	* of a maximum size, whilst maintaining the aspect ratio of the image.
	*/
	imageHandler.resize = function resize(options, cb) {

		var template = {
			fileLocation: null,
			maxSize: null
		}

		var result = decorate(template, options);

		if(isEmpty(results.fileLocation) || isEmpty(results.maxSize)){
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
             [results.fileLocation, results.maxSize]);

	};


	/**
	* @name thumbnail
	* @param {object} options
	* @param {function} cb
	* @description This function will generate a square jpg thumbnail in the directory specified 
	* with a specific height/width.
	*/
	imageHandler.thumbnail = function thumbnail(options, cb) {

		var template = {
			fileLocation: null,
			destDirectory: null,
			destFilename: null,
			thumbSize: null
		}

		var result = decorate(template, options);

		if(isEmpty(results.fileLocation) 
			|| isEmpty(results.destDirectory) 
			|| isEmpty(results.destFilename) 
			|| isEmpty(results.thumbSize)){

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
             [results.fileLocation, results.destDirectory
             	, results.destFilename, results.thumbSize]);

	};
	
	
	/**
	* @name rotate
	* @param {object} options
	* @param {function} cb
	* @description This function will rotate an image 
	*/
	imageHandler.rotate = function rotate(options, cb) {

		var template = {
			fileLocation: null,
			direction: null,
			degrees: null
		}
		, validDegrees = [90, 180, 270]
		, finalRotation = 0;


		//validation
		if(isEmpty(results.fileLocation) 
			|| isEmpty(results.direction)
			|| isEmpty(results.degrees)){
			cb("One or more parameters are undefined or empty.");
			return;
		}

		if(!isValid(validDegrees, results.degrees)){
			cb("Degrees must be valid");
			return;
		}
		
		switch(results.direction){
			case "ANTICLOCKWISE":{
				
				//reverse rotations for anticlockwise.
				if(results.degrees === 90){
					finalRotation = 270;
				}else if(results.degrees === 270){
					finalRotation = 90;
				}else{
					finalRotation = results.degrees;
				}

				break;
			} 
			case "CLOCKWISE":{
				//tis all good man... keep going.
				finalRotation = results.degrees;
				
				break;
			}
			default: {
				cb("direction of rotation is not recognised");
				return;
			}
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
             "rotate",
             [results.fileLocation, finalRotation]);

	};


	//expose the handler
	module.exports = imageHandler;
	
})()
