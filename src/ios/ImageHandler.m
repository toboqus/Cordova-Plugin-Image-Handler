//Created by Roman Kisil romankisilo@gmail.com on 14/10/2015

#import "ImageHandler.h"

@implementation ImageHandler

#pragma mark - Main methods
/**
 This method converts any base64 Image string into a JPG file
 and saves it in the specified directory
 
 - Required parameters:
 @param imageBase64
        (String) Base64 image data string. Will take PNG or JPG images.
 @param destDirectory
        (String) Destination directory in which the new JPG image will be saved.
 @param destFilename
        (String) JPG filename w/o the .jpg extension.
 
 */
- (void)base64ToJpg:(CDVInvokedUrlCommand*)command{

    [self.commandDelegate runInBackground:^{


        //Check if any parameters are missing
        if([[command arguments] objectAtIndex:0] == [NSNull null] || [[command arguments] objectAtIndex:1] == [NSNull null] || [[command arguments] objectAtIndex:2] == [NSNull null]){
            [self callback:CDVCommandStatus_ERROR withMessage:@"Parameters missing" toCallbackId:command.callbackId];
        }

        NSString* imageBase64 = [[command arguments] objectAtIndex:0];
        NSString* destDirectory = [[command arguments] objectAtIndex:1];
        NSString* destFilename = [[command arguments] objectAtIndex:2];
        NSString* doTimeStamp = [[command arguments] objectAtIndex:3];


        //Check if parameters are invalid
        if([imageBase64 length] == 0 || [destDirectory length] == 0 || [destFilename length] == 0){
            return;
        }

        destDirectory = [self correctPath:destDirectory];

        //Convert base64 to JPG image
        NSURL *url = [NSURL URLWithString:imageBase64];
        NSData *imageData = [NSData dataWithContentsOfURL:url];
        UIImage *image = [UIImage imageWithData:imageData];
        
        //Timestamp the image if needed
        if([doTimeStamp isEqualToString:@"YES"]){
            NSDateFormatter *dateFormatter=[[NSDateFormatter alloc] init];
            [dateFormatter setDateFormat:@"dd/MM/yyyy hh:mm"];
            image = [self drawText:[dateFormatter stringFromDate:[NSDate date]] inImage:image atPoint:CGPointMake(10, 10)];
        }
        
        NSData *imageJPGData = UIImageJPEGRepresentation(image, 1.0);

        
        //Check if destination directory exists, if not create the directory
        destDirectory = [destDirectory substringFromIndex:7]; //Subtracting "file://" url unrecognized by Obj-C
        BOOL isDirectory;
        if([[NSFileManager defaultManager] fileExistsAtPath:destDirectory isDirectory:&isDirectory] == false){
            NSError * error = nil;
            [[NSFileManager defaultManager] createDirectoryAtPath:destDirectory
                                      withIntermediateDirectories:YES
                                                       attributes:nil
                                                            error:&error];
            if (error != nil) {
                NSLog(@"error creating directory: %@", error);
                [self callback:CDVCommandStatus_ERROR withMessage:@"Error creating directory" toCallbackId:command.callbackId];
            }
        }
        
        //Construct the absolute image path and save the image
        NSString *imagePath =[NSString stringWithFormat:@"%@%@.jpg", destDirectory, destFilename];
        if (![imageJPGData writeToFile:imagePath atomically:NO]){
            [self callback:CDVCommandStatus_IO_EXCEPTION withMessage:@"Failed to save the file" toCallbackId:command.callbackId];
        }
        else{
            [self callback:CDVCommandStatus_OK withMessage:[NSString stringWithFormat:@"file://%@", imagePath] toCallbackId:command.callbackId];
        }
    }];

}

/**
 This method resizes an image in a specified directory 
 and saves it in any other directory with a specified name.
 If the final directory or name are not specified it will 
 overwrite the original image.
 ONLY DEALS WITH JPGs!
 
 - Required parameters:
 @param currentDirectory
        (String) Directory path for the original image
 @param currentFilename
        (String) Original image filename w/o the .jpg extension
 @param maxSize
        (Integer) Size of the images longest side after resizing
 
 
 - Optional parameters:
 @param destDirectory
        (String) Destination directory in which the new JPG image will be saved.
 @param destFilename
        (String) JPG filename w/o the .jpg extension.
 */
- (void)resize:(CDVInvokedUrlCommand*)command{
    
    [self.commandDelegate runInBackground:^{
        //Check if any parameters are missing
        if([[command arguments] objectAtIndex:0] == [NSNull null] || [[command arguments] objectAtIndex:1] == [NSNull null] || [[command arguments] objectAtIndex:2] == [NSNull null]){
            [self callback:CDVCommandStatus_ERROR withMessage:@"Parameters missing" toCallbackId:command.callbackId];
        }
        NSString* currentDirectory = [[command arguments] objectAtIndex:0];
        NSString* currentFilename = [[command arguments] objectAtIndex:1];
        NSString* maxSizeString = [[command arguments] objectAtIndex:2];
        NSNumber* maxSize = [NSNumber numberWithInt:[maxSizeString intValue]];
        NSString* destDirectory;
        NSString* destFilename;
        
        //Check if all the parameters are valid
        if([maxSize intValue] < 1){
            [self callback:CDVCommandStatus_ERROR withMessage:@"Invalid size specified" toCallbackId:command.callbackId];
        }
        if([[command arguments] objectAtIndex:3] == [NSNull null]){
            destDirectory = @"";
        }else{
            destDirectory = [[command arguments] objectAtIndex:3];
            destDirectory = [destDirectory substringFromIndex:7];
            destDirectory = [self correctPath:destDirectory];
        }
        if([[command arguments] objectAtIndex:4] == [NSNull null]){
            destFilename = @"";
        }else{
            destFilename = [[command arguments] objectAtIndex:4];
        }
        
        //Get the original image
        currentDirectory = [currentDirectory substringFromIndex:7];
        currentDirectory = [self correctPath:currentDirectory];
        NSString* fullPath = [NSString stringWithFormat:@"%@%@.jpg", currentDirectory, currentFilename];
        UIImage *image = [UIImage imageWithContentsOfFile:fullPath];
        
        //Check if the original image is already smaller than the specified size
        if([image size].height <= [maxSize intValue] && [image size].width <= [maxSize intValue]){
            //Image is small already
            [self callback:CDVCommandStatus_NO_RESULT withMessage:@"Image is smaller than the specified size" toCallbackId:command.callbackId];
        }
        
        //Resize the image
        UIImage *resizedImage;
        if([image size].width > [image size].height){
            //Landscape
            CGFloat factor = [image size].width / [maxSize intValue];
            int newHeight = (int)[image size].height/factor;
            CGSize newSize = CGSizeMake([maxSize intValue], newHeight);
            resizedImage = [self imageWithImage:image scaledToSize:newSize];
        }else if([image size].height > [image size].width){
            //Portrait
            CGFloat factor = [image size].height / [maxSize intValue];
            int newWidth = (int)[image size].width/factor;
            CGSize newSize = CGSizeMake(newWidth, [maxSize intValue]);
            resizedImage = [self imageWithImage:image scaledToSize:newSize];
        }else{
            CGSize newSize = CGSizeMake([maxSize intValue], [maxSize intValue]);
            resizedImage = [self imageWithImage:image scaledToSize:newSize];
        }
        
        //Save the image
        if (resizedImage) {
            //Construct the final path of the resized image
            NSString *newPath;
            if ([destDirectory length] == 0 || [destFilename length] == 0) {
                if ([destDirectory length] == 0) {newPath = currentDirectory;}else{newPath = destDirectory;}
                if ([destFilename length] == 0) {newPath = [newPath stringByAppendingString:[currentFilename stringByAppendingString:@".jpg"]];
                }else{newPath = [newPath stringByAppendingString:[destFilename stringByAppendingString:@".jpg"]];}
            }else{
                newPath = [destDirectory stringByAppendingString: [destFilename stringByAppendingString:@".jpg"]];
            }
            
            //Generate destination directory
            if ([destDirectory length] != 0) {
                BOOL isDirectory;
                if([[NSFileManager defaultManager] fileExistsAtPath:destDirectory isDirectory:&isDirectory] == false){
                    NSError * error = nil;
                    [[NSFileManager defaultManager] createDirectoryAtPath:destDirectory
                                              withIntermediateDirectories:YES
                                                               attributes:nil
                                                                    error:&error];
                    if (error != nil) {
                        NSLog(@"error creating directory: %@", error);
                        [self callback:CDVCommandStatus_ERROR withMessage:@"Error creating directory" toCallbackId:command.callbackId];
                    }
                }
            }
            //Save image
            if ([UIImageJPEGRepresentation(resizedImage, 1.0) writeToFile:newPath atomically:NO]) {
                [self callback:CDVCommandStatus_OK withMessage:[NSString stringWithFormat:@"file://%@", newPath] toCallbackId:command.callbackId];
            }else{
                [self callback:CDVCommandStatus_ERROR withMessage:@"Failed to save image" toCallbackId:command.callbackId];
            }
        }else{
            [self callback:CDVCommandStatus_NO_RESULT withMessage:@"Image wasn't resized" toCallbackId:command.callbackId];
        }
    }];
    
}

/**
 This method generates a thumbnail for a provided image
 and saves it in any other directory with a specified name.
 If the final directory or name are not specified it will
 append "-thumb.jpg" to the original image name and save 
 it in the same directory. Thumbnails are cropped to be 
 square shaped.
 ONLY DEALS WITH JPGs!
 
 - Required parameters:
 @param currentDirectory
        (String) Directory path for the original image
 @param currentFilename
        (String) Original image filename w/o the .jpg extension
 @param thumbSize
        (Integer) Size thumbnail. Thumbnails are squares.
 
 
 - Optional parameters:
 @param destDirectory
 (String) Destination directory in which the new JPG image will be saved.
 @param destFilename
 (String) JPG filename w/o the .jpg extension.
 */
- (void)thumbnail:(CDVInvokedUrlCommand*)command{
    
    [self.commandDelegate runInBackground:^{
        if([[command arguments] objectAtIndex:0] == [NSNull null] || [[command arguments] objectAtIndex:1] == [NSNull null] || [[command arguments] objectAtIndex:2] == [NSNull null]){
            [self callback:CDVCommandStatus_ERROR withMessage:@"Parameters missing" toCallbackId:command.callbackId];
        }
        NSString* currentDirectory = [[command arguments] objectAtIndex:0];
        NSString* currentFilename = [[command arguments] objectAtIndex:1];
        NSString* thumbSizeString = [[command arguments] objectAtIndex:2];
        NSNumber* thumbSize = [NSNumber numberWithInt:[thumbSizeString intValue]];
        NSString* destDirectory;
        NSString* destFilename;
        if([thumbSize intValue] < 1){
            [self callback:CDVCommandStatus_ERROR withMessage:@"Invalid size specified" toCallbackId:command.callbackId];
        }
        if([[command arguments] objectAtIndex:3] == [NSNull null]){
            destDirectory = @"";
        }else{
            destDirectory = [[command arguments] objectAtIndex:3];
            destDirectory = [destDirectory substringFromIndex:7];
            destDirectory = [self correctPath:destDirectory];
        }
        if([[command arguments] objectAtIndex:4] == [NSNull null]){
            destFilename = @"";
        }else{
            destFilename = [[command arguments] objectAtIndex:4];
        }
        
        
        currentDirectory = [currentDirectory substringFromIndex:7];
        currentDirectory = [self correctPath:currentDirectory];
        NSString* fullPath = [NSString stringWithFormat:@"%@%@.jpg", currentDirectory, currentFilename];
        UIImage *image = [UIImage imageWithContentsOfFile:fullPath];
        
        if([image size].height <= [thumbSize intValue] && [image size].width <= [thumbSize intValue]){
            //Image is small already
            [self callback:CDVCommandStatus_NO_RESULT withMessage:@"Image is smaller than the specified size" toCallbackId:command.callbackId];
        }
        UIImage *resizedImage;
        CGRect cropRect;
        if([image size].width > [image size].height){
            //Landscape
            CGFloat factor = [image size].height / [thumbSize intValue];
            int newWidth = (int)[image size].width/factor;
            CGSize newSize = CGSizeMake(newWidth, [thumbSize intValue]);
            resizedImage = [self imageWithImage:image scaledToSize:newSize];
            cropRect = CGRectMake([resizedImage size].width/2 - [thumbSize intValue]/2, 0, [thumbSize intValue], [thumbSize intValue]);
            resizedImage = [self cropImageByImage:resizedImage toRect:cropRect];
        }else if([image size].height > [image size].width){
            //Portrait
            CGFloat factor = [image size].width / [thumbSize intValue];
            int newHeight = (int)[image size].height/factor;
            CGSize newSize = CGSizeMake([thumbSize intValue], newHeight);
            resizedImage = [self imageWithImage:image scaledToSize:newSize];
            cropRect = CGRectMake(0, [resizedImage size].height/2 - [thumbSize intValue]/2, [thumbSize intValue], [thumbSize intValue]);
            resizedImage = [self cropImageByImage:resizedImage toRect:cropRect];
        }else{
            CGSize newSize = CGSizeMake([thumbSize intValue], [thumbSize intValue]);
            resizedImage = [self imageWithImage:image scaledToSize:newSize];
        }
        
        if (resizedImage) {
            //Construct the final path of the resized image
            NSString *newPath;
            if ([destDirectory length] == 0 || [destFilename length] == 0) {
                if ([destDirectory length] == 0) {newPath = currentDirectory;}else{newPath = destDirectory;}
                if ([destFilename length] == 0) {newPath = [newPath stringByAppendingString:[currentFilename stringByAppendingString:@"-thumb.jpg"]];
                }else{newPath = [newPath stringByAppendingString:[destFilename stringByAppendingString:@".jpg"]];}
            }else{
                newPath = [destDirectory stringByAppendingString: [destFilename stringByAppendingString:@".jpg"]];
            }
            
            //Generate directory
            if ([destDirectory length] != 0) {
                
                BOOL isDirectory;
                if([[NSFileManager defaultManager] fileExistsAtPath:destDirectory isDirectory:&isDirectory] == false){
                    NSError * error = nil;
                    [[NSFileManager defaultManager] createDirectoryAtPath:destDirectory
                                              withIntermediateDirectories:YES
                                                               attributes:nil
                                                                    error:&error];
                    if (error != nil) {
                        NSLog(@"error creating directory: %@", error);
                        [self callback:CDVCommandStatus_ERROR withMessage:@"Error creating directory" toCallbackId:command.callbackId];
                    }
                }
            }
            
            
            //Save image
            if ([UIImageJPEGRepresentation(resizedImage, 1.0) writeToFile:newPath atomically:NO]) {
                [self callback:CDVCommandStatus_OK withMessage:[NSString stringWithFormat:@"file://%@", newPath] toCallbackId:command.callbackId];
            }else{
                [self callback:CDVCommandStatus_ERROR withMessage:@"Failed to generate Thumbnail" toCallbackId:command.callbackId];
            }
        }else{
            [self callback:CDVCommandStatus_NO_RESULT withMessage:@"Thumbnail wasn't generated" toCallbackId:command.callbackId];
        }

    }];
    
}

/**
 This method rotates an image in a specified directory
 and saves it in any other directory with a specified name.
 If the final directory or name are not specified it will
 overwrite the original image.
 ONLY DEALS WITH JPGs!
 
 - Required parameters:
 @param currentDirectory
        (String) Directory path for the original image.
 @param currentFilename
        (String) Original image filename w/o the .jpg extension.
 @param degrees
        (Integer) Degrees by which the images is rotated. Either 90,180 or 270.
 
 
 - Optional parameters:
 @param destDirectory
 (String) Destination directory in which the new JPG image will be saved.
 @param destFilename
 (String) JPG filename w/o the .jpg extension.
 */
- (void)rotate:(CDVInvokedUrlCommand*)command{
    
    [self.commandDelegate runInBackground:^{
        //Check if any parameters are missing
        if([[command arguments] objectAtIndex:0] == [NSNull null] || [[command arguments] objectAtIndex:1] == [NSNull null] || [[command arguments] objectAtIndex:4] == [NSNull null]){
            [self callback:CDVCommandStatus_ERROR withMessage:@"Parameters missing" toCallbackId:command.callbackId];
        }
        
        NSString* currentDirectory = [[command arguments] objectAtIndex:0];
        NSString* currentFilename = [[command arguments] objectAtIndex:1];
        
        NSString* destDirectory;
        NSString* destFilename;
        
        NSString* degreesString = [[command arguments] objectAtIndex:4];
        NSNumber* degrees = [NSNumber numberWithInt:[degreesString intValue]];
        
        //Check destination folder and filename are nill
        if([[command arguments] objectAtIndex:2] == [NSNull null]){
            destDirectory = @"";
        }else{
            destDirectory = [[command arguments] objectAtIndex:2];
            destDirectory = [destDirectory substringFromIndex:7];
            destDirectory = [self correctPath:destDirectory];
        }
        if([[command arguments] objectAtIndex:3] == [NSNull null]){
            destFilename = @"";
        }else{
            destFilename = [[command arguments] objectAtIndex:3];
        }
        
        //Get the original image
        currentDirectory = [currentDirectory substringFromIndex:7];
        currentDirectory = [self correctPath:currentDirectory];
        NSString* fullPath = [NSString stringWithFormat:@"%@%@.jpg", currentDirectory, currentFilename];
        UIImage *image = [UIImage imageWithContentsOfFile:fullPath];
        
        //Get the desired orientation
        UIImageOrientation rot;
        UIImageOrientation originalRot = [image imageOrientation];
        switch ([degrees intValue]) {
            case 90:
                switch (originalRot) {
                    case UIImageOrientationUp:
                        rot = UIImageOrientationRight;
                        break;
                    case UIImageOrientationRight:
                        rot = UIImageOrientationDown;
                        break;
                    case UIImageOrientationDown:
                        rot = UIImageOrientationLeft;
                        break;
                    case UIImageOrientationLeft:
                        rot = UIImageOrientationUp;
                        break;
                    default:
                        rot = UIImageOrientationUp;
                        break;
                }
                break;
            case 180:
                switch (originalRot) {
                    case UIImageOrientationUp:
                        rot = UIImageOrientationDown;
                        break;
                    case UIImageOrientationRight:
                        rot = UIImageOrientationLeft;
                        break;
                    case UIImageOrientationDown:
                        rot = UIImageOrientationUp;
                        break;
                    case UIImageOrientationLeft:
                        rot = UIImageOrientationRight;
                        break;
                    default:
                        rot = UIImageOrientationUp;
                        break;
                }
                break;
            case 270:
                switch (originalRot) {
                    case UIImageOrientationUp:
                        rot = UIImageOrientationLeft;
                        break;
                    case UIImageOrientationRight:
                        rot = UIImageOrientationUp;
                        break;
                    case UIImageOrientationDown:
                        rot = UIImageOrientationRight;
                        break;
                    case UIImageOrientationLeft:
                        rot = UIImageOrientationDown;
                        break;
                    default:
                        rot = UIImageOrientationUp;
                        break;
                }
                break;
            default:
                rot = UIImageOrientationUp;
                break;
        }
        
        //Generate the new rotated image
        UIImage *rotatedImage = [[UIImage alloc] initWithCGImage: image.CGImage
                                                           scale: 1.0
                                                     orientation: rot];
        
        if (rotatedImage) {
            //Construct the final path of the resized image
            NSString *newPath;
            if ([destDirectory length] == 0 || [destFilename length] == 0) {
                if ([destDirectory length] == 0) {newPath = currentDirectory;}else{newPath = destDirectory;}
                if ([destFilename length] == 0) {newPath = [newPath stringByAppendingString:[currentFilename stringByAppendingString:@".jpg"]];
                }else{newPath = [newPath stringByAppendingString:[destFilename stringByAppendingString:@".jpg"]];}
            }else{
                newPath = [destDirectory stringByAppendingString: [destFilename stringByAppendingString:@".jpg"]];
            }
            
            //Generate directory
            if ([destDirectory length] != 0) {
                BOOL isDirectory;
                if([[NSFileManager defaultManager] fileExistsAtPath:destDirectory isDirectory:&isDirectory] == false){
                    NSError * error = nil;
                    [[NSFileManager defaultManager] createDirectoryAtPath:destDirectory
                                              withIntermediateDirectories:YES
                                                               attributes:nil
                                                                    error:&error];
                    if (error != nil) {
                        NSLog(@"error creating directory: %@", error);
                        [self callback:CDVCommandStatus_ERROR withMessage:@"Error creating directory" toCallbackId:command.callbackId];
                    }
                }
            }
            
            //Save image
            if ([UIImageJPEGRepresentation(rotatedImage, 1.0) writeToFile:newPath atomically:NO]) {
                [self callback:CDVCommandStatus_OK withMessage:[NSString stringWithFormat:@"file://%@", newPath] toCallbackId:command.callbackId];
            }else{
                [self callback:CDVCommandStatus_ERROR withMessage:@"Failed to generate Thumbnail" toCallbackId:command.callbackId];
            }
        }else{
            [self callback:CDVCommandStatus_NO_RESULT withMessage:@"Thumbnail wasn't generated" toCallbackId:command.callbackId];
        }
    }];
}



#pragma mark - Assisting methods
/**
 Resize image to a new CGSize
 
 @param image
        UIImage to be resized.
 @param newSize
        CGSize of the output image
 @returns UIImage
        Returns the resized image
 */
-(UIImage *)imageWithImage:(UIImage *)image scaledToSize:(CGSize)newSize {
    
    UIGraphicsBeginImageContextWithOptions(newSize, NO, 1.0);
    [image drawInRect:CGRectMake(0, 0, newSize.width, newSize.height)];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

/**
 Draw stroked white text on an image
 
 @param text
 NSString to be drawn.
 @param image
 UIImage to be drawn in
 @param point
 CGPoint at which to draw the text
 @returns UIImage
 Returns the modified image
 */
-(UIImage*) drawText:(NSString*) text
             inImage:(UIImage*)  image
             atPoint:(CGPoint)   point
{
    UIGraphicsBeginImageContextWithOptions(image.size, YES, 0.0f);
    [image drawInRect:CGRectMake(0,0,image.size.width,image.size.height)];
    CGRect rect = CGRectMake(point.x, point.y, image.size.width, image.size.height);
    [[UIColor whiteColor] set];
    
    UIFont *font = [UIFont systemFontOfSize:image.size.width/20];
    if([text respondsToSelector:@selector(drawInRect:withAttributes:)])
    {
        //iOS 7
        
        
        NSDictionary *att = @{NSFontAttributeName:font
                              ,NSForegroundColorAttributeName: [UIColor whiteColor]
                              };
        [text drawInRect:rect withAttributes:att];
        
        att = @{NSFontAttributeName:font
                ,NSStrokeWidthAttributeName: [NSNumber numberWithFloat:[font pointSize]/10]
                ,NSStrokeColorAttributeName: [UIColor blackColor]
                };
        [text drawInRect:rect withAttributes:att];
        
    }
    else
    {
        //legacy support
        [text drawInRect:CGRectIntegral(rect) withFont:font];
    }
    
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return newImage;
}


/**
 Crop image to a new CGRect
 
 @param imageToCrop
 UIImage to be cropped.
 @param rect
        CGRect of the output image
 @returns UIImage
        Returns the cropped image
 */
- (UIImage *)cropImageByImage:(UIImage *)imageToCrop toRect:(CGRect)rect{
    
    CGImageRef imageRef = CGImageCreateWithImageInRect([imageToCrop CGImage], rect);
    UIImage *cropped = [UIImage imageWithCGImage:imageRef];
    CGImageRelease(imageRef);
    
    return cropped;
}

/**
 Cordova plugin callback function
 
 @param status
        CDVCommandStatus
 @param msg
        Status message
 @param callbackId
 */
-(void)callback:(CDVCommandStatus)status withMessage:(NSString*)msg toCallbackId:(NSString*)callbackId{
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:status
                               messageAsString:msg];
    
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

/**
 Check if a directory path ends with a "/", if not append and return
 */
-(NSString *)correctPath:(NSString *)pathStr{
    if ([pathStr hasSuffix:@"/"]) {
        return pathStr;
    }else{
        NSString *newStr = [pathStr stringByAppendingString:@"/"];
        return newStr;
    }
}

@end
