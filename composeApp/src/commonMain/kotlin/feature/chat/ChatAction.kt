package feature.chat

sealed interface ChatAction {
    data object SendClicked : ChatAction
    data class TextChanged(val new: String) : ChatAction
    data class ModelSelected(val display: ChatContentUIState.ModelDisplay): ChatAction
}
