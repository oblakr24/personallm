package feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

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

actual class SharedImage {
    actual fun toByteArray(): ByteArray? {
        TODO("Not yet implemented")
    }

    actual fun toImageBitmap(): ImageBitmap? {
        TODO("Not yet implemented")
    }
}