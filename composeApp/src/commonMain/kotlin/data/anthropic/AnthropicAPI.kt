package data.anthropic

import data.NetworkError
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Streaming
import io.ktor.client.statement.HttpStatement
import kotlinx.serialization.Serializable
import util.OpResult



interface AnthropicAPI {

    @Headers("Content-Type: application/json")
    @POST("v1/messages")
    suspend fun getChatCompletions(
        @Header("Authorization") bearerToken: String,
        @Body body: AnthropicChatCompletionsRequestBody
    ): OpResult<AntropicChatCompletionResponse, NetworkError>

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    @Streaming
    suspend fun getChatCompletionsStreaming(
        @Header("Authorization") bearerToken: String,
        @Body body: AnthropicChatCompletionsRequestBody
    ): HttpStatement

    companion object {

        const val BEARER_TOKEN_PREFIX = "Bearer"
    }
}

data class AnthropicWrappedCompletionResponse(
    val message: String,
    val response: AntropicChatCompletionResponse,
)

@Serializable
data class AnthropicChatCompletionsRequestBody(
    val model: String,
    val max_tokens: Int = 1000,
    val messages: List<AnthropicMessage>,
    val stream: Boolean,
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: List<Content>
) {
    @Serializable
    sealed class Content {
        @Serializable
        data class Image(val type: String = TYPE_IMAGE, val source: ImageSource) : Content()

        @Serializable
        data class Text(val type: String = TYPE_TEXT, val text: String) : Content()
    }

    @Serializable
    data class ImageSource(
        val type: String,
        val media_type: String,
        val data: String
    )

    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image_url"

        const val ROLE_SYSTEM = "system"
        const val ROLE_ASSISTANT = "assistant"
        const val ROLE_USER = "user"
    }
}

@Serializable
data class AntropicChatCompletionResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<Choice>,
    val stop_reason: String?,
) {

    fun done() = stop_reason != null

    @Serializable
    data class Choice(
        val type: String,
        val text: String?,
    )
}
