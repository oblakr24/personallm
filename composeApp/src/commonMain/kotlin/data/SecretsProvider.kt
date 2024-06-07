package data

import com.rokoblak.personallm.config.SecretConfig
import me.tatarka.inject.annotations.Inject

@Inject
class SecretsProvider {

    fun openAIApiKey(): String {
        return SecretConfig.OPENAI_API_KEY
    }
}
