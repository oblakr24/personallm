package feature.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.contentValuesOf
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

// PermissionsManager.android.kt
@Composable
actual fun createPermissionsManager(callback: PermissionCallback): PermissionsManager {
    val currentContext = LocalContext.current
    val permissionsManager = remember { mutableStateOf<PermissionsManager?>(null) }

    val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
            grantedMap.forEach { (perm, granted) ->
                val permission = when (perm) {
                    Manifest.permission.CAMERA -> PermissionType.CAMERA
                    Manifest.permission.MANAGE_MEDIA -> PermissionType.GALLERY
                    else -> return@rememberLauncherForActivityResult
                }
                callback.onPermissionStatus(
                    permission,
                    if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
                )
            }
        }



    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        permissionsManager.value?.recheckPermissions()
    }

    return remember {
        PermissionsManager(callback, currentContext, launcher, settingsLauncher).also {
            permissionsManager.value = it
        }
    }
}

internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

actual class PermissionsManager actual constructor(private val callback: PermissionCallback) :
    PermissionHandler {

    private lateinit var context: Context
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>

    constructor(
        callback: PermissionCallback,
        context: Context,
        permissionsLauncher: ActivityResultLauncher<Array<String>>,
        settingsLauncher: ActivityResultLauncher<Intent>
    ) : this(callback) {
        this.context = context
        this.permissionsLauncher = permissionsLauncher
        this.settingsLauncher = settingsLauncher
    }

    private val prefs by lazy {
        context.getSharedPreferences("prefs_denied_status", Context.MODE_PRIVATE)
    }

    private fun PermissionType.markDenied(denied: Boolean) {
        prefs.edit {
            putBoolean(name, denied)
        }
    }

    private fun PermissionType.alreadyDenied() = prefs.getBoolean(name, false)

    fun recheckPermissions() {
        PermissionType.entries.forEach {
            if (it.alreadyDenied()) {
                isPermissionGranted(it)
            }
        }
    }

    override fun askPermissionNormal(permission: PermissionType) {
        when (permission) {
            PermissionType.CAMERA -> {
                val camPermission = context.checkSelfPermission(Manifest.permission.CAMERA)
                if (camPermission != PERMISSION_GRANTED) {
                    if (permission.alreadyDenied()) {
                        launchSettings()
                        return
                    }
                    permission.markDenied(true)

                    val activity = context.findActivity()
                    val shouldShowRationale =
                        activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    if (shouldShowRationale) {
                        callback.onPermissionStatus(permission, PermissionStatus.SHOW_RATIONALE)
                    } else {
                        permissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    }

                } else {
                    callback.onPermissionStatus(
                        permission, PermissionStatus.GRANTED
                    )
                }
            }

            PermissionType.GALLERY -> { // GetContent API does not require any runtime permissions
                callback.onPermissionStatus(
                    permission, PermissionStatus.GRANTED
                )
            }
        }
    }

    override fun isPermissionGranted(permission: PermissionType): Boolean {
        return when (permission) {
            PermissionType.CAMERA -> {
                val granted =
                    context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    permission.markDenied(false)
                }
                granted
            }

            PermissionType.GALLERY -> { // GetContent API does not require any runtime permissions
                true
            }
        }
    }

    override fun launchSettings() {
        settingsLauncher.launch(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
        )
    }
}