package feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import feature.sharedimage.SharedImage

actual class CameraManager actual constructor(onLaunch: () -> Unit) {
    actual fun launch() {
    }
}

@Composable
actual fun rememberCameraManager(onResult: (SharedImage?) -> Unit): CameraManager {
    return remember {
        CameraManager {
            
        }
    }
}
