package feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import data.OpenAIAPIWrapper
import data.repo.ChatMessage
import data.repo.ChatRepo
import di.VMContext
import di.vmScope
import feature.commonui.MessageDisplayData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.DefaultRootComponent
import navigation.RouteNavigator
import util.formatTimeElapsed
import util.formatted
import util.formattedReadable
import kotlin.coroutines.CoroutineContext


@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ChatComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    private val repo: ChatRepo,
    @Assisted private val vmContext: VMContext,
    @Assisted private val config: DefaultRootComponent.Config.Chat,
): VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    private val chatId = MutableStateFlow(config.chatId)
    val text = MutableStateFlow("")

    private val selectedModel = MutableStateFlow<OpenAIAPIWrapper.Model>(OpenAIAPIWrapper.Model.V3)

    private val message = chatId.flatMapLatest { id ->
        if (id == null) emptyFlow() else repo.flow(id)
    }

    private val title = chatId.flatMapLatest { id ->
        if (id == null) emptyFlow() else repo.chatById(id).mapNotNull { c -> c.summary.takeIf { it.isNotBlank() } }
    }

    val state: StateFlow<ChatContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            ChatPresenter(
                messageFlow = message,
                selectedModelFlow = selectedModel,
                titleFlow = title,
            )
        }
    }

    @Composable
    private fun ChatPresenter(
        messageFlow: Flow<List<ChatMessage>>,
        selectedModelFlow: StateFlow<OpenAIAPIWrapper.Model>,
        titleFlow: Flow<String>,
    ): ChatContentUIState {
        val repoMessages = messageFlow.collectAsState(initial = null).value
        val selectedModel = selectedModelFlow.collectAsState().value
        val title = titleFlow.collectAsState(null).value ?: "New Chat"
        val messages = repoMessages?.map {
            MessageDisplayData(
                id = it.id,
                content = it.content + if (it.finished) "" else " ... ",
                date = it.timestamp.formatTimeElapsed(),
                alignedLeft = !it.fromUser,
                avatar = null,
                imageUri = null,
            )
        }.orEmpty()
        val models = OpenAIAPIWrapper.Model.entries.map {
            ChatContentUIState.ModelDisplay(
                value = it.value,
                name = it.displayName(),
                selected = it == selectedModel,
            )
        }
        return ChatContentUIState(
            messages = messages,
            models = models,
            selectedModel = selectedModel.displayName(),
            title = title,
        )
    }

    fun onAction(action: ChatAction) {
        when (action) {
            ChatAction.SendClicked -> {
                val prompt = text.value
                text.value = ""
                val model = selectedModel.value
                val existingChatId = chatId.value
                scope.launch {
                    val newChatId = repo.submitNew(existingChatId, prompt = prompt, model = model)
                    chatId.value = newChatId
                }
            }
            is ChatAction.TextChanged -> {
                text.value = action.new
            }

            is ChatAction.ModelSelected -> {
                selectedModel.value = OpenAIAPIWrapper.Model.entries.first { it.value == action.display.value }
            }
        }
    }
}
