package feature.landing

import com.arkivanov.decompose.router.stack.push
import di.VMContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.DefaultRootComponent
import navigation.RouteNavigator

@Inject
class LandingComponent(
    private val nav: RouteNavigator,
    @Assisted private val vmContext: VMContext,
): VMContext by vmContext, RouteNavigator by nav {

    val state: StateFlow<LandingContentUIState> by lazy {
        MutableStateFlow(LandingContentUIState())
    }

    fun onAction(action: LandingAction) {
        when (action) {
            LandingAction.OpenHome -> {
                nav.navigation.push(DefaultRootComponent.Config.Home)
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
        }
    }
}
