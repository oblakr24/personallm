package feature.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import feature.commonui.DialogData
import feature.commonui.GenericAlertDialog
import feature.commonui.rememberGenericAlertDialogState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

// PermissionsManager.kt
expect class PermissionsManager(callback: PermissionCallback) : PermissionHandler

interface PermissionCallback {
    fun onPermissionStatus(permissionType: PermissionType, status: PermissionStatus)
}

@Composable
expect fun createPermissionsManager(callback: PermissionCallback): PermissionsManager

interface PermissionHandler {
    fun askPermissionNormal(permission: PermissionType)

    fun isPermissionGranted(permission: PermissionType): Boolean

    fun launchSettings()
}

class SuspendPermissionManager(
    val handler: PermissionHandler,
    val updates: MutableStateFlow<Map<PermissionType, PermissionStatus?>>
)

@Composable
fun rememberSuspendPermissionManager(): SuspendPermissionManager {
    val state = remember { mutableStateOf<SuspendPermissionManager?>(null) }
    val dialogState = rememberGenericAlertDialogState<PermissionType?>(onConfirm = { type ->
        if (type != null) {
            state.value?.handler?.askPermissionNormal(type)
        }
    })

    val (updates, callback) = remember {
        val updates = MutableStateFlow<Map<PermissionType, PermissionStatus?>>(emptyMap())
        updates to object : PermissionCallback {
            override fun onPermissionStatus(
                permissionType: PermissionType,
                status: PermissionStatus
            ) {
                if (status == PermissionStatus.SHOW_RATIONALE) {
                    dialogState.open(DialogData("Permission needed for ${permissionType.name.lowercase()}", "To enable this feature, please grant the permission"), permissionType)
                }
                if (status == PermissionStatus.DENIED) {
                    dialogState.open(DialogData("Permission needed for ${permissionType.name.lowercase()}", "To enable this feature, please grant the permission"), permissionType)
                }
                updates.update {
                    it.toMutableMap().apply {
                        this[permissionType] = status
                    }.toMap()
                }
            }
        }
    }


    val handler = createPermissionsManager(callback)
    GenericAlertDialog(dialogState)

    return remember {
        SuspendPermissionManager(handler, updates).also {
            state.value = it
        }
    }
}

suspend fun SuspendPermissionManager.request(type: PermissionType): PermissionStatus {
    updates.update {
        it.toMutableMap().apply {
            this[type] = null
        }.toMap()
    }

    handler.askPermissionNormal(type)
    val result = updates.mapNotNull { it[type] }.first()
    return result
}

suspend fun SuspendPermissionManager.requestGranted(type: PermissionType) {
    updates.update {
        it.toMutableMap().apply {
            this[type] = null
        }.toMap()
    }

    handler.askPermissionNormal(type)
    updates.mapNotNull { it[type] }.filter { it == PermissionStatus.GRANTED }.first()
}

enum class PermissionType {
    CAMERA, GALLERY
}

enum class PermissionStatus {
    GRANTED, DENIED, SHOW_RATIONALE
}