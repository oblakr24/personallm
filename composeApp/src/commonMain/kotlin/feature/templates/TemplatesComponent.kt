package feature.templates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.buildAnnotatedString
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import data.repo.Template
import data.repo.TemplatesRepo
import di.VMContext
import di.vmScope
import feature.commonui.TemplateDisplayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.RouteNavigator
import util.formatted
import util.formattedReadable
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

    private val templates = repo.templatesFlow()

    val state: StateFlow<TemplatesContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            TemplatesPresenter(
                templatesFlow = templates,
            )
        }
    }

    @Composable
    private fun TemplatesPresenter(
        templatesFlow: Flow<List<Template>>,
    ): TemplatesContentUIState {
        val chats = templatesFlow.collectAsState(initial = null).value
        val templates = chats?.map {
            TemplateDisplayData(
                id = it.id,
                title = buildAnnotatedString { append(it.title) },
                subtitle = buildAnnotatedString { append("Last updated ${it.updatedAt?.formattedReadable()}") },
                date = "Created ${it.createdAt.formattedReadable()}",
            )
        }.orEmpty()
        return TemplatesContentUIState(
            templates = templates,
        )
    }

    fun onAction(action: TemplatesAction) {
        when (action) {
            is TemplatesAction.TemplateClicked -> {
                // TODO
            }
        }
    }
}
