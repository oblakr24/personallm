package navigation

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop

interface RouteNavigator {
    fun navigateUp() {
        navigation.pop()
    }

    val navigation: StackNavigation<DefaultRootComponent.Config>
}

expect class IntentHandler {

    fun openURL(url: String)

}