package feature.chats

sealed interface ChatsAction {
    data class ChatClicked(val id: String) : ChatsAction
    data object NewChatClicked : ChatsAction
}
