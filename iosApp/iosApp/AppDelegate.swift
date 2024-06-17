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
    
       let diComponent: AppComponent = App_iosKt.createAppComponent()
       var root: RootComponent!
       var backDispatcher: BackDispatcher!

       override init() {
           super.init()
           backDispatcher = App_iosKt.createBackDispatcher()

           let componentContext = App_iosKt.customDefaultComponentContext(backDispatcher: backDispatcher, lifecycle: ApplicationLifecycle())

           root = DefaultRootComponent(
               componentContext: componentContext,
               diComponent: diComponent
           )
       }
}
