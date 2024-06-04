package feature.commonui

import AppTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.jetbrains.compose.ui.tooling.preview.Preview

data class DialogData(
    val title: String,
    val subtitle: String,
    val confirmText: String = "OK",
    val dismissText: String? = "Dismiss",
)

@Composable
fun <T: Any?>rememberGenericAlertDialogState(onConfirm: ((T) -> Unit)? = null) = remember {
    GenericAlertDialogState<T>(
        onConfirm = onConfirm,
        displayedData = mutableStateOf(null),
    )
}

data class GenericAlertDialogState<T: Any?>(
    val pendingPayload: MutableState<T?> = mutableStateOf<T?>(null),
    val onConfirm: ((T) -> Unit)?,
    val displayedData: MutableState<DialogData?> = mutableStateOf(null),
    val onDismissRequest: () -> Unit = {
        displayedData.value = null
    },
) {

    fun open(data: DialogData, payload: T) {
        displayedData.value = data
        pendingPayload.value = payload
    }

    fun close() {
        displayedData.value = null
        pendingPayload.value = null
    }

    val confirmPayload : () -> Unit = {
        val payload = pendingPayload.value
        payload?.let {
            onConfirm?.invoke(it)
        }
    }
}

@Composable
fun <T: Any?>GenericAlertDialog(
    state: GenericAlertDialogState<T>,
) {
    val displayedValue = state.displayedData.value
    if (displayedValue != null) {
        AppAlertDialog(onDismissRequest = state.onDismissRequest, onConfirm = state.confirmPayload, data = displayedValue)
    }
}

@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    data: DialogData,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = data.title)
        },
        text = {
            Text(text = data.subtitle)
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                onConfirm?.invoke()
            }) {
                Text(text = data.confirmText)
            }
        },
        dismissButton = data.dismissText?.let {
            {
                TextButton(onClick = onDismissRequest) {
                    Text(text = it)
                }
            }
        }
    )
}

@Preview
@Composable
fun GenericAlertDialogPreview() {
    AppTheme {
        val data = DialogData(
            title = "Title",
            subtitle = "Subtitle",
        )
        AppAlertDialog(onDismissRequest = {}, data = data)
    }
}