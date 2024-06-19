package data

import data.openai.OpenAIChatCompletionsRequestBody
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
    val role: String,
    val content: List<MessageItem>,
) {

    data class MessageItem(
        val type: String = TYPE_TEXT,
        val text: String? = null,
        val image_url: ImageUrl? = null,
//        val image_url: String? = null,
    ) {

        data class ImageUrl(
            val url: String,
            val detail: String = "low",
        )

        companion object {
            const val TYPE_TEXT = "text"

            const val ROLE_SYSTEM = "system"
            const val ROLE_ASSISTANT = "assistant"
        }
    }
}
