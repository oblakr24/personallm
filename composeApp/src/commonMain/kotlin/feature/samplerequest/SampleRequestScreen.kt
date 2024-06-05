package feature.samplerequest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun SampleRequestScreen(component: SampleRequestComponent) {
    val state = component.state.collectAsState().value
    SampleRequestContent(state, onAction = component::onAction, onBackClicked = {
        component.navigateUp()
    })
}
