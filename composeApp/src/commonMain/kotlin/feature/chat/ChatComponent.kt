package feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import data.repo.Chat
import data.repo.ChatMessage
import data.repo.ChatRepo
import di.VMContext
import di.vmScope
import feature.commonui.MessageDisplayData
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.DefaultRootComponent
import navigation.RouteNavigator
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

    private val chats = chatId.flatMapLatest {
        if (it == null) emptyFlow() else repo.flow(it)
    }

    val state: StateFlow<ChatContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            ChatPresenter(
                chatFlow = chats,
            )
        }
    }

    @Composable
    private fun ChatPresenter(
        chatFlow: Flow<List<ChatMessage>>,
    ): ChatContentUIState {
        val repoMessages = chatFlow.collectAsState(initial = null).value
        val messages = repoMessages?.map {
            MessageDisplayData(
                id = it.id,
                content = it.content + if (it.finished) "" else " ... ",
                date = it.timestamp.toString(),
                alignedLeft = !it.fromUser,
                avatar = null,
                imageUri = null,
            )
        }.orEmpty()
        return ChatContentUIState(
            messages = messages,
        )
    }

    fun onAction(action: ChatAction) {
        when (action) {
            ChatAction.SendClicked -> {
                val prompt = text.value
                text.value = ""
                val existingChatId = chatId.value
                scope.launch {
                    val newChatId = repo.submitNew(existingChatId, prompt = prompt)
                    chatId.value = newChatId
                }
            }
            is ChatAction.TextChanged -> {
                text.value = action.new
            }
        }
    }
}
