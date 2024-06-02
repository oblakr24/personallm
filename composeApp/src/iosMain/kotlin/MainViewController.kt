import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureIcon
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import navigation.RootComponent
import platform.UIKit.UIViewController

@OptIn(ExperimentalDecomposeApi::class)
fun MainViewController(root: RootComponent, backDispatcher: BackDispatcher): UIViewController {
    return ComposeUIViewController(configure = {
        this.onFocusBehavior = OnFocusBehavior.DoNothing // We let the scaffold's IME padding modifiers adjust the keyboard padding
    }) {
        PredictiveBackGestureOverlay(
            backDispatcher = backDispatcher,
            backIcon = { progress, _ ->
                PredictiveBackGestureIcon(
                    imageVector = Icons.Default.ArrowBack,
                    progress = progress,
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            App(root)
        }
    }
}
