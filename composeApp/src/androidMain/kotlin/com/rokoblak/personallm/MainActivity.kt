package com.rokoblak.personallm

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.defaultComponentContext
import di.AppComponent
import di.PlatformProviders
import di.create
import navigation.DefaultRootComponent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = AppComponent::class.create().apply {
            platformProviders = PlatformProviders(applicationContext)
        }

        val root =
            DefaultRootComponent(
                diComponent = appComponent,
                componentContext = defaultComponentContext(),
            )

        setContent {
            App(appComponent, root = root)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    //App()
}