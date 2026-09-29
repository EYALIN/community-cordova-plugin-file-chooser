#import "FileChooserPlugin.h"

@implementation FileChooserPlugin

- (void)chooseFile:(CDVInvokedUrlCommand*)command {
    self.callbackId = command.callbackId;
    // PLU-93: chooseFile() called with no options serializes to a single NSNull argument (not an
    // empty array), and an empty arguments array is possible too; either one used to crash on the
    // dictionary subscript below because neither NSNull nor a missing index responds like a dict.
    id rawOptions = command.arguments.count > 0 ? [command.arguments objectAtIndex:0] : nil;
    self.options = [rawOptions isKindOfClass:[NSDictionary class]] ? rawOptions : @{};
    NSString* mimeType = self.options[@"mimeType"] ?: @"public.data";
    BOOL multiple = [self.options[@"multiple"] boolValue];

    UIDocumentPickerViewController* documentPicker = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:@[mimeType] inMode:UIDocumentPickerModeImport];
    documentPicker.delegate = self;
    documentPicker.allowsMultipleSelection = multiple;
    [self.viewController presentViewController:documentPicker animated:YES completion:nil];
}

- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray<NSURL *> *)urls {
    if (urls.count > 0) {
        NSMutableArray* results = [NSMutableArray array];
        BOOL includeBase64 = [self.options[@"includeBase64"] boolValue];

        for (NSURL* url in urls) {
            NSError* error;
            NSData* data = [NSData dataWithContentsOfURL:url options:0 error:&error];
            if (data) {
                NSMutableDictionary* fileDetails = [NSMutableDictionary dictionary];
                NSString* fileName = [url lastPathComponent];
                NSString* path = [url path];
                NSString* extension = [url pathExtension];
                NSNumber* fileSize = @([data length]);

                fileDetails[@"fileName"] = fileName;
                fileDetails[@"path"] = path;
                fileDetails[@"extension"] = extension;
                fileDetails[@"fileSize"] = fileSize;

                if (includeBase64) {
                    NSString* base64String = [data base64EncodedStringWithOptions:0];
                    fileDetails[@"base64"] = base64String;
                }

                [results addObject:fileDetails];
            } else {
                CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
                [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
                return;
            }
        }

        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:results];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
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
