package data.openai

import data.CompletionsApi
import data.IModel
import data.Message
import data.Models
import data.NetworkError
import data.NetworkResp
import data.NetworkResponse
import data.SecretsProvider
import data.WrappedCompletionResponse
import data.openai.OpenAIChatCompletionsRequestBody.MessageItem.Companion.ROLE_USER
import data.parseToResponse
import data.toStreamingFlow
import di.Singleton
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.runningFold
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import util.OpResult

@Singleton
@Inject
class OpenAIAPIWrapper(
    private val api: OpenAIAPI,
    private val json: Json,
    private val secretsProvider: SecretsProvider,
): CompletionsApi {

    override suspend fun getChatCompletions(
        prompt: String,
        imageEncoded: String?,
        prevMessages: List<Message>,
        model: IModel,
    ): Flow<NetworkResponse<WrappedCompletionResponse>> {
        val newMessage = imageEncoded?.let {
            createImageMessage(it, prompt)
        } ?: OpenAIChatCompletionsRequestBody.Message(
            role = ROLE_USER,
            content = listOf(
                OpenAIChatCompletionsRequestBody.MessageItem(
                    text = prompt,
                )
            ),
        )
        val prevMessagesMapped = prevMessages.map { m ->
            OpenAIChatCompletionsRequestBody.Message(
                role = m.role,
                content = m.content.map { item ->
                    OpenAIChatCompletionsRequestBody.MessageItem(
                        type = item.type,
                        text = item.text,
                        image_url = item.image_url?.let {
                            OpenAIChatCompletionsRequestBody.MessageItem.ImageUrl(
                                url = it.url,
                                detail = it.detail,
                            )
                        },
                    )
                }
            )
        }
        val body = OpenAIChatCompletionsRequestBody(
            model = if (imageEncoded != null) Models.OpenAI.V4_O.value else model.value,
            messages = prevMessagesMapped + newMessage,
            stream = true,
        )
        return streamCompletions(body)
    }

    override suspend fun getChatSummary(
        prevMessages: List<OpenAIChatCompletionsRequestBody.Message>,
        model: IModel,
    ): NetworkResponse<String> {
        val body = OpenAIChatCompletionsRequestBody(
            model = model.value,
            messages = prevMessages + listOf(
                OpenAIChatCompletionsRequestBody.Message(
                    role = ROLE_USER,
                    content = listOf(
                        OpenAIChatCompletionsRequestBody.MessageItem(
                            text = "Summarize this conversations in 2-5 words maximum. Only include this summary in your response. Do not include any other messages.",
                        )
                    ),
                )
            ),
            stream = false,
        )
        return api.getChatCompletions(
            bearerToken = bearerTokenHeader(),
            body = body,
        ).map {
            it.choices.firstOrNull()?.message?.content.orEmpty()
        }
    }

    override suspend fun getImageCompletions(
        prompt: String,
        imageEncoded: String,
        model: IModel,
    ): Flow<NetworkResponse<WrappedCompletionResponse>> {
        val body = OpenAIChatCompletionsRequestBody(
            model = model.value,
            messages = listOf(
                createImageMessage(imageEncoded, prompt)
            ),
            stream = true,
        )
        return streamCompletions(body)
    }

    private fun createImageMessage(prompt: String, imageEncoded: String) =
        OpenAIChatCompletionsRequestBody.Message(
            role = "user",
            content = listOf(
                OpenAIChatCompletionsRequestBody.MessageItem(
                    text = prompt,
                ),
                OpenAIChatCompletionsRequestBody.MessageItem(
                    type = OpenAIChatCompletionsRequestBody.MessageItem.TYPE_IMAGE_URL,
                    image_url = OpenAIChatCompletionsRequestBody.MessageItem.ImageUrl(
                        url = "data:image/jpeg;base64,$imageEncoded"
                    )
                ),
            ),
        )

    private fun bearerTokenHeader() = OpenAIAPI.BEARER_TOKEN_PREFIX + " ${secretsProvider.openAIApiKey()}"

    private suspend fun streamCompletions(body: OpenAIChatCompletionsRequestBody): Flow<NetworkResponse<WrappedCompletionResponse>> =
        api.getChatCompletionsStreaming(
            bearerToken = bearerTokenHeader(),
            body = body,
        ).toStreamingFlow().parseToResponse().map { resp ->
            resp.map {
                val message = it.choices.firstOrNull()?.delta?.content ?: ""
                OpenAIWrappedCompletionResponse(message, it)
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
                WrappedCompletionResponse(innerResp.message, done = innerResp.response!!.done(), id = innerResp.response.id)
            }
        }

    private fun Flow<NetworkResponse<String>>.parseToResponse(): Flow<NetworkResponse<OpenAIChatCompletionResponse>> = parseToResponse(
        mapper = { jsonString ->
            if (jsonString.contains("data:")) {
                val trimmed = jsonString.substringAfter("data: ")
                val decoded = json.decodeFromString<OpenAIChatCompletionResponse>(trimmed)
                decoded
            } else {
                null
            }
        },
        errorMapper = { body ->
            val error = json.decodeFromString<ApiErrors>(body)
            NetworkError.NotSuccessful(
                body = error.error?.message ?: "Error", code = 400
            )
        }
    )

    data class WrappedCompletionResponseInner(
        val message: String = "",
        val response: OpenAIChatCompletionResponse? = null,
    )
}

@Serializable
private data class ApiErrors(
    val error: ApiErrorResponse?
)

@Serializable
private data class ApiErrorResponse(
    val message: String,
    val type: String?,
    val param: String?,
    val code: String?,
)