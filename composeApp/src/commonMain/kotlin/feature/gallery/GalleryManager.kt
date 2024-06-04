package feature.gallery

import androidx.compose.runtime.Composable
import feature.camera.SharedImage

// GalleryManager.kt
@Composable
expect fun rememberGalleryManager(onResult: (SharedImage?) -> Unit): GalleryManager


expect class GalleryManager(
    onLaunch: () -> Unit
) {
    fun launch()
}