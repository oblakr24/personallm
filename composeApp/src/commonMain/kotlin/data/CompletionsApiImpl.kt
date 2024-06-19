package data

import data.openai.OpenAIAPIWrapper
import di.Singleton
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Singleton
@Inject
class CompletionsApiImpl(
    private val openAIAPIWrapper: OpenAIAPIWrapper,
    private val anthropicAPIWrapper: OpenAIAPIWrapper,
) : CompletionsApi {

    override suspend fun getChatCompletions(
        prompt: String,
        imageEncoded: String?,
        prevMessages: List<Message>,
        model: Model
    ): Flow<NetworkResponse<WrappedCompletionResponse>> {
        return model.delegate().getChatCompletions(
            prompt = prompt,
            imageEncoded = imageEncoded,
            prevMessages = prevMessages,
            model = model
        )
    }

    override suspend fun getChatSummary(
        prevMessages: List<Message>,
        model: Model
    ): NetworkResponse<String> {
        return model.delegate().getChatSummary(prevMessages = prevMessages, model = model)
    }

    override suspend fun getImageCompletions(
        prompt: String,
        imageEncoded: String,
        model: Model
    ): Flow<NetworkResponse<WrappedCompletionResponse>> {
        return model.delegate().getImageCompletions(
            prompt = prompt,
            imageEncoded = imageEncoded,
            model = model
        )
    }

    private fun Model.delegate(): OpenAIAPIWrapper {
        return when (this) {
            is Model.Anthropic -> anthropicAPIWrapper
            is Model.OpenAI -> openAIAPIWrapper
        }
    }
}