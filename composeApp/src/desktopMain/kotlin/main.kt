import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import di.AppComponent
import di.PlatformProviders
import di.create
import navigation.DefaultRootComponent
import javax.swing.SwingUtilities

@OptIn(ExperimentalDecomposeApi::class)
fun main() {

    val appComponent = AppComponent::class.create().apply {
        platformProviders = PlatformProviders()
    }
    val lifecycle = LifecycleRegistry()
    // Always create the root component outside Compose on the UI thread
    val root =
        runOnUiThread {
            DefaultRootComponent(
                diComponent = appComponent,
                componentContext = DefaultComponentContext(lifecycle = lifecycle),
            )
        }

    application {
        val windowState = rememberWindowState()

        LifecycleController(lifecycle, windowState)

        Window(onCloseRequest = ::exitApplication, state = windowState, title = "PersonaLLM") {

            App(component = appComponent, root = root)
        }
    }
}

internal fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var error: Throwable? = null
    var result: T? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (e: Throwable) {
            error = e
        }
    }

    error?.also { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}