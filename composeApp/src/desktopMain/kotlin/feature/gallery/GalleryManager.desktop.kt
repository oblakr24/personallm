package feature.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import feature.camera.SharedImage

actual class GalleryManager actual constructor(onLaunch: () -> Unit) {
    actual fun launch() {
    }
}

@Composable
actual fun rememberGalleryManager(onResult: (SharedImage?) -> Unit): GalleryManager {
    return remember {
        GalleryManager {

        }
    }
}