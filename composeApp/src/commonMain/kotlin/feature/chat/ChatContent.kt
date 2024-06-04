package feature.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.commonui.InputBar
import feature.commonui.MessageDisplay
import feature.commonui.MessageDisplayData
import feature.commonui.TitledScaffold
import feature.commonui.verticalScrollbar

data class ChatContentUIState(
    val messages: List<MessageDisplayData> = emptyList(),
)

@Composable
fun ChatContent(
    state: ChatContentUIState,
    text: String, onAction: (ChatAction) -> Unit, onBackClicked: () -> Unit
) {
    TitledScaffold("Chat", onBackClicked = onBackClicked, content = {
        val lazyListState = rememberLazyListState()

        LazyColumn(state = lazyListState, modifier = Modifier.verticalScrollbar(lazyListState)) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(
                count = state.messages.size,
                key = { state.messages[it].id },
                itemContent = { idx ->
                    val item = state.messages[idx]
                    MessageDisplay(modifier = Modifier.fillMaxWidth(), data = item)
                })
        }
    }, footer = {
        Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            InputBar(
                input = text,
                modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(24.dp),
                onChange = {
                    onAction(ChatAction.TextChanged(it))
                },
                onSend = {
                    onAction(ChatAction.SendClicked)
                })
        }
    })
}
