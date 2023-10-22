//
//  LineLogin.m
//  LineLoginSample
//
//  Created by Takagi Kensuke on 2023/10/20.
//

#import <UIKit/UIKit.h>
#import <React/RCTLog.h>
#import "AppDelegate.h"
#import "RNCConfig.h"
#import "LineLogin.h"
@import LineSDK;

@implementation MBTLineLogin

// https://developer.apple.com/documentation/objectivec/nsobject/1418639-initialize
+ (void)initialize {
  if (self == [MBTLineLogin self]) {
    NSString *lineChannelId = [RNCConfig envFor:@"LINE_CHANNEL_ID"];
    RCTLogInfo(@"LINE channel ID: %@", lineChannelId);
    [[LineSDKLoginManager sharedManager] setupWithChannelID:lineChannelId universalLinkURL:nil];
  }
}

RCT_EXPORT_MODULE(LineLogin);

RCT_EXPORT_METHOD(login: (NSString *)nonce
                  resolver:(RCTPromiseResolveBlock)resolve
                  refector:(RCTPromiseRejectBlock)reject)
{
  RCTLogInfo(@"Trying LINE login");

  NSSet *permissions = [NSSet setWithObjects:
                        [LineSDKLoginPermission profile],
                        [LineSDKLoginPermission email],
                        [LineSDKLoginPermission openID],
                        nil];
  
  LineSDKLoginManagerParameters *parameters = [[LineSDKLoginManagerParameters new] init];
  parameters.IDTokenNonce = nonce;
  
  // appDelegate and rootViewController must be used in main thread
  dispatch_async(dispatch_get_main_queue(), ^{
    AppDelegate *delegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];

    [[LineSDKLoginManager sharedManager]
     loginWithPermissions:permissions
     inViewController:delegate.window.rootViewController
     parameters:parameters
     completionHandler:^(LineSDKLoginResult *result, NSError *error) {
      if (result) {
        RCTLogInfo(@"Succeeded login with LINE as %@", result.userProfile.displayName);
        NSURL* pictureUrl = result.userProfile.pictureURL;
        NSString* email = result.accessToken.IDToken.payload.email;
        NSDictionary *user = @{
          @"displayName": result.userProfile.displayName,
          @"pictureUrl": pictureUrl ? pictureUrl.absoluteString : [NSNull null],
          @"email": email ? email : [NSNull null],
          @"idToken": result.accessToken.IDTokenRaw,
        };
        resolve(user);
        return;
      } else {
        RCTLogInfo(@"[%ld]%@(%@)", error.code, error.description, error.localizedRecoverySuggestion);
        if ([error.domain isEqualToString:[LineSDKErrorConstant errorDomain]]) {
          // TODO: handle error
          reject([NSString stringWithFormat:@"%ld", error.code], error.description, error);
          return;
        }
        reject([NSString stringWithFormat:@"%ld", error.code], error.description, error);
        return;
      }
    }];
  });
}

RCT_EXPORT_METHOD(logout: (NSString *) dummy
                  resolver:(RCTPromiseResolveBlock)resolve
                  refector:(RCTPromiseRejectBlock)reject)
{
  [[LineSDKLoginManager sharedManager] logoutWithCompletionHandler:^(NSError *error) {
    if (error) {
      reject([NSString stringWithFormat:@"%ld", error.code], error.description, error);
      return;
    }
    resolve(@"success");
  }];
}
@end
