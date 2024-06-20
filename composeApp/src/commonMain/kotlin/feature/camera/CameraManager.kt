package feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import feature.sharedimage.SharedImage

// CameraManager.kt
@Composable
expect fun rememberCameraManager(onResult: (SharedImage?) -> Unit): CameraManager

expect class CameraManager(
    onLaunch: () -> Unit
) {
    fun launch()
}
