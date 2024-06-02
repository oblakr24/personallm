package feature.landing

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.commonui.NavigationButton
import feature.commonui.TitledScaffold
import feature.commonui.calendar.CustomCalendarBottomSheet
import feature.commonui.rememberGenericBottomSheetState
import feature.commonui.verticalScrollbar
import kotlinx.coroutines.launch

data class LandingContentUIState(
    val test: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingContent(state: LandingContentUIState, onAction: (LandingAction) -> Unit) {
    val lazyListState = rememberLazyListState()
    val state = rememberGenericBottomSheetState()
    CustomCalendarBottomSheet(state)

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.verticalScrollbar(lazyListState)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            NavigationButton("New Chat") {
                onAction(LandingAction.OpenChat)
            }
        }

        item {
            NavigationButton("Prompt Test") {
                onAction(LandingAction.OpenHome)
            }
        }

        item {
            NavigationButton("Add Image") {
                onAction(LandingAction.OpenImage)
            }
        }

        item {
            NavigationButton("Listing Test") {
                onAction(LandingAction.OpenListing)
            }
        }

        item {
            NavigationButton("Open Date Picker") {
                state.open()

            }
        }

        item {
            NavigationButton("History") {

            }
        }

        item {
            NavigationButton("Prompts") {

            }
        }

        items(60, key = { it.toString() }, itemContent = { idx ->
            NavigationButton("Item $idx") {

            }
        })
    }
}
