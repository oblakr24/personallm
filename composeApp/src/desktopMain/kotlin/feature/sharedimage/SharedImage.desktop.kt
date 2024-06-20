package feature.sharedimage

import androidx.compose.ui.graphics.ImageBitmap

actual class SharedImage {

    actual fun toByteArray(): ByteArray? {
        return TODO()
    }

    actual fun toImageBitmap(): ImageBitmap? {
        return TODO()
    }

    actual val location: ImageLocation by lazy {
        TODO()
    }

    actual suspend fun storeLocally(imageResolver: ImageResolver): ImageLocation.StoredUri {
        return TODO()
    }
}

actual class ImageResolver {
    actual suspend fun store(imageLocation: ImageLocation): ImageLocation.StoredUri {
        return TODO()
    }
}
