package feature.chats

sealed interface ChatsAction {
    data class ChatClicked(val id: String) : ChatsAction
    data object SortAscDescToggled : ChatsAction
    data object NewChatClicked : ChatsAction
    data object ExtendedSettingsToggled : ChatsAction
    data object EditToggled : ChatsAction
    data object DeleteConfirmed : ChatsAction
    data object ClearSelection : ChatsAction
    data object SelectAll : ChatsAction
    data class ItemCheckedToggled(val id: String) : ChatsAction
}
