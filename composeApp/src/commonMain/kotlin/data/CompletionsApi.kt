package data

import kotlinx.coroutines.flow.Flow

// TODO: Adjust interface and models to be more generic for other APIs
interface CompletionsApi {

    suspend fun getChatCompletions(
        prompt: String,
        imageEncoded: String?,
        prevMessages: List<Message> = emptyList(),
        model: Model,
    ): Flow<NetworkResponse<WrappedCompletionResponse>>

    suspend fun getChatSummary(
        prevMessages: List<Message>,
        model: Model,
    ): NetworkResponse<String>

    suspend fun getImageCompletions(
        prompt: String,
        imageEncoded: String,
        model: Model,
    ): Flow<NetworkResponse<WrappedCompletionResponse>>
}

data class WrappedCompletionResponse(
    val message: String,
    val done: Boolean,
    val id: String,
)

data class Message(
    val role: Role,
    val content: List<MessageItem>,
) {

    enum class Role {
        SYSTEM, ASSISTANT, USER,
    }

    data class MessageItem(
        val text: String? = null,
        val image: EncodedImage? = null,
    ) {

        data class EncodedImage(
            val base64EncodedImage: String,
        )
    }
}
