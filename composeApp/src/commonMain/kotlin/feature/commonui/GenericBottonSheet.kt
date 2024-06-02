package feature.commonui

import AppColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberGenericBottomSheetState(
    isOpenInitially: Boolean = false,
    modalSheetState: SheetState = rememberModalSheetState(
        initialValue = if (isOpenInitially) SheetValue.Expanded else SheetValue.Hidden,
        skipPartiallyExpanded = true
    )
): GenericBottomSheetState {
    val coroutineScope = rememberCoroutineScope()
    val isOpen =
        remember { mutableStateOf(isOpenInitially) }

    return remember {
        GenericBottomSheetState(
            isOpen = isOpen,
            sheetState = modalSheetState,
            close = {
                coroutineScope.launch {
                    modalSheetState.hide()
                    isOpen.value = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberActionsBottomSheetState(density: Density = LocalDensity.current,) = rememberGenericBottomSheetState(modalSheetState = remember {
    SheetState(
        skipPartiallyExpanded = false,
        initialValue = SheetValue.Hidden,
        density = density,
        confirmValueChange = { true },
        skipHiddenState = false
    )
})

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberModalSheetState(
    initialValue: SheetValue,
    skipPartiallyExpanded: Boolean,
    density: Density = LocalDensity.current,
) = remember {
    SheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        initialValue = initialValue,
        density = density,
        confirmValueChange = { true },
        skipHiddenState = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
data class GenericBottomSheetState(
    val isOpen: MutableState<Boolean>,
    val sheetState: SheetState,
    val close: () -> Unit
) {
    fun open() {
        isOpen.value = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericBottomSheet(
    bottomSheetState: GenericBottomSheetState,
    contentColor: Color = AppColors.OffWhiteBG,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
    footer: @Composable BoxScope.() -> Unit,
) {
    if (bottomSheetState.isOpen.value) {
        val insets = BottomSheetDefaults.windowInsets
        ModalBottomSheet(
            dragHandle = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    BottomSheetDefaults.DragHandle()
                    header?.invoke(this)
                }
            },
            contentColor = contentColor,
            scrimColor = AppColors.BlackAlpha20,
            containerColor = AppColors.OffWhiteBG,
            windowInsets = insets.union(WindowInsets(top = 0.dp, bottom = 48.dp)),
            onDismissRequest = {
                bottomSheetState.close()
            },
            modifier = Modifier,
            sheetState = bottomSheetState.sheetState,
            tonalElevation = 12.dp,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            content = {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxHeight(0.85f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        content()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        footer()
                    }
                }
            }
        )
    }
}
