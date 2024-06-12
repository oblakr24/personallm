package feature.chat

import feature.commonui.MessageDisplayData
import feature.commonui.TemplateDisplayData

sealed interface ChatAction {
    data object SendClicked : ChatAction
    data class TextChanged(val new: String) : ChatAction
    data class ModelSelected(val display: ChatContentUIState.ModelDisplay) : ChatAction
    data class TemplateSelected(val display: TemplateDisplayData) : ChatAction
    data object ShowNoTemplatesMessage : ChatAction
    data object DismissTemplates : ChatAction
    data object DismissEdit : ChatAction
    data class EditClicked(val display: MessageDisplayData) : ChatAction
    data class DeleteClicked(val display: MessageDisplayData) : ChatAction
}
