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
        TODO("Not yet implemented")
    }

    override fun isPermissionGranted(permission: PermissionType): Boolean {
        TODO("Not yet implemented")
    }

    override fun launchSettings() {
        TODO("Not yet implemented")
    }
}
