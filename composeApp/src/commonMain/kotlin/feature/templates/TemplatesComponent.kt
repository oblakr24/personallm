package feature.templates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.arkivanov.decompose.router.stack.push
import data.repo.Template
import data.repo.TemplatesRepo
import di.VMContext
import di.vmScope
import feature.commonui.CommonUIMappers.toDisplay
import feature.commonui.ExtendedToolbarState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.DefaultRootComponent
import navigation.RouteNavigator
import kotlin.coroutines.CoroutineContext

@Inject
class TemplatesComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    private val repo: TemplatesRepo,
    @Assisted private val vmContext: VMContext,
) : VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    val text = MutableStateFlow("")

    private val templates = repo.templatesFlow().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val editState = MutableStateFlow(EditState())

    val state: StateFlow<TemplatesContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            TemplatesPresenter(
                templatesFlow = templates,
                editStateFlow = editState,
            )
        }
    }

    @Composable
    private fun TemplatesPresenter(
        templatesFlow: StateFlow<List<Template>>,
        editStateFlow: StateFlow<EditState>,
    ): TemplatesContentUIState {
        val templates = templatesFlow.collectAsState().value
        val editState = editStateFlow.collectAsState().value
        val templatesDisplays = templates.map {
            it.toDisplay(
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

        return TemplatesContentUIState(
            templates = templatesDisplays,
            extendedToolbarState = extendedToolbarState,
        )
    }

    fun onAction(action: TemplatesAction) {
        when (action) {
            is TemplatesAction.TemplateClicked -> {
                nav.navigation.push(DefaultRootComponent.Config.AddTemplate(action.id))
            }

            TemplatesAction.AddNewClicked -> {
                nav.navigation.push(DefaultRootComponent.Config.AddTemplate(null))
            }

            TemplatesAction.ExtendedSettingsToggled -> {
                editState.update {
                    it.copy(expanded = !it.expanded)
                }
            }

            is TemplatesAction.ItemCheckedToggled -> {
                editState.update {
                    it.copy(
                        selectedIds = it.selectedIds.toggledWith(action.id)
                    )
                }
            }

            TemplatesAction.EditToggled -> {
                editState.update {
                    it.copy(editing = !it.editing)
                }
            }

            TemplatesAction.ClearSelection -> {
                editState.update {
                    it.copy(
                        selectedIds = emptySet()
                    )
                }
            }

            TemplatesAction.DeleteConfirmed -> {
                scope.launch {
                    val idsToDelete = editState.value.selectedIds
                    repo.deleteByIds(ids = idsToDelete)
                    editState.update { EditState() }
                }
            }

            TemplatesAction.SelectAll -> {
                val allTemplates = templates.value.map { it.id }
                editState.update {
                    it.copy(
                        selectedIds = allTemplates.toSet()
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

