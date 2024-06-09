package feature.chats

import alpha
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.commonui.ChatDisplay
import feature.commonui.ChatDisplayData
import feature.commonui.PrimaryButton
import feature.commonui.verticalScrollbar

data class ChatsContentUIState(
    val chats: List<ChatDisplayData> = emptyList(),
    val sortOrder: String = "",
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsContent(
    state: ChatsContentUIState,
    onAction: (ChatsAction) -> Unit,
) {
    Scaffold(content = {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize().verticalScrollbar(lazyListState)
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.alpha(0.8f)),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(state.sortOrder, modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    IconButton(onClick = {
                        onAction(ChatsAction.SortAscDescToggled)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            items(count = state.chats.size, key = { state.chats[it].id }, itemContent = { idx ->
                val item = state.chats[idx]
                ChatDisplay(modifier = Modifier.fillMaxWidth().clickable {
                    onAction(ChatsAction.ChatClicked(id = item.id))
                }, data = item)
            })

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }, floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = {
                onAction(ChatsAction.NewChatClicked)
            },
            icon = { Icon(Icons.Filled.Add, "Start New") },
            text = { Text(text = "Start New", color = MaterialTheme.colorScheme.onPrimaryContainer) },
        )
    }, floatingActionButtonPosition = FabPosition.End)

}
