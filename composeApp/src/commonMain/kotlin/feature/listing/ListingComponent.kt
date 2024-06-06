package feature.listing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import db.AppDatabase
import di.VMContext
import di.vmScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.RouteNavigator
import personallm.data.ListingItem
import kotlin.coroutines.CoroutineContext

@Inject
class ListingComponent(
    private val mainContext: CoroutineContext,
    private val database: AppDatabase,
    private val nav: RouteNavigator,
    @Assisted private val vmContext: VMContext,
) : VMContext by vmContext, RouteNavigator by nav {

    val text = MutableStateFlow("")

    private val scope = vmScope(mainContext)

    private val items = database.listingItems()

    val state: StateFlow<ListingContentUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            ListingPresenter(
                textFlow = text,
                itemsFlow = items,
            )
        }
    }

    @Composable
    private fun ListingPresenter(
        textFlow: StateFlow<String>,
        itemsFlow: Flow<List<ListingItem>>,
    ): ListingContentUIState {
        val text = textFlow.collectAsState().value
        val items = itemsFlow.collectAsState(emptyList()).value
        val mappedItems = items.map {
            ListingContentUIState.Item(
                id = it.id.toString(),
                text = it.text,
            )
        }
        return ListingContentUIState(
            text = text,
            items = mappedItems
        )
    }

    fun onAction(action: ListingAction) {
        when (action) {
            is ListingAction.OnTextChanged -> {
                text.update { action.new }
            }

            ListingAction.OnAddItemClicked -> {
                val itemText = text.value
                text.value = ""
                scope.launch {
                    database.insertListingItem(
                        itemText = itemText
                    )
                }
            }
        }
    }
}
