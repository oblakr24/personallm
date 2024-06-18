package data

import data.openai.OpenAIAPIWrapper
import di.Singleton
import me.tatarka.inject.annotations.Inject

@Singleton
@Inject
class CompletionsApiImpl(
    private val openAIAPIWrapper: OpenAIAPIWrapper,
): CompletionsApi by openAIAPIWrapper {
}