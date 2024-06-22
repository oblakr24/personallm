package feature.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.chats.ChatsAction
import feature.commonui.ExtendedToolbar
import feature.commonui.ExtendedToolbarState
import feature.commonui.TemplateDisplay
import feature.commonui.TemplateDisplayData
import feature.commonui.TitledScaffold
import feature.commonui.verticalScrollbar

data class TemplatesContentUIState(
    val templates: List<TemplateDisplayData>,
    val extendedToolbarState: ExtendedToolbarState?,
)

@Composable
fun TemplatesContent(
    state: TemplatesContentUIState,
    onAction: (TemplatesAction) -> Unit,
) {
    TitledScaffold(
        leadingIcon = if (state.templates.isNotEmpty() && state.extendedToolbarState == null) {
            {
                IconButton(onClick = {
                    onAction(TemplatesAction.ExtendedSettingsToggled)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Edit toggle",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        } else null,
        title = "Templates",
        content = {
            Column {
                if (state.extendedToolbarState != null) {
                    ExtendedToolbar(
                        state = state.extendedToolbarState,
                        onEditClicked = {
                            onAction(TemplatesAction.EditToggled)
                        },
                        onClearSelection = {
                            onAction(TemplatesAction.ClearSelection)
                        },
                        onSelectAll = {
                            onAction(TemplatesAction.SelectAll)
                        },
                        onDeleteConfirmed = {
                            onAction(TemplatesAction.DeleteConfirmed)
                        },
                        onClose = {
                            onAction(TemplatesAction.ExtendedSettingsToggled)
                        }
                    )
                }

                val lazyListState = rememberLazyListState()
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize().verticalScrollbar(lazyListState)
                ) {
                    items(
                        count = state.templates.size,
                        key = { state.templates[it].id },
                        itemContent = { idx ->
                            val item = state.templates[idx]
                            TemplateDisplay(modifier = Modifier.fillMaxWidth().clickable {
                                onAction(TemplatesAction.TemplateClicked(id = item.id))
                            }, data = item, onCheckedChanged = {
                                onAction(TemplatesAction.ItemCheckedToggled(item.id))
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
                onAction(TemplatesAction.AddNewClicked)
            },
            icon = { Icon(Icons.Filled.Add, "Add new") },
            text = { Text(text = "Add New", color = MaterialTheme.colorScheme.onPrimaryContainer) },
        )
    })
}
