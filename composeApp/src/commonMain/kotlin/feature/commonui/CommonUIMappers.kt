package feature.commonui

import androidx.compose.ui.text.buildAnnotatedString
import data.repo.Template
import util.formattedReadable

object CommonUIMappers {

    fun Template.toDisplay(): TemplateDisplayData {
        val appendedUpdateAt = updatedAt?.let { " (updated ${it.formattedReadable()})" } ?: ""
        return TemplateDisplayData(
            id = id,
            title = buildAnnotatedString { append(title) },
            subtitle = buildAnnotatedString { append(prompt) },
            date = "Created ${createdAt.formattedReadable()}$appendedUpdateAt",
        )
    }
}
