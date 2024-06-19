package data.openai

import data.CompletionsApi
import data.CompletionsUtils
import data.Message
import data.Model
import data.NetworkError
import data.NetworkResp
import data.NetworkResponse
import data.SecretsProvider
import data.WrappedCompletionResponse
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
class OpenAIAPIWrapper(
    private val api: OpenAIAPI,
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
        } ?: OpenAIChatCompletionsRequestBody.Message(
            role = ROLE_USER,
            content = listOf(
                OpenAIChatCompletionsRequestBody.MessageItem(
                    type = TYPE_TEXT,
                    text = prompt,
                )
            ),
        )
        val prevMessagesMapped = prevMessages.map { m ->
            m.map()
        }
        val body = OpenAIChatCompletionsRequestBody(
            model = if (imageEncoded != null) Model.OpenAI.V4_O.value else model.value,
            messages = prevMessagesMapped + newMessage,
            stream = true,
        )
        return streamCompletions(body)
    }

    private fun Message.map() = OpenAIChatCompletionsRequestBody.Message(
        role = when (role) {
            Message.Role.SYSTEM -> ROLE_SYSTEM
            Message.Role.ASSISTANT -> ROLE_ASSISTANT
            Message.Role.USER -> ROLE_USER
        },
        content = content.map { item ->
            OpenAIChatCompletionsRequestBody.MessageItem(
                type = if (item.image != null) TYPE_IMAGE_URL else TYPE_TEXT,
                text = item.text,
                image_url = item.image?.let {
                    OpenAIChatCompletionsRequestBody.MessageItem.ImageUrl(
                        url = createEncodedImageString(it.base64EncodedImage),
                        detail = "low",
                    )
                },
            )
        }
    )

    override suspend fun getChatSummary(
        prevMessages: List<Message>,
        model: Model,
    ): NetworkResponse<String> {
        val body = OpenAIChatCompletionsRequestBody(
            model = model.value,
            messages = prevMessages.map {
                it.map()
            } + listOf(
                OpenAIChatCompletionsRequestBody.Message(
                    role = ROLE_USER,
                    content = listOf(
                        OpenAIChatCompletionsRequestBody.MessageItem(
                            type = TYPE_TEXT,
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
            it.choices.firstOrNull()?.message?.content.orEmpty()
        }
    }

    override suspend fun getImageCompletions(
        prompt: String,
        imageEncoded: String,
        model: Model,
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
                    type = TYPE_TEXT,
                    text = prompt,
                ),
                OpenAIChatCompletionsRequestBody.MessageItem(
                    type = TYPE_IMAGE_URL,
                    image_url = OpenAIChatCompletionsRequestBody.MessageItem.ImageUrl(
                        url = createEncodedImageString(imageEncoded)
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

    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE_URL = "image_url"

        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
        const val ROLE_SYSTEM = "system"

        private fun createEncodedImageString(encodedImage: String) = "data:image/jpeg;base64,$encodedImage"
    }
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