package data

import com.rokoblak.personallm.config.AppBuildConfig
import me.tatarka.inject.annotations.Inject

@Inject
class SecretsProvider {

    fun openAIApiKey(): String {
        return AppBuildConfig.OPENAI_API_KEY
    }

    fun anthropicApiKey(): String {
        return AppBuildConfig.ANTHROPIC_API_KEY
    }
}
