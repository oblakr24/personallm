package feature.templates

sealed interface TemplatesAction {
    data class TemplateClicked(val id: String) : TemplatesAction
}
