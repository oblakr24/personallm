package feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

// CameraManager.kt
@Composable
expect fun rememberCameraManager(onResult: (SharedImage?) -> Unit): CameraManager

expect class CameraManager(
    onLaunch: () -> Unit
) {
    fun launch()
}

expect class SharedImage {
    fun toByteArray(): ByteArray?
    fun toImageBitmap(): ImageBitmap?
}