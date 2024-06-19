package data

import util.capitalized

sealed interface Model {
    enum class OpenAI(override val value: String): Model {
        V3("gpt-3.5-turbo"),
        V4("gpt-4.0-turbo"),
        V4_O("gpt-4o");
    }

    enum class Anthropic(override val value: String): Model {
        CLAUDE_3_HAIKU("claude-3-haiku-20240307")
    }

    val value: String

    companion object {

        fun allEntries(): List<Model> = Model.OpenAI.entries + Model.Anthropic.entries

        fun default(): Model = OpenAI.V3
        fun defaultImage(): Model = OpenAI.V4_O
    }
}

fun Model.displayName() = value.split("_").joinToString(separator = " ") { it.capitalized() }
