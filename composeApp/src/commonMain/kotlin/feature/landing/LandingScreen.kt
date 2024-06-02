package feature.landing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun LandingScreen(component: LandingComponent) {
    val state = component.state.collectAsState().value
    LandingContent(state, onAction = component::onAction)
}
