package feature.commonui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun rememberGenericDropdownState(
    isOpenInitially: Boolean = false,
): GenericDropdownState {
    val isOpen =
        remember { mutableStateOf(isOpenInitially) }

    return remember {
        GenericDropdownState(
            isOpen = isOpen,
            close = {
                isOpen.value = false
            }
        )
    }
}

data class GenericDropdownState(
    val isOpen: MutableState<Boolean>,
    val close: () -> Unit
) {
    fun open() {
        isOpen.value = true
    }
}

@Composable
fun GenericDropdownMenu(
    state: GenericDropdownState,
    content: @Composable ColumnScope.() -> Unit
) {
    if (state.isOpen.value) {
        DropdownMenu(
            expanded = state.isOpen.value,
            onDismissRequest = { state.close() },
            offset = DpOffset(x = 0.dp, y = 0.dp),
            modifier = Modifier.padding(top = 5.dp, start = 8.dp, end = 8.dp, bottom = 2.dp),
            content = content
        )
    }
}