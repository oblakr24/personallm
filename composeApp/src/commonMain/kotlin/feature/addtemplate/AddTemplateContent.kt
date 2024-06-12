package feature.addtemplate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.commonui.PrimaryButton
import feature.commonui.TitledScaffold

data class AddTemplateContentUIState(
    val new: Boolean = true,
    val saveEnabled: Boolean = false,
)

@Composable
fun AddTemplateContent(
    state: AddTemplateContentUIState,
    title: String,
    prompt: String,
    onAction: (AddTemplateAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    TitledScaffold(
        title = if (state.new) "Create Template" else "Edit Template",
        onBackClicked = onBackClicked,
        content = {
            Column {
                Spacer(Modifier.height(16.dp))
                Text("Title:", modifier = Modifier.padding(horizontal = 24.dp))
                TextField(value = title, onValueChange = { new ->
                    onAction(AddTemplateAction.OnTitleChanged(new))
                }, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                Spacer(Modifier.height(24.dp))
                Text("Prompt:", modifier = Modifier.padding(horizontal = 24.dp))
                TextField(value = prompt, onValueChange = { new ->
                    onAction(AddTemplateAction.OnPromptChanged(new))
                }, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
            }
        },
        footer = {
            PrimaryButton(
                text = if (state.new) "Create" else "Save",
                enabled = state.saveEnabled,
                modifier = Modifier.fillMaxWidth().padding(24.dp)
            ) {
                onAction(AddTemplateAction.SaveClicked)
            }
        })
}
