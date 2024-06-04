@file:OptIn(ExperimentalEncodingApi::class)

package feature.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.ImageBitmap
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import data.OpenAIAPIWrapper
import di.VMContext
import di.vmScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.RouteNavigator
import kotlin.coroutines.CoroutineContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Inject
class ImageComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    private val api: OpenAIAPIWrapper,
    @Assisted private val vmContext: VMContext,
) : VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    private val completion = MutableStateFlow<String?>(null)
    private val image = MutableStateFlow<ImageBitmap?>(null)

    val state: StateFlow<ImageContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            ImagePresenter(
                completionsFlow = completion,
                images = image,
            )
        }
    }

    @Composable
    private fun ImagePresenter(
        completionsFlow: StateFlow<String?>,
        images: StateFlow<ImageBitmap?>,
    ): ImageContentUIState {
        val completion = completionsFlow.collectAsState().value
        val image = images.collectAsState().value
        return ImageContentUIState(
            bitmap = image,
            completion = completion,
        )
    }

    fun onAction(action: ImageAction) {
        when (action) {
            is ImageAction.OnImageResultReceived -> {
                val img = action.image ?: return
                img.toByteArray()?.let { byteArray ->
                    completion.value = null
                    scope.launch {
                        val encoded = Base64.encode(byteArray)
                        api.getImageCompletions(
                            prompt = "What is on this image?",
                            imageEncoded = encoded
                        ).collect {
                            println("Resp is $it")
                            completion.value = it.optValue()?.message ?: "Error"
                        }
                    }
                }

                img.toImageBitmap()?.let { bitmap ->
                    image.update { bitmap }
                }
            }
        }
    }
}
