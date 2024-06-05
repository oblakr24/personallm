package feature.faq

sealed interface FAQAction {
    data class ItemExpandedCollapsed(val idx: Int): FAQAction
}
