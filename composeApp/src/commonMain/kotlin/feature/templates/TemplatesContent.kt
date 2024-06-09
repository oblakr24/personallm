package feature.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.commonui.TemplateDisplay
import feature.commonui.TemplateDisplayData
import feature.commonui.verticalScrollbar

data class TemplatesContentUIState(
    val templates: List<TemplateDisplayData> = emptyList(),
)

@Composable
fun TemplatesContent(
    state: TemplatesContentUIState,
    onAction: (TemplatesAction) -> Unit,
) {
    Scaffold(content = {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize().verticalScrollbar(lazyListState)
        ) {
            items(count = state.templates.size, key = { state.templates[it].id }, itemContent = { idx ->
                val item = state.templates[idx]
                TemplateDisplay(modifier = Modifier.fillMaxWidth().clickable {
                    onAction(TemplatesAction.TemplateClicked(id = item.id))
                }, data = item)
            })

            item {
                Spacer(modifier = Modifier.height(72.dp))
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
    }, floatingActionButtonPosition = FabPosition.End)
}
