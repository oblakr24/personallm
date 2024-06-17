package com.rokoblak.personallm

import android.app.Application
import data.AppStorage
import di.AppComponent
import di.CurrentActivityProvider
import di.PlatformProviders
import di.createAppComponent

class AndroidApp : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        val activityProvider = CurrentActivityProvider(null)
        registerActivityLifecycleCallbacks(activityProvider.callback)

        val platformProviders = PlatformProviders(applicationContext, activityProvider)

        val appStorage = AppStorage(platformProviders.settingsFactory())

        appComponent = createAppComponent().apply {
            this.platformProviders = PlatformProviders(applicationContext, activityProvider)
            this.appStorage = appStorage
        }
    }
}
