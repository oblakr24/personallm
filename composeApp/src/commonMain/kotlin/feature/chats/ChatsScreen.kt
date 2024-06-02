package feature.chats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun ChatsScreen(component: ChatsComponent) {
    val state = component.state.collectAsState().value
    ChatsContent(state, onAction = component::onAction)
}
