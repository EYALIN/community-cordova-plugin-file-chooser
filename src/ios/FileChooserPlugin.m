#import "FileChooserPlugin.h"

@implementation FileChooserPlugin

- (void)chooseFile:(CDVInvokedUrlCommand*)command {
    self.callbackId = command.callbackId;
    NSDictionary* options = [command.arguments objectAtIndex:0];
    NSString* mimeType = options[@"mimeType"] ?: @"*/*";

    UIDocumentPickerViewController* documentPicker = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:@[mimeType] inMode:UIDocumentPickerModeImport];
    documentPicker.delegate = self;
    [self.viewController presentViewController:documentPicker animated:YES completion:nil];
}

- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray<NSURL *> *)urls {
    if (urls.count > 0) {
        NSURL* url = urls[0];
        NSError* error;
        NSData* data = [NSData dataWithContentsOfURL:url options:0 error:&error];
        if (data) {
            NSString* responseType = self.options[@"responseType"] ?: @"path";
            if ([responseType isEqualToString:@"base64"]) {
                NSString* base64String = [data base64EncodedStringWithOptions:0];
                CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:base64String];
                [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
            } else {
                NSString* path = [url path];
                CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:path];
                [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
            }
        } else {
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
            [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        }
    } else {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"File selection canceled"];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    }
}

- (void)documentPickerWasCancelled:(UIDocumentPickerViewController *)controller {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"File selection canceled"];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

@end
