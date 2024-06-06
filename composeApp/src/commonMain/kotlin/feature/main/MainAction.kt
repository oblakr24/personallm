package feature.main

import data.DarkModeState

sealed interface MainAction {
    data object FAQClicked : MainAction
    data class SetDarkMode(val new: DarkModeState) : MainAction
    data object SetDarkModeFollowsSystem : MainAction
    data object OpenRepoUrl : MainAction
}