import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

@OptIn(ExperimentalDecomposeApi::class)
actual fun <C : Any, T : Any> backAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
): StackAnimation<C, T> =
    predictiveBackAnimation(
        backHandler = backHandler,
        fallbackAnimation = stackAnimation(fade()),
        selector = { backEvent, _, _ -> androidPredictiveBackAnimatable(backEvent) },
        onBack = onBack,
    )