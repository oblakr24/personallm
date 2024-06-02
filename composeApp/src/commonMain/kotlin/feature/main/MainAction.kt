package feature.main

sealed interface MainAction {
    data object FAQClicked : MainAction
    data class SetDarkMode(val enabled: Boolean) : MainAction
    data object OpenRepoUrl : MainAction
}