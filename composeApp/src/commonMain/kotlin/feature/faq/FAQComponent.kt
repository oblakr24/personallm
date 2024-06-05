package feature.faq

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import di.VMContext
import di.vmScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import navigation.RouteNavigator
import org.jetbrains.compose.resources.getStringArray
import personallm.composeapp.generated.resources.Res
import personallm.composeapp.generated.resources.faq_items
import kotlin.coroutines.CoroutineContext

@Inject
class FAQComponent(
    private val mainContext: CoroutineContext,
    private val nav: RouteNavigator,
    @Assisted private val vmContext: VMContext,
) : VMContext by vmContext, RouteNavigator by nav {

    private val scope = vmScope(mainContext)

    private val items = flow {
        emit(getStringArray(Res.array.faq_items))
    }

    private val expandedIndices = MutableStateFlow(emptyMap<Int, Boolean>())

    val uiState: StateFlow<FAQScreenUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.Immediate) {
            FAQPresenter(items, expandedIndices)
        }
    }

    fun handleAction(act: FAQAction) {
        when (act) {
            is FAQAction.ItemExpandedCollapsed -> expandCollapseItem(act.idx)
        }
    }

    @Composable
    private fun FAQPresenter(
        itemsFlow: Flow<List<String>>,
        expandedIndicesFlow: StateFlow<Map<Int, Boolean>>,
    ): FAQScreenUIState {
        val items = itemsFlow.collectAsState(initial = emptyList()).value
        val expandedIndices = expandedIndicesFlow.collectAsState().value

        val mappedItems = items.chunked(2).withIndex().flatMap { (idx, titleAndSub) ->
            val expanded = expandedIndices[idx] == true
            val title = FAQScreenUIState.Item.Title(
                id = idx.toString(),
                idx = idx,
                titleAndSub.first(),
                expanded = expanded
            )
            if (expanded) {
                listOf(
                    title,
                    FAQScreenUIState.Item.Subtitle("sub_$idx", subtitle = titleAndSub.last())
                )
            } else {
                listOf(title)
            }
        }

        return FAQScreenUIState(
            mappedItems
        )
    }

    private fun expandCollapseItem(idx: Int) {
        expandedIndices.update {
            it.toMutableMap().apply {
                val current = it[idx] ?: false
                put(idx, !current)
            }.toMap()
        }
    }
}