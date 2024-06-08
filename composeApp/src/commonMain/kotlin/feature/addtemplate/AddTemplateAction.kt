package feature.addtemplate

sealed interface AddTemplateAction {
    data class OnTitleChanged(val new: String) : AddTemplateAction
    data class OnPromptChanged(val new: String) : AddTemplateAction
    data object SaveClicked : AddTemplateAction
}
