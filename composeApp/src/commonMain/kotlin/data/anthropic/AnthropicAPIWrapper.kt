package data.anthropic

import data.CompletionsApi
import data.CompletionsUtils
import data.Message
import data.Model
import data.NetworkError
import data.NetworkResp
import data.NetworkResponse
import data.SecretsProvider
import data.WrappedCompletionResponse
import data.anthropic.AnthropicAPI.Companion.BEARER_TOKEN_PREFIX
import data.anthropic.AnthropicMessage.Companion.ROLE_USER
import data.parseToResponse
import data.toStreamingFlow
import di.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import util.OpResult

@Singleton
@Inject
class AnthropicAPIWrapper(
    private val api: AnthropicAPI,
    private val json: Json,
    private val secretsProvider: SecretsProvider,
): CompletionsApi {

    override suspend fun getChatCompletions(
        prompt: String,
        imageEncoded: String?,
        prevMessages: List<Message>,
        model: Model,
    ): Flow<NetworkResponse<WrappedCompletionResponse>> {
        val newMessage = imageEncoded?.let {
            createImageMessage(it, prompt)
        } ?: AnthropicMessage(
            role = ROLE_USER,
            content = listOf(
                AnthropicMessage.Content.Text(
                    text = prompt,
                )
            ),
        )
        val prevMessagesMapped = prevMessages.map { m ->
            m.map()
        }
        val body = AnthropicChatCompletionsRequestBody(
            model = model.value,
            messages = prevMessagesMapped + newMessage,
            stream = true,
        )
        return streamCompletions(body)
    }

    override suspend fun getChatSummary(
        prevMessages: List<Message>,
        model: Model,
    ): NetworkResponse<String> {
        val body = AnthropicChatCompletionsRequestBody(
            model = model.value,
            messages = prevMessages.map { it.map() } + listOf(
                AnthropicMessage(
                    role = ROLE_USER,
                    content = listOf(
                        AnthropicMessage.Content.Text(
                            text = CompletionsUtils.Prompts.SUMMARIZE,
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
            it.content.firstOrNull()?.text.orEmpty()
        }
    }

    // TODO: Finalize mapping
    private fun Message.map() = AnthropicMessage(
        role = role,
        content = content.map { item ->
            if (item.image_url != null) {
                AnthropicMessage.Content.Image(
                    source = AnthropicMessage.ImageSource(
                        type = "TODO",
                        media_type = "TODO",
                        data = item.image_url.url
                    )
                )
            } else {
                AnthropicMessage.Content.Text(
                    text = item.text.orEmpty(),
                )
            }
        }
    )

    override suspend fun getImageCompletions(
        prompt: String,
        imageEncoded: String,
        model: Model,
    ): Flow<NetworkResponse<WrappedCompletionResponse>> {
        val body = AnthropicChatCompletionsRequestBody(
            model = model.value,
            messages = listOf(
                createImageMessage(imageEncoded, prompt)
            ),
            stream = true,
        )
        return streamCompletions(body)
    }

    private fun createImageMessage(prompt: String, imageEncoded: String) =
        AnthropicMessage(
            role = "user",
            content = listOf(
                AnthropicMessage.Content.Text(
                    text = prompt,
                ),
                AnthropicMessage.Content.Image(
                    type = "TODO",
                    source = AnthropicMessage.ImageSource(
                        media_type = "TODO",
                        type = "TODO",
                        data = "data:image/jpeg;base64,$imageEncoded",
                    ),
                ),
            ),
        )

    private fun bearerTokenHeader() = BEARER_TOKEN_PREFIX + " ${secretsProvider.anthropicApiKey()}"

    private suspend fun streamCompletions(body: AnthropicChatCompletionsRequestBody): Flow<NetworkResponse<WrappedCompletionResponse>> =
        api.getChatCompletionsStreaming(
            bearerToken = bearerTokenHeader(),
            body = body,
        ).toStreamingFlow().parseToResponse().map { resp ->
            resp.map {
                val message = it.content.firstOrNull()?.text ?: ""
                AnthropicWrappedCompletionResponse(message, it)
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

    private fun Flow<NetworkResponse<String>>.parseToResponse(): Flow<NetworkResponse<AntropicChatCompletionResponse>> = parseToResponse(
        mapper = { jsonString ->
            if (jsonString.contains("data:")) {
                val trimmed = jsonString.substringAfter("data: ")
                val decoded = json.decodeFromString<AntropicChatCompletionResponse>(trimmed)
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
        val response: AntropicChatCompletionResponse? = null,
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