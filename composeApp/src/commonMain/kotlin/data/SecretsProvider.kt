package data

import me.tatarka.inject.annotations.Inject

@Inject
class SecretsProvider {

    fun openAIApiKey(): String {
        return "TODO!!"
    }
}
