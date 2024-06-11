package feature.landing

import data.DarkModeState

sealed interface LandingAction {
    data object OpenHome : LandingAction
    data object OpenImage : LandingAction
    data object OpenListing : LandingAction
    data object OpenChat : LandingAction
    data object FAQClicked : LandingAction
    data class SetDarkMode(val new: DarkModeState) : LandingAction
    data object SetDarkModeFollowsSystem : LandingAction
    data object OpenRepoUrl : LandingAction
}
