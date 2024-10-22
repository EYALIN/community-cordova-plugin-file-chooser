#import <Cordova/CDV.h>

@interface FileChooserPlugin : CDVPlugin

- (void)chooseFile:(CDVInvokedUrlCommand*)command;

@end
