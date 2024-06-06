package feature.templates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun TemplatesScreen(component: TemplatesComponent) {
    val state = component.state.collectAsState().value
    TemplatesContent(state, onAction = component::onAction)
}
