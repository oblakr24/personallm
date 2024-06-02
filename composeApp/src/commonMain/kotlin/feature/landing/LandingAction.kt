package feature.landing

sealed interface LandingAction {
    data object OpenHome : LandingAction
    data object OpenImage : LandingAction
    data object OpenListing : LandingAction
    data object OpenChat : LandingAction
}
