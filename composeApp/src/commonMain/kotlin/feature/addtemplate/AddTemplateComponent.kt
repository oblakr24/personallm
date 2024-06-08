package feature.addtemplate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import data.repo.InAppSignaling
import data.repo.Template
import data.repo.TemplatesRepo
import di.VMContext
import di.vmScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.DefaultRootComponent
import navigation.RouteNavigator
import util.randomUUID
import kotlin.coroutines.CoroutineContext

@Inject
class AddTemplateComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    private val templatesRepo: TemplatesRepo,
    private val signaling: InAppSignaling,
    @Assisted private val vmContext: VMContext,
    @Assisted private val config: DefaultRootComponent.Config.AddTemplate,
): VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    private val _title = MutableStateFlow("")
    private val _prompt = MutableStateFlow("")

    val title = _title.asStateFlow()
    val prompt = _prompt.asStateFlow()

    private val initialTemplate = config.templateId?.let { id ->
        templatesRepo.templateById(id).shareIn(scope, SharingStarted.Eagerly, replay = 1)
    } ?: emptyFlow()

    init {
        scope.launch {
            initialTemplate.firstOrNull()?.let { template ->
                _title.value = template.title
                _prompt.value = template.prompt
            }
        }
    }

    val state: StateFlow<AddTemplateContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            AddTemplatePresenter(
                titleFlow = title,
                promptFlow = prompt,
                initialTemplateFlow = initialTemplate,
            )
        }
    }

    @Composable
    private fun AddTemplatePresenter(
        titleFlow: StateFlow<String>,
        promptFlow: StateFlow<String>,
        initialTemplateFlow: Flow<Template>,
    ): AddTemplateContentUIState {
        val title = titleFlow.collectAsState().value
        val prompt = promptFlow.collectAsState().value
        val initial = initialTemplateFlow.collectAsState(null).value
        val dataIsSame = initial?.let {
            it.title == title && it.prompt == prompt
        } ?: (title.isBlank() && prompt.isBlank())

        return AddTemplateContentUIState(
            saveEnabled = !dataIsSame,
            new = config.templateId == null,
        )
    }

    fun onAction(action: AddTemplateAction) {
        when (action) {
            is AddTemplateAction.OnPromptChanged -> _prompt.update { action.new }
            is AddTemplateAction.OnTitleChanged -> _title.update { action.new }
            AddTemplateAction.SaveClicked -> {
                scope.launch {
                    val initial = initialTemplate.firstOrNull()
                    val newOrUpdatedTemplate = Template(
                        id = initial?.id ?: randomUUID(),
                        title = title.value,
                        prompt = prompt.value,
                        createdAt = initial?.createdAt ?: Clock.System.now(),
                        updatedAt = initial?.updatedAt?.let { Clock.System.now() },
                    )
                    templatesRepo.insertOrUpdate(newOrUpdatedTemplate)
                    signaling.sendGenericMessage(if (initial != null) "Template updated" else "Template created")
                    nav.navigateUp()
                }
            }
        }
    }
}
