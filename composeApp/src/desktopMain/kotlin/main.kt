import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import data.AppStorage
import di.PlatformProviders
import di.createAppComponent
import navigation.DefaultRootComponent
import javax.swing.SwingUtilities

fun main() {
    val platformProviders = PlatformProviders()
    val appStorage = AppStorage(platformProviders.settingsFactory())
    val appComponent = createAppComponent().apply {
        this.platformProviders = platformProviders
        this.appStorage = appStorage
    }
    val lifecycle = LifecycleRegistry()
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
            App(root = root)
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