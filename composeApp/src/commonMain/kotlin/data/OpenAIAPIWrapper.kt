package data

import data.ChatCompletionsRequestBody.MessageItem.Companion.ROLE_USER
import di.Singleton
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.runningFold
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import util.OpResult

@Singleton
@Inject
class OpenAIAPIWrapper(
    private val api: OpenAIAPI,
    private val json: Json,
    private val secretsProvider: SecretsProvider,
) {

    enum class Model(val value: String) {
        V3("gpt-3.5-turbo"),
        V4("gpt-4.0-turbo"),
        V4_VISION_PREVIEW("gpt-4-vision-preview")
    }

    suspend fun getChatCompletions(
        prompt: String,
        prevMessages: List<ChatCompletionsRequestBody.Message> = emptyList()
    ): Flow<NetworkResponse<WrappedCompletionResponse>> {
        val body = ChatCompletionsRequestBody(
            messages = prevMessages + listOf(
                ChatCompletionsRequestBody.Message(
                    role = ROLE_USER,
                    content = listOf(
                        ChatCompletionsRequestBody.MessageItem(
                            text = prompt,
                        )
                    ),
                )
            ),
            stream = true,
        )
        return streamCompletions(body)
    }

    suspend fun getImageCompletions(
        prompt: String,
        imageEncoded: String
    ): Flow<NetworkResponse<WrappedCompletionResponse>> {
        val body = ChatCompletionsRequestBody(
            model = Model.V4_VISION_PREVIEW.value,
            messages = listOf(
                ChatCompletionsRequestBody.Message(
                    role = "user",
                    content = listOf(
                        ChatCompletionsRequestBody.MessageItem(
                            text = prompt,
                        ),
                        ChatCompletionsRequestBody.MessageItem(
                            type = ChatCompletionsRequestBody.MessageItem.TYPE_IMAGE_URL,
                            image_url = "data:image/jpeg;base64,$imageEncoded",
                        ),
                    ),
                )
            ),
            stream = true,
        )
        return streamCompletions(body)
    }

    private suspend fun streamCompletions(body: ChatCompletionsRequestBody): Flow<NetworkResponse<WrappedCompletionResponse>> =
        api.getChatCompletionsStreaming(
            bearerToken = OpenAIAPI.BEARER_TOKEN_PREFIX + " ${secretsProvider.openAIApiKey()}",
            body = body,
        ).toStreamingFlow().parseToResponse().map { resp ->
            resp.map {
                val message = it.choices.firstOrNull()?.delta?.content ?: ""
                WrappedCompletionResponse(message, it)
            }
        }.runningFold(initial = NetworkResp.success(WrappedCompletionResponseInner())) { acc, new ->
            val combined = acc.optValue()?.message + (new.optValue()?.message ?: "")
            new.map {
                WrappedCompletionResponseInner(combined, it.response)
            }
        }.filter {
            when (it) {
                is OpResult.Done -> it.data.message.isNotBlank()
                is OpResult.Error -> true
            }
        }.map {
            it.map { innerResp ->
                WrappedCompletionResponse(innerResp.message, innerResp.response!!)
            }
        }

    private fun Flow<NetworkResponse<String>>.parseToResponse(): Flow<NetworkResponse<ChatCompletionResponse>> {
        return this.mapNotNull {
            it.flatMap { jsonString ->
                try {
                    val trimmed = jsonString.substringAfter("data: ")
                    val decoded = json.decodeFromString<ChatCompletionResponse>(trimmed)
                    NetworkResp.success(decoded)
                } catch (e: Throwable) {
                    println("Could not parse: $jsonString because of ${e.message}")
                    NetworkResp.error(NetworkError.Error(e))
                }
            }
        }
    }

    private fun HttpStatement.toStreamingFlow(): Flow<NetworkResponse<String>> = flow {
        execute { httpResponse ->
            val channel = httpResponse.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line()
                if (!line.isNullOrBlank()) {
                    if (line.contains("[DONE]")) {
                        println("Done!")
                    } else {
                        emit(NetworkResp.success(line))
                    }
                } else if (line == null) {
                    NetworkResp.error(NetworkError.NoData)
                } else {
                    println("Blank line")
                }
            }
        }
    }

    data class WrappedCompletionResponseInner(
        val message: String = "",
        val response: ChatCompletionResponse? = null,
    )
}