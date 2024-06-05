package com.rokoblak.personallm

import App
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.core.platform.WeakReference
import com.arkivanov.decompose.defaultComponentContext
import di.AppComponent
import di.CurrentActivityProvider
import di.PlatformProviders
import di.create
import navigation.DefaultRootComponent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext as Application
        val activityProvider = CurrentActivityProvider(WeakReference(this))
        app.registerActivityLifecycleCallbacks(activityProvider.callback)

        val appComponent = AppComponent::class.create().apply {
            platformProviders = PlatformProviders(applicationContext, activityProvider)
        }

        val root =
            DefaultRootComponent(
                diComponent = appComponent,
                componentContext = defaultComponentContext(),
            )

        setContent {
            App(root = root)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    //App()
}