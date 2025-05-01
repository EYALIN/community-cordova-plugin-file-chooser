#import <Cordova/CDV.h>

@interface FileChooserPlugin : CDVPlugin <UIDocumentPickerDelegate>

@property (nonatomic, strong) NSString* callbackId;
@property (nonatomic, strong) NSDictionary* options;

- (void)chooseFile:(CDVInvokedUrlCommand*)command;

@end
