package feature.addtemplate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun AddTemplateScreen(component: AddTemplateComponent) {
    val state = component.state.collectAsState().value
    val title = component.title.collectAsState().value
    val prompt = component.prompt.collectAsState().value
    AddTemplateContent(
        state,
        title = title,
        prompt = prompt,
        onAction = component::onAction,
        onBackClicked = {
            component.navigateUp()
        })
}
