package data

import util.capitalized

interface IModel {
    val value: String
}

sealed interface Models {
    enum class OpenAI(override val value: String): IModel {
        V3("gpt-3.5-turbo"),
        V4("gpt-4.0-turbo"),
        V4_O("gpt-4o");
    }

    enum class Anthropic(override val value: String): IModel {
        CLAUDE_3_HAIKU("claude-3-haiku-20240307")
    }

}
fun IModel.displayName() = value.split("_").joinToString(separator = " ") { it.capitalized() }
