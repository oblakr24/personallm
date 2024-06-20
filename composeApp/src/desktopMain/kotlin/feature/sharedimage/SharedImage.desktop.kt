package feature.sharedimage

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual class SharedImage(private val bitmap: BufferedImage, actual val location: ImageLocation) {

    actual fun toByteArray(): ByteArray? {
        return try {
            ByteArrayOutputStream().use { baos ->
                ImageIO.write(bitmap, "png", baos)
                baos.toByteArray()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    actual suspend fun storeLocally(imageResolver: ImageResolver): ImageLocation.StoredUri {
        return imageResolver.store(location)
    }
}

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

    actual fun resolveUri(imageLocation: ImageLocation.StoredUri): String {
        return imageLocation.uri
    }
}
