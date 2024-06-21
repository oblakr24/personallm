package feature.commonui

import alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AllOut
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

data class ExtendedToolbarState(
    val showEdit: Boolean,
    val deleteEnabled: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtendedToolbar(
    state: ExtendedToolbarState,
    onEditClicked: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onClose: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.alpha(0.6f),
        ),
        title = { },
        actions = {
            val moreOptions = rememberGenericDropdownState()

            GenericDropdownMenu(moreOptions) {
                GenericDropdownMenuItem("Select all", Icons.Outlined.AllOut) {
                    onSelectAll()
                    moreOptions.close()
                }
                GenericDropdownMenuItem("Clear selection", Icons.Outlined.ClearAll) {
                    onClearSelection()
                    moreOptions.close()
                }
            }

            IconButton(
                onClick = {
                    moreOptions.open()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Show more options"
                )
            }

            val deleteDialogState = rememberGenericAlertDialogState<Unit>(onConfirm = {
                onDeleteConfirmed()
            })
            GenericAlertDialog(deleteDialogState)
            IconButton(enabled = state.deleteEnabled,
                onClick = {
                    deleteDialogState.open(
                        DialogData(
                            title = "Delete items",
                            subtitle = "Are you sure?",
                        ), Unit
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete selected"
                )
            }

            if (state.showEdit.not()) {
                IconButton(
                    onClick = {
                        onEditClicked()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Enter Edit mode"
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        onEditClicked()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditOff,
                        contentDescription = "Close Edit mode"
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                onClose()
            }) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    )
}
