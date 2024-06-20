package feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.ImageBitmap
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import data.Model
import data.displayName
import data.repo.Chat
import data.repo.ChatMessage
import data.repo.ChatRepo
import data.repo.InAppSignaling
import data.repo.Template
import data.repo.TemplatesRepo
import di.VMContext
import di.vmScope
import feature.commonui.CommonUIMappers.toDisplay
import feature.commonui.MessageDisplayData
import feature.sharedimage.SharedImage
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.DefaultRootComponent
import navigation.RouteNavigator
import util.formatTimeElapsed
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ChatComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    private val repo: ChatRepo,
    private val templatesRepo: TemplatesRepo,
    private val signaling: InAppSignaling,
    @Assisted private val vmContext: VMContext,
    @Assisted private val config: DefaultRootComponent.Config.Chat,
): VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    private val chatId = MutableStateFlow(config.chatId)
    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    private val templates = templatesRepo.templatesFlow().stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val inputState = MutableStateFlow(InputState())

    private val chat: Flow<Chat> = chatId.flatMapLatest { id ->
        if (id == null) emptyFlow() else repo.chatById(id).shareIn(scope, SharingStarted.Eagerly)
    }

    private val message = chatId.flatMapLatest { id ->
        if (id == null) emptyFlow() else repo.flow(id)
    }

    private val title = chatId.flatMapLatest { id ->
        if (id == null) emptyFlow() else chat.mapNotNull { c -> c.summary.takeIf { it.isNotBlank() } }
    }

    private val imageBitmap = inputState.map { it.attachedImage?.toImageBitmap() }.shareIn(scope, SharingStarted.Lazily)

    init {
        scope.launch {
            val templateId= if (config.chatId != null) chat.firstOrNull()?.templateId else null
            val initialTemplate = templateId?.let { templatesRepo.templateById(it).firstOrNull() }
            if (initialTemplate != null) {
                inputState.update { it.copy(selectedTemplate = initialTemplate) }
            } else {
                inputState.update { it.copy(showingTemplatesCarousel = config.chatId == null) }
            }
        }
    }

    val state: StateFlow<ChatContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            ChatPresenter(
                promptFlow = text,
                messageFlow = message,
                titleFlow = title,
                templatesFlow = templates,
                inputStateFlow = inputState,
                imageBitmapFlow = imageBitmap,
            )
        }
    }

    private var initialFocus: Boolean = true

    @Composable
    private fun ChatPresenter(
        promptFlow: StateFlow<String>,
        messageFlow: Flow<List<ChatMessage>>,
        titleFlow: Flow<String>,
        templatesFlow: Flow<List<Template>>,
        inputStateFlow: StateFlow<InputState>,
        imageBitmapFlow: Flow<ImageBitmap?>,
    ): ChatContentUIState {
        val prompt = promptFlow.collectAsState().value
        val repoMessages = messageFlow.collectAsState(initial = null).value
        val templates = templatesFlow.collectAsState(emptyList()).value
        val title = titleFlow.collectAsState(null).value ?: "New Chat"
        val inputState = inputStateFlow.collectAsState().value
        val imageBitmap = imageBitmapFlow.collectAsState(null).value

        val messages = repoMessages?.map {
            MessageDisplayData(
                id = it.id,
                content = it.content + if (it.finished) "" else " ... ",
                date = it.timestamp.formatTimeElapsed(),
                alignedLeft = !it.fromUser,
                avatar = null,
                imageUri = it.imageLocation?.uri,
                error = it.error,
            )
        }.orEmpty()
        val models = Model.allEntries().map {
            ChatContentUIState.ModelDisplay(
                value = it.value,
                name = it.displayName(),
                selected = it == inputState.selectedModel,
            )
        }
        val templatesDisplays = templates.map {
            it.toDisplay()
        }
        val focusToInput = !initialFocus && config.chatId != null
        this.initialFocus = false

        val sendEnabled = if (inputState.editState != null) {
            prompt != inputState.editState.orgPrompt
        } else {
            prompt.isNotBlank()
        }

        return ChatContentUIState(
            messages = messages,
            models = models,
            selectedModel = inputState.selectedModel.displayName(),
            title = title,
            selectedTemplateId = inputState.selectedTemplate?.id,
            templates = templatesDisplays,
            focusToInput = focusToInput,
            showTemplatesCarousel = inputState.showingTemplatesCarousel && inputState.selectedTemplate == null,
            inEditState = inputState.editState != null,
            sendEnabled = sendEnabled,
            attachedImage = imageBitmap,
            inputExpanded = inputState.expanded
        )
    }

    fun onAction(action: ChatAction) {
        when (action) {
            ChatAction.SendClicked -> {
                val prompt = text.value
                _text.value = ""
                val currInput = inputState.value
                val editState = currInput.editState
                val model = currInput.selectedModel
                val template = currInput.selectedTemplate
                val existingChatId = chatId.value
                val image = currInput.attachedImage
                scope.launch {
                    if (editState != null && existingChatId != null) {
                        repo.edit(chatId = existingChatId, messageId = editState.messageId, newPrompt = prompt, image = image, model = model, isFromUser = true, template = template)
                        inputState.update { it.copy(editState = null, expanded = false, attachedImage = null) }
                    } else {
                        val newChatId = repo.submitNew(existingChatId, prompt = prompt, image = image, model = model, template = template)
                        chatId.value = newChatId
                    }
                }
            }
            is ChatAction.TextChanged -> {
                _text.value = action.new
            }

            is ChatAction.ModelSelected -> {
                val newModel = Model.allEntries().first { it.value == action.display.value }
                inputState.update { it.copy(selectedModel = newModel) }
            }

            is ChatAction.TemplateSelected -> {
                val newTemplate = templates.value.firstOrNull { action.display.id == it.id }
                inputState.update { it.copy(selectedTemplate = newTemplate) }
                signaling.sendGenericMessage("Template ${newTemplate?.title} applied")
            }

            ChatAction.ShowNoTemplatesMessage -> {
                signaling.sendGenericMessage("No templates yet", subtitle = "Create a new template in the templates panel")
            }

            ChatAction.DismissTemplates -> {
                inputState.update { it.copy(showingTemplatesCarousel = false) }
            }

            is ChatAction.DeleteClicked -> {
                scope.launch {
                    val existingChatId = chatId.value ?: return@launch
                    repo.delete(chatId = existingChatId, messageId = action.display.id)
                }
            }

            is ChatAction.EditClicked -> {
                inputState.update { it.copy(editState = EditState(messageId = action.display.id, orgPrompt = action.display.content)) }
                _text.update { action.display.content }
            }

            is ChatAction.DismissEdit -> {
                inputState.update { it.copy(editState = null) }
                _text.update { "" }
            }

            is ChatAction.OnImageResultReceived -> {
                inputState.update { it.copy(attachedImage = action.image) }
            }

            ChatAction.OpenExpandedInput -> {
                inputState.update { it.copy(expanded = true, showingTemplatesCarousel = false) }
            }

            ChatAction.DismissExpandedInput -> {
                inputState.update { it.copy(expanded = false, attachedImage = null) }
            }

            ChatAction.RemoveImage -> {
                inputState.update { it.copy(attachedImage = null) }
            }
        }
    }
}

private data class InputState(
    val showingTemplatesCarousel: Boolean = false,
    val editState: EditState? = null,
    val expanded: Boolean = false,
    val attachedImage: SharedImage? = null,
    val selectedTemplate: Template? = null,
    val selectedModel: Model = Model.default(),
)

data class EditState(
    val messageId: String,
    val orgPrompt: String,
)
