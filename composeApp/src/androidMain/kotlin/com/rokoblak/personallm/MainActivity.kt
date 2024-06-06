package com.rokoblak.personallm

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.defaultComponentContext
import navigation.DefaultRootComponent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext as AndroidApp
        val appComponent = app.appComponent

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