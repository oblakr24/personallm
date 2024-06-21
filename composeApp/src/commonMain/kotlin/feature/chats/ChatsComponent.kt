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
import feature.commonui.ExtendedToolbarState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    private val sortAsc = MutableStateFlow(false)
    private val chats = sortAsc.flatMapLatest { repo.chatsFlow(it) }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val editState = MutableStateFlow(EditState())

    val state: StateFlow<ChatsContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            ChatsPresenter(
                chatFlow = chats,
                sortAscFlow = sortAsc,
                editStateFlow = editState,
            )
        }
    }

    @Composable
    private fun ChatsPresenter(
        chatFlow: StateFlow<List<Chat>>,
        sortAscFlow: StateFlow<Boolean>,
        editStateFlow: StateFlow<EditState>,
    ): ChatsContentUIState {
        val chats = chatFlow.collectAsState().value
        val sortAsc = sortAscFlow.collectAsState().value

        val editState = editStateFlow.collectAsState().value

        val messages = chats.map {
            ChatDisplayData(
                id = it.id,
                title = buildAnnotatedString { append(it.summary) },
                subtitle = buildAnnotatedString { append("Last updated ${it.lastMessageAt.formattedReadable()}") },
                date = "Created ${it.createdAt.formattedReadable()}",
                checked = if (!editState.editing || !editState.expanded) null else editState.selectedIds.contains(it.id)
            )
        }

        val extendedToolbarState = if (editState.expanded) {
            ExtendedToolbarState(
                showEdit = editState.editing,
                deleteEnabled = editState.selectedIds.isNotEmpty()

            )
        } else {
            null
        }
        return ChatsContentUIState(
            chats = messages,
            sortOrder = if (sortAsc) "Oldest first" else "Newest first",
            extendedToolbarState = extendedToolbarState,
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

            ChatsAction.ExtendedSettingsToggled -> {
                editState.update {
                    it.copy(expanded = !it.expanded)
                }
            }

            is ChatsAction.ItemCheckedToggled -> {
                editState.update {
                    it.copy(
                        selectedIds = it.selectedIds.toggledWith(action.id)
                    )
                }
            }

            ChatsAction.EditToggled -> {
                editState.update {
                    it.copy(editing = !it.editing)
                }
            }

            ChatsAction.ClearSelection -> {
                editState.update {
                    it.copy(
                        selectedIds = emptySet()
                    )
                }
            }

            ChatsAction.DeleteConfirmed -> {
                scope.launch {
                    val idsToDelete = editState.value.selectedIds
                    repo.deleteChats(ids = idsToDelete)
                    editState.update { EditState() }
                }
            }

            ChatsAction.SelectAll -> {
                val allChats = chats.value.map { it.id }
                editState.update {
                    it.copy(
                        selectedIds = allChats.toSet()
                    )
                }
            }
        }
    }
}

private fun Set<String>.toggledWith(new: String): Set<String> {
    return toMutableSet().apply {
        if (contains(new)) {
            remove(new)
        } else {
            add(new)
        }
    }.toSet()
}

private data class EditState(
    val expanded: Boolean = false,
    val editing: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
)
