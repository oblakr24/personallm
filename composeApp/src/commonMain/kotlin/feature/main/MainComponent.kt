package feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.arkivanov.decompose.router.stack.push
import data.DarkModeState
import data.repo.Chat
import di.VMContext
import di.vmScope
import feature.chats.ChatsContentUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.DefaultRootComponent
import navigation.IntentHandler
import navigation.RouteNavigator
import usecase.DarkModeToggleUseCase
import kotlin.coroutines.CoroutineContext

@Inject
class MainComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    private val intentHandler: IntentHandler,
    private val darkModeToggleUseCase: DarkModeToggleUseCase,
    @Assisted private val pageNavigator: PageNavigation,
    @Assisted private val vmContext: VMContext,
): VMContext by vmContext, RouteNavigator by nav, PageNavigation by pageNavigator {

    private val scope = vmScope(mainContext)

    val state: StateFlow<MainScreenUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            MainPresenter(
                darkModeFlow = darkModeToggleUseCase.darkModeEnabled()
            )
        }
    }

    @Composable
    private fun MainPresenter(
        darkModeFlow: StateFlow<DarkModeState>,
    ): MainScreenUIState {
        val darkModeState = darkModeFlow.collectAsState(null).value
        val drawer = MainDrawerUIState(
            darkMode = darkModeState,
            versionLabel = "Version TODO",
        )
        return MainScreenUIState(drawer)
    }

    fun onAction(action: MainAction) {
        when (action) {
            MainAction.FAQClicked -> {
                navigation.push(DefaultRootComponent.Config.FAQ)
            }
            MainAction.OpenRepoUrl -> intentHandler.openURL("https://github.com/oblakr24/personallm")
            is MainAction.SetDarkMode -> scope.launch {
                darkModeToggleUseCase.setDarkMode(action.new)
            }
            MainAction.SetDarkModeFollowsSystem -> scope.launch {
                darkModeToggleUseCase.setDarkMode(DarkModeState.FOLLOW_SYSTEM)
            }
        }
    }
}
