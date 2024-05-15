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

       override init() {

           diComponent.platformProviders = PlatformProviders()
           super.init()
           root = DefaultRootComponent(
               componentContext: DefaultComponentContext(lifecycle: ApplicationLifecycle()),
               diComponent: diComponent
           )
       }
}
