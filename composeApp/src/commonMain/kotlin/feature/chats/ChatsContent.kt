package feature.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowCircleUp
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.CurtainsClosed
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Expand
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.commonui.ChatDisplay
import feature.commonui.ChatDisplayData
import feature.commonui.ExtendedToolbar
import feature.commonui.ExtendedToolbarState
import feature.commonui.TitledScaffold
import feature.commonui.verticalScrollbar

data class ChatsContentUIState(
    val chats: List<ChatDisplayData>,
    val sortOrder: String,
    val extendedToolbarState: ExtendedToolbarState?,
)

@Composable
fun ChatsContent(
    state: ChatsContentUIState,
    onAction: (ChatsAction) -> Unit,
) {
    TitledScaffold(title = "Chats",
        leadingIcon = if (state.chats.isNotEmpty() && state.extendedToolbarState == null) {
            {
                IconButton(onClick = {
                    onAction(ChatsAction.ExtendedSettingsToggled)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Edit toggle",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        } else null,
        actions = {
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    state.sortOrder,
                    modifier = Modifier.padding(4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
        },
        content = {
            Column {
                if (state.extendedToolbarState != null) {
                    ExtendedToolbar(
                        state = state.extendedToolbarState,
                        onEditClicked = {
                            onAction(ChatsAction.EditToggled)
                        },
                        onClearSelection = {
                            onAction(ChatsAction.ClearSelection)
                        },
                        onSelectAll = {
                            onAction(ChatsAction.SelectAll)
                        },
                        onDeleteConfirmed = {
                            onAction(ChatsAction.DeleteConfirmed)
                        },
                        onClose = {
                            onAction(ChatsAction.ExtendedSettingsToggled)
                        }
                    )
                }
                val lazyListState = rememberLazyListState()
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize().verticalScrollbar(lazyListState)
                ) {
                    items(count = state.chats.size, key = { state.chats[it].id }, itemContent = { idx ->
                        val item = state.chats[idx]
                        ChatDisplay(modifier = Modifier.fillMaxWidth().clickable {
                            onAction(ChatsAction.ChatClicked(id = item.id))
                        }, data = item, onCheckedChanged = {
                            onAction(ChatsAction.ItemCheckedToggled(item.id))
                        })
                    })

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }, floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onAction(ChatsAction.NewChatClicked)
                },
                icon = { Icon(Icons.Filled.Add, "Start New") },
                text = {
                    Text(
                        text = "Start New",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
            )
        })
}
