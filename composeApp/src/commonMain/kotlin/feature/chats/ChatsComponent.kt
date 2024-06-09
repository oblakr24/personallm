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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.DefaultRootComponent
import navigation.RouteNavigator
import util.formattedReadable
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ChatsComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    private val repo: ChatRepo,
    @Assisted private val vmContext: VMContext,
) : VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    private val sortAsc = MutableStateFlow(true)
    private val chats = sortAsc.flatMapLatest { repo.chatsFlow(it) }

    val state: StateFlow<ChatsContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            ChatsPresenter(
                chatFlow = chats,
                sortAscFlow = sortAsc,
            )
        }
    }

    @Composable
    private fun ChatsPresenter(
        chatFlow: Flow<List<Chat>>,
        sortAscFlow: StateFlow<Boolean>,
    ): ChatsContentUIState {
        val chats = chatFlow.collectAsState(initial = null).value
        val sortAsc = sortAscFlow.collectAsState().value
        val messages = chats?.map {
            ChatDisplayData(
                id = it.id,
                title = buildAnnotatedString { append(it.summary) },
                subtitle = buildAnnotatedString { append("Last updated ${it.lastMessageAt.formattedReadable()}") },
                date = "Created ${it.createdAt.formattedReadable()}",
            )
        }.orEmpty()
        return ChatsContentUIState(
            chats = messages,
            sortOrder = "Sort: " + if (sortAsc) "oldest first" else "newest first",
        )
    }

    fun onAction(action: ChatsAction) {
        when (action) {
            is ChatsAction.ChatClicked -> {
                nav.navigation.push(DefaultRootComponent.Config.Chat(chatId = action.id))
            }
            is ChatsAction.NewChatClicked -> {
                nav.navigation.push(DefaultRootComponent.Config.Chat(chatId = null))
            }

            ChatsAction.SortAscDescToggled -> {
                sortAsc.update { !it }
            }
        }
    }
}
