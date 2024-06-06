package feature.samplerequest

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.commonui.NavigationButton
import feature.commonui.TitledScaffold
import feature.commonui.verticalScrollbar

data class SampleRequestUIState(
    val response: String?,
    val loading: Boolean = false,
)

@Composable
fun SampleRequestContent(state: SampleRequestUIState, onAction: (SampleRequestAction) -> Unit, onBackClicked: () -> Unit) {
    TitledScaffold("Make a sample request", onBackClicked = onBackClicked, content = {
        val lazyListState = rememberLazyListState()

        LazyColumn(state = lazyListState, modifier = Modifier.verticalScrollbar(lazyListState)) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                NavigationButton("Make request") {
                    onAction(SampleRequestAction.ExecuteRequestClicked)
                }
            }

            if (state.loading) {
                item {
                    Text("Loading...", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                }
            }

            if (state.response != null) {
                item {
                    Text(state.response, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    })
}
