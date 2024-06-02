package feature.listing

sealed interface ListingAction {
    data class OnTextChanged(val new: String): ListingAction
    data object OnAddItemClicked: ListingAction
}