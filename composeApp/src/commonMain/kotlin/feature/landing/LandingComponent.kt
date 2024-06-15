package feature.landing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.arkivanov.decompose.router.stack.push
import data.DarkModeState
import di.VMContext
import di.vmScope
import getPlatform
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
class LandingComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    private val intentHandler: IntentHandler,
    private val darkModeToggleUseCase: DarkModeToggleUseCase,
    @Assisted private val vmContext: VMContext,
): VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    val state: StateFlow<LandingContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            LandingPresenter(
                darkModeFlow = darkModeToggleUseCase.darkModeEnabled()
            )
        }
    }

    @Composable
    private fun LandingPresenter(
        darkModeFlow: StateFlow<DarkModeState>,
    ): LandingContentUIState {
        val darkModeState = darkModeFlow.collectAsState(null).value
        val drawer = LandingDrawerUIState(
            darkMode = darkModeState,
            versionLabel = "Version ${getPlatform().appVersion.name}",
        )
        return LandingContentUIState(drawer)
    }

    fun onAction(action: LandingAction) {
        when (action) {
            LandingAction.OpenHome -> {
                nav.navigation.push(DefaultRootComponent.Config.SampleRequest)
            }

            LandingAction.OpenImage -> {
                nav.navigation.push(DefaultRootComponent.Config.Image)
            }

            LandingAction.OpenListing -> {
                nav.navigation.push(DefaultRootComponent.Config.Listing)
            }

            LandingAction.OpenChat -> {
                nav.navigation.push(DefaultRootComponent.Config.Chat(chatId = null))
            }

            LandingAction.FAQClicked -> {
                navigation.push(DefaultRootComponent.Config.FAQ)
            }
            LandingAction.OpenRepoUrl -> intentHandler.openURL("https://github.com/oblakr24/personallm")

            is LandingAction.SetDarkMode -> scope.launch {
                darkModeToggleUseCase.setDarkMode(action.new)
            }
        }
    }
}
