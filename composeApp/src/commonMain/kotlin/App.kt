import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import feature.commonui.AppSnackBar
import kotlinx.coroutines.flow.collectLatest
import navigation.RootComponent
import org.jetbrains.compose.ui.tooling.preview.Preview
import util.AppSnackbarData
import util.AppSnackbarVisuals

@Composable
@Preview
fun App(root: RootComponent) {
    AppTheme {
        val snackBarHostState = remember { SnackbarHostState() }

        val component = root.mainAppComponent
        Scaffold(
            snackbarHost = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    SnackbarHost(
                        modifier = Modifier.align(Alignment.TopCenter),
                        hostState = snackBarHostState
                    ) { snackbarData ->
                        val apVisuals =
                            (snackbarData.visuals as? AppSnackbarVisuals) ?: return@SnackbarHost
                        AppSnackBar(
                            title = apVisuals.message,
                            subtitle = apVisuals.subtitle,
                            onDismissRequest = {
                                snackBarHostState.currentSnackbarData?.dismiss()
                            },
                            modifier = Modifier.clickable {
                                snackBarHostState.currentSnackbarData?.dismiss()
                            },
                            showCloseRow = apVisuals.showCloseRow,
                            containerColor = when (apVisuals.type) {
                                AppSnackbarData.Type.NORMAL -> AppColors.SecondaryGreen
                                AppSnackbarData.Type.ERROR -> AppColors.SecondaryRed
                            },
                        )
                    }
                }
            },
        ) { contentPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
                Children(
                    stack = root.childStack,
                    modifier = Modifier,
                    animation = backAnimation(
                        backHandler = root.backHandler,
                        onBack = root::onBackClicked,
                    )
                ) { child ->
                    child.instance.Content()
                }
            }


        }

        LaunchedEffect(key1 = component, block = {
            component.snackbars.collectLatest { data ->
                showSnackbar(snackBarHostState, data)
            }
        })
    }
}

fun createBackDispatcher(): BackDispatcher = BackDispatcher()

fun customDefaultComponentContext(
    backDispatcher: BackDispatcher,
    lifecycle: Lifecycle
): DefaultComponentContext {
    return DefaultComponentContext(
        lifecycle = lifecycle,
        stateKeeper = null,
        instanceKeeper = null,
        backHandler = backDispatcher,
    )
}

private suspend fun showSnackbar(
    snackbarHostState: SnackbarHostState,
    data: AppSnackbarData,
) {
    val result = snackbarHostState
        .showSnackbar(data.toVisuals())
    when (result) {
        SnackbarResult.ActionPerformed -> Unit
        SnackbarResult.Dismissed -> Unit
    }
}

expect fun <C : Any, T : Any> backAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
): StackAnimation<C, T>