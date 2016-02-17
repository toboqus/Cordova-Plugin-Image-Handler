#import <Cordova/CDV.h>

@interface ImageHandler : CDVPlugin

- (void) base64ToJpg:(CDVInvokedUrlCommand*)command;
- (void) resize:(CDVInvokedUrlCommand*)command;
- (void) thumbnail:(CDVInvokedUrlCommand*)command;
- (void) rotate:(CDVInvokedUrlCommand*)command;
- (void) timestamp:(CDVInvokedUrlCommand*)command;


@end
