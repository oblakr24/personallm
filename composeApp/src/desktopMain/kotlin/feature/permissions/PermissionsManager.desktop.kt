package feature.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun createPermissionsManager(callback: PermissionCallback): PermissionsManager {
    return remember { PermissionsManager(callback) }
}

actual class PermissionsManager actual constructor(private val callback: PermissionCallback) :
    PermissionHandler {

    override fun askPermissionNormal(permission: PermissionType) {
        callback.onPermissionStatus(permission, PermissionStatus.GRANTED)
    }

    override fun isPermissionGranted(permission: PermissionType): Boolean {
        return true
    }

    override fun launchSettings() {

    }
}
