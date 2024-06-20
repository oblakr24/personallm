package feature.sharedimage

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import org.jetbrains.skia.Image
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

actual class ImageResolver {

    actual suspend fun store(imageLocation: ImageLocation): ImageLocation.StoredUri {
        return when (imageLocation) {
            is ImageLocation.StoredUri -> imageLocation
            is ImageLocation.TempUri -> {
                ImageLocation.StoredUri(
                    uri = imageLocation.originalUri
                )
            }
        }
    }
}

actual class SharedImage(private val image: UIImage?, loc: ImageLocation) {
    @OptIn(ExperimentalForeignApi::class)
    actual fun toByteArray(): ByteArray? {
        return if (image != null) {
            val imageData = UIImageJPEGRepresentation(image, COMPRESSION_QUALITY)
                ?: throw IllegalArgumentException("image data is null")
            val bytes = imageData.bytes ?: throw IllegalArgumentException("image bytes is null")
            val length = imageData.length

            val data: CPointer<ByteVar> = bytes.reinterpret()
            ByteArray(length.toInt()) { index -> data[index] }
        } else {
            null
        }

    }

    actual fun toImageBitmap(): ImageBitmap? {
        val byteArray = toByteArray()
        return if (byteArray != null) {
            Image.makeFromEncoded(byteArray).toComposeImageBitmap()
        } else {
            null
        }
    }

    private companion object {
        const val COMPRESSION_QUALITY = 0.80
    }

    actual val location: ImageLocation = loc

    actual suspend fun storeLocally(imageResolver: ImageResolver): ImageLocation.StoredUri {
        return when (location) {
            is ImageLocation.StoredUri -> location
            is ImageLocation.TempUri -> {
                ImageLocation.StoredUri(
                    uri = location.originalUri
                )
            }
        }
    }
}