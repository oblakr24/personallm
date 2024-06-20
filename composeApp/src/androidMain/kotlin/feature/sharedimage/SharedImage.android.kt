package feature.sharedimage

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import feature.camera.ComposeFileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URI


actual class SharedImage(private val bitmap: android.graphics.Bitmap?, loc: ImageLocation) {

    actual fun toByteArray(): ByteArray? {
        return if (bitmap != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            @Suppress("MagicNumber") bitmap.compress(
                android.graphics.Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream
            )
            byteArrayOutputStream.toByteArray()
        } else {
            println("toByteArray returned null")
            null
        }
    }

    actual fun toImageBitmap(): ImageBitmap? {
        val byteArray = toByteArray()
        return if (byteArray != null) {
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
        } else {
            println("toByteArray returned null")
            null
        }
    }

    actual val location: ImageLocation = loc

    actual suspend fun storeLocally(imageResolver: ImageResolver): ImageLocation.StoredUri {
        return when (location) {
            is ImageLocation.StoredUri -> location
            is ImageLocation.TempUri -> {
                imageResolver.store(imageLocation = location)
            }
        }
    }
}

actual class ImageResolver(private val appContext: Context) {

    actual suspend fun store(imageLocation: ImageLocation): ImageLocation.StoredUri {

        return when (imageLocation) {
            is ImageLocation.StoredUri -> imageLocation
            is ImageLocation.TempUri -> {
                withContext(Dispatchers.IO) {
                    val tempFile = File(URI.create(imageLocation.originalUri))
                    val imageName = tempFile.name
                    val persistentFile = File(appContext.filesDir, imageName)
                    try {
                        FileInputStream(tempFile).use { inputStream ->
                            FileOutputStream(persistentFile).use { outputStream ->
                                val buffer = ByteArray(1024)
                                var length: Int
                                while (inputStream.read(buffer).also { length = it } > 0) {
                                    outputStream.write(buffer, 0, length)
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                    val sharableUri = ComposeFileProvider.getSharableStoredUri(persistentFile, appContext)
                    ImageLocation.StoredUri(uri = sharableUri?.toString() ?: "")
                }
            }
        }
    }
}