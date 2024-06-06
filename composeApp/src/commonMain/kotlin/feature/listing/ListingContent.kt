package feature.listing

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.commonui.ButtonWithIcon
import feature.commonui.TitledScaffold
import feature.commonui.verticalScrollbar


data class ListingContentUIState(
    val text: String?,
    val items: List<Item>,
) {
    data class Item(
        val id: String,
        val text: String
    )
}

@Composable
fun ListingContent(
    state: ListingContentUIState,
    text: String,
    onAction: (ListingAction) -> Unit,
    onBackClicked: () -> Unit
) {
    TitledScaffold("Listing", onBackClicked = onBackClicked, content = {
        val lazyListState = rememberLazyListState()
        LazyColumn(state = lazyListState, modifier = Modifier.verticalScrollbar(lazyListState)) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                TextField(value = text,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    onValueChange = {
                        onAction(ListingAction.OnTextChanged(it))
                    })
            }

            item {
                ButtonWithIcon(
                    "Add New Item",
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    onAction(ListingAction.OnAddItemClicked)
                }
            }

            items(
                count = state.items.size,
                key = {
                    state.items[it].id
                },
                itemContent = { idx ->
                    val item = state.items[idx]
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.text)
                    }
                }
            )

        }
    })

}