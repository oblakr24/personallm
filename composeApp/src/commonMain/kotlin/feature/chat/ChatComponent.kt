package feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import data.OpenAIAPIWrapper
import data.repo.Chat
import data.repo.ChatMessage
import data.repo.ChatRepo
import data.repo.Template
import data.repo.TemplatesRepo
import di.VMContext
import di.vmScope
import feature.commonui.CommonUIMappers.toDisplay
import feature.commonui.MessageDisplayData
import feature.commonui.TemplateDisplayData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
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
    private val templatesRepo: TemplatesRepo,
    @Assisted private val vmContext: VMContext,
    @Assisted private val config: DefaultRootComponent.Config.Chat,
): VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    private val chatId = MutableStateFlow(config.chatId)
    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    private val selectedModel = MutableStateFlow(OpenAIAPIWrapper.Model.V3)

    private val templates = templatesRepo.templatesFlow().stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val selectedTemplate = MutableStateFlow<Template?>(null)

    private val chat: Flow<Chat> = chatId.flatMapLatest { id ->
        if (id == null) emptyFlow() else repo.chatById(id).shareIn(scope, SharingStarted.Eagerly)
    }

    private val message = chatId.flatMapLatest { id ->
        if (id == null) emptyFlow() else repo.flow(id)
    }

    private val title = chatId.flatMapLatest { id ->
        if (id == null) emptyFlow() else chat.mapNotNull { c -> c.summary.takeIf { it.isNotBlank() } }
    }

    init {
        scope.launch {
            chat.firstOrNull()?.templateId?.let { templateId ->
                templatesRepo.templateById(templateId).firstOrNull()?.let { template ->
                    selectedTemplate.value = template
                }
            }
        }
    }

    val state: StateFlow<ChatContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            ChatPresenter(
                messageFlow = message,
                selectedModelFlow = selectedModel,
                titleFlow = title,
                templatesFlow = templates,
                selectedTemplateFlow = selectedTemplate,
            )
        }
    }

    @Composable
    private fun ChatPresenter(
        messageFlow: Flow<List<ChatMessage>>,
        selectedModelFlow: StateFlow<OpenAIAPIWrapper.Model>,
        titleFlow: Flow<String>,
        templatesFlow: Flow<List<Template>>,
        selectedTemplateFlow: Flow<Template?>,
    ): ChatContentUIState {
        val repoMessages = messageFlow.collectAsState(initial = null).value
        val selectedModel = selectedModelFlow.collectAsState().value
        val templates = templatesFlow.collectAsState(emptyList()).value
        val selectedTemplate = selectedTemplateFlow.collectAsState(null).value
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
        val templatesDisplays = templates?.map {
            it.toDisplay()
        }.orEmpty()
        return ChatContentUIState(
            messages = messages,
            models = models,
            selectedModel = selectedModel.displayName(),
            title = title,
            selectedTemplateId = selectedTemplate?.id,
            templates = templatesDisplays,
        )
    }

    fun onAction(action: ChatAction) {
        when (action) {
            ChatAction.SendClicked -> {
                val prompt = text.value
                _text.value = ""
                val model = selectedModel.value
                val template = selectedTemplate.value
                val existingChatId = chatId.value
                scope.launch {
                    val newChatId = repo.submitNew(existingChatId, prompt = prompt, model = model, template = template)
                    chatId.value = newChatId
                }
            }
            is ChatAction.TextChanged -> {
                _text.value = action.new
            }

            is ChatAction.ModelSelected -> {
                selectedModel.value = OpenAIAPIWrapper.Model.entries.first { it.value == action.display.value }
            }

            is ChatAction.TemplateSelected -> {
                selectedTemplate.value = templates.value.firstOrNull { action.display.id == it.id }
            }
        }
    }
}
