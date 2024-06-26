package data.openai

import data.NetworkError
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Streaming
import io.ktor.client.statement.HttpStatement
import kotlinx.serialization.Serializable
import util.OpResult


interface OpenAIAPI {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun getChatCompletions(
        @Header("Authorization") bearerToken: String,
        @Body body: OpenAIChatCompletionsRequestBody
    ): OpResult<OpenAIChatCompletionResponse, NetworkError>

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    @Streaming
    suspend fun getChatCompletionsStreaming(
        @Header("Authorization") bearerToken: String,
        @Body body: OpenAIChatCompletionsRequestBody
    ): HttpStatement

    companion object {

        const val BEARER_TOKEN_PREFIX = "Bearer"
    }
}

data class OpenAIWrappedCompletionResponse(
    val message: String,
    val response: OpenAIChatCompletionResponse,
)

@Serializable
data class OpenAIChatCompletionResponse(
    val id: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage?,
) {

    fun done() = choices.any { it.finish_reason != null }

    @Serializable
    data class Choice(
        val index: Int,
        val message: Message?,
        val delta: Message?,
        val logprobs: String?,
        val finish_reason: String?
    ) {
        @Serializable
        data class Message(
            val role: String?,
            val content: String?
        )
    }

    @Serializable
    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
}

@Serializable
data class OpenAIChatCompletionsRequestBody(
    val messages: List<Message>,
    val model: String,
    val stream: Boolean,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 1000,
) {
    @Serializable
    data class Message(
        val role: String,
        val content: List<MessageItem>,
    )

    @Serializable
    data class MessageItem(
        val type: String,
        val text: String? = null,
        val image_url: ImageUrl? = null,
//        val image_url: String? = null,
    ) {

        @Serializable
        data class ImageUrl(
            val url: String,
            val detail: String = "low",
        )
    }
}
