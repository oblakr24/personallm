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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        val templates = templatesFlow.collectAsState(initial = null).value
        val templatesDisplays = templates?.map {
            it.toDisplay()
        }.orEmpty()
        return TemplatesContentUIState(
            templates = templatesDisplays,
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
        }
    }
}
