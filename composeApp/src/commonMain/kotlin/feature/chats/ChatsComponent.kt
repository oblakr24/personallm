package feature.chats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.buildAnnotatedString
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.arkivanov.decompose.router.stack.push
import data.repo.Chat
import data.repo.ChatRepo
import di.VMContext
import di.vmScope
import feature.commonui.ChatDisplayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.DefaultRootComponent
import navigation.RouteNavigator
import util.formatted
import kotlin.coroutines.CoroutineContext

@Inject
class ChatsComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    private val repo: ChatRepo,
    @Assisted private val vmContext: VMContext,
) : VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    val text = MutableStateFlow("")

    private val chats = repo.chatsFlow()

    val state: StateFlow<ChatsContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            ChatsPresenter(
                chatFlow = chats,
            )
        }
    }

    @Composable
    private fun ChatsPresenter(
        chatFlow: Flow<List<Chat>>,
    ): ChatsContentUIState {
        val chats = chatFlow.collectAsState(initial = null).value
        val messages = chats?.map {
            ChatDisplayData(
                id = it.id,
                title = buildAnnotatedString { append(it.summary) },
                subtitle = buildAnnotatedString { append("Last updated ${it.lastMessageAt.formatted()}") },
                date = "Created ${it.createdAt.formatted()}",
            )
        }.orEmpty()
        return ChatsContentUIState(
            chats = messages,
        )
    }

    fun onAction(action: ChatsAction) {
        when (action) {
            is ChatsAction.ChatClicked -> {
                nav.navigation.push(DefaultRootComponent.Config.Chat(chatId = action.id))
            }
        }
    }
}
