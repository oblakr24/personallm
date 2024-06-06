//
//  AppDelegate.swift
//  iosApp
//
//  Created by Rok Oblak on 13. 04. 24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import ComposeApp
import UIKit

class AppDelegate: NSObject, UIApplicationDelegate {
    
       let diComponent: AppComponent = InjectAppComponent()
       var root: RootComponent!
       var backDispatcher: BackDispatcher!

       override init() {
           let platformProviders = PlatformProviders()
           diComponent.platformProviders = platformProviders
           
           let appStorage = AppStorage(factory: platformProviders.settingsFactory())
           diComponent.appStorage = appStorage
           
           super.init()
           backDispatcher = App_iosKt.createBackDispatcher()

           let componentContext = App_iosKt.customDefaultComponentContext(backDispatcher: backDispatcher, lifecycle: ApplicationLifecycle())

           root = DefaultRootComponent(
               componentContext: componentContext,
               diComponent: diComponent
           )
       }
}
