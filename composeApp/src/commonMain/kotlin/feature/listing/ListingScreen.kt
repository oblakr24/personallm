package feature.listing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState


@Composable
fun ListingScreen(component: ListingComponent) {
    val state = component.state.collectAsState().value
    val text = component.text.collectAsState().value
    ListingContent(state, text, onAction = component::onAction, onBackClicked = {
        component.navigateUp()
    })
}