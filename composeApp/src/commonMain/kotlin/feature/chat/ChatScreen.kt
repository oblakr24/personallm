package feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun ChatScreen(component: ChatComponent) {
    val state = component.state.collectAsState().value
    val text = component.text.collectAsState().value
    ChatContent(state, text, onAction = component::onAction, onBackClicked = {
        component.navigateUp()
    })
}
