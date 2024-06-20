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
            system = null,
            content = listOf(
                AnthropicMessage.Content.Text(
                    type = TEXT_TYPE,
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
                    system = null,
                    content = listOf(
                        AnthropicMessage.Content.Text(
                            type = TEXT_TYPE,
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

    private fun Message.map() = AnthropicMessage(
        role = when (role) {
            Message.Role.SYSTEM -> ROLE_USER
            Message.Role.ASSISTANT -> ROLE_ASSISTANT
            Message.Role.USER -> ROLE_USER
        },
        system = if (role == Message.Role.SYSTEM) content.firstOrNull()?.text else null,
        // TODO: Skip content in case of a system prompt?
        content = content.map { item ->
            if (item.image != null) {
                AnthropicMessage.Content.Image(
                    type = IMAGE_TPYE,
                    source = AnthropicMessage.ImageSource(
                        type = IMAGE_TYPE_BASE64,
                        media_type = IMAGE_MEDIA_TYPE_JPEG,
                        data = createEncodedImageString(item.image.base64EncodedImage),
                    )
                )
            } else {
                AnthropicMessage.Content.Text(
                    type = TEXT_TYPE,
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
            role = ROLE_USER,
            system = null,
            content = listOf(
                AnthropicMessage.Content.Text(
                    type = TEXT_TYPE,
                    text = prompt,
                ),
                AnthropicMessage.Content.Image(
                    type = IMAGE_TPYE,
                    source = AnthropicMessage.ImageSource(
                        media_type = IMAGE_MEDIA_TYPE_JPEG,
                        type = IMAGE_TYPE_BASE64,
                        data = createEncodedImageString(imageEncoded),
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
            // TODO: Adjust parsing here
            if (jsonString.contains("data:")) {
                val trimmed = jsonString.substringAfter("data: ")
                val decoded = json.decodeFromString<AntropicChatCompletionResponse>(trimmed)
                decoded
            } else {
                null
            }
        },
        errorMapper = { body ->
            val errors = json.decodeFromString<ApiErrors>(body)
            val error = errors.error
            NetworkError.NotSuccessful(
                body = error?.message ?: "Error", code = null, type = error?.type, codeString = error?.code,
            )
        }
    )

    data class WrappedCompletionResponseInner(
        val message: String = "",
        val response: AntropicChatCompletionResponse? = null,
    )

    companion object {
        const val IMAGE_TPYE = "image"
        const val TEXT_TYPE = "text"
        const val IMAGE_TYPE_BASE64 = "base64"
        const val IMAGE_MEDIA_TYPE_JPEG = "image/jpeg"

        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"

        private fun createEncodedImageString(encodedImage: String) = "data:image/jpeg;base64,$encodedImage"
    }
}

@Serializable
private data class ApiErrors(
    val type: String?,
    val error: ApiErrorResponse?
)

@Serializable
private data class ApiErrorResponse(
    val message: String,
    val type: String?,
    val param: String?,
    val code: String?,
)