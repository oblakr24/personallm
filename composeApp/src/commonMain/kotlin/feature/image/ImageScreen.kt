package feature.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun ImageScreen(component: ImageComponent) {
    val state = component.state.collectAsState().value
    ImageContent(state, onAction = component::onAction, onBackClicked = {
        component.navigateUp()
    })
}
