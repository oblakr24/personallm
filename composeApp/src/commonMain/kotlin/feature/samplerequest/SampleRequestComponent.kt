package feature.samplerequest

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

@Inject
class SampleRequestComponent(
    private val api: OpenAIAPIWrapper,
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    @Assisted private val vmContext: VMContext,
): VMContext by vmContext, RouteNavigator by nav {

    private val vmScope = vmScope(mainContext)

    private val _state: MutableStateFlow<SampleRequestUIState> = MutableStateFlow(SampleRequestUIState(null))
    val state: StateFlow<SampleRequestUIState> = _state

    fun onAction(action: SampleRequestAction) {
        when (action) {
            SampleRequestAction.ExecuteRequestClicked -> executeRequest()
        }
    }

    private fun executeRequest() {
        vmScope.launch {
            _state.update { it.copy(loading = true, response = null) }
            val prompt = "say something funny. do it 5 times."
            api.getChatCompletions(prompt).collect { value ->
                _state.update { it.copy(response = value.optValue()?.message, loading = false) }
            }
        }
    }
}
