package feature.templates

sealed interface TemplatesAction {
    data class TemplateClicked(val id: String) : TemplatesAction
    data object AddNewClicked : TemplatesAction
    data object ExtendedSettingsToggled : TemplatesAction
    data object EditToggled : TemplatesAction
    data object DeleteConfirmed : TemplatesAction
    data object ClearSelection : TemplatesAction
    data object SelectAll : TemplatesAction
    data class ItemCheckedToggled(val id: String) : TemplatesAction
}
