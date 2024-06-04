package feature.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.commonui.ChatDisplay
import feature.commonui.ChatDisplayData
import feature.commonui.verticalScrollbar


data class ChatsContentUIState(
    val chats: List<ChatDisplayData> = emptyList(),
)

@Composable
fun ChatsContent(
    state: ChatsContentUIState,
    onAction: (ChatsAction) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize().verticalScrollbar(lazyListState)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(count = state.chats.size, key = { state.chats[it].id }, itemContent = { idx ->
            val item = state.chats[idx]
            ChatDisplay(modifier = Modifier.fillMaxWidth().clickable {
                onAction(ChatsAction.ChatClicked(id = item.id))
            }, data = item)
        })
    }
}