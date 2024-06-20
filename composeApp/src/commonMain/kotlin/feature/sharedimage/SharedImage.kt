package feature.sharedimage

import androidx.compose.ui.graphics.ImageBitmap

expect class ImageResolver {
    suspend fun store(imageLocation: ImageLocation): ImageLocation.StoredUri

    fun resolveUri(imageLocation: ImageLocation.StoredUri): String
}

sealed interface ImageLocation {
    data class TempUri(val sharableUri: String, val originalUri: String): ImageLocation
    data class StoredUri(val uri: String, val fullyResolved: Boolean = true): ImageLocation
}

expect class SharedImage {

    val location: ImageLocation

    fun toByteArray(): ByteArray?
    fun toImageBitmap(): ImageBitmap?

    suspend fun storeLocally(imageResolver: ImageResolver): ImageLocation.StoredUri
}
