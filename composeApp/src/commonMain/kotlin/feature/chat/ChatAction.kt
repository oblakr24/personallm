package feature.chat

import feature.commonui.TemplateDisplayData

sealed interface ChatAction {
    data object SendClicked : ChatAction
    data class TextChanged(val new: String) : ChatAction
    data class ModelSelected(val display: ChatContentUIState.ModelDisplay): ChatAction
    data class TemplateSelected(val display: TemplateDisplayData): ChatAction
}
