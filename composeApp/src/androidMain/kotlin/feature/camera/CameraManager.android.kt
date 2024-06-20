package feature.camera

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.rokoblak.personallm.R
import feature.sharedimage.ImageLocation
import feature.sharedimage.SharedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URI

// CameraManager.android.kt
@Composable
actual fun rememberCameraManager(onResult: (SharedImage?) -> Unit): CameraManager {
    val context = LocalContext.current
    val contentResolver: ContentResolver = context.contentResolver
    var tempPhotoUriSharable by remember { mutableStateOf<String?>(value = null) }
    var tempPhotoUriOriginal by remember { mutableStateOf<String?>(value = null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val bitmapUri = tempPhotoUriSharable?.toUri() ?: return@rememberLauncherForActivityResult
                val bitmap = BitmapUtils.getBitmapFromUri(bitmapUri, contentResolver)
                val location = ImageLocation.TempUri(sharableUri = tempPhotoUriSharable.orEmpty(), originalUri = tempPhotoUriOriginal.orEmpty())
                val image = SharedImage(bitmap, location)
                onResult.invoke(image)
            }
        }
    )
    return remember {
        CameraManager(
            onLaunch = {
                val location = ComposeFileProvider.getTempImageUri(context)
                tempPhotoUriOriginal = location.originalUri
                tempPhotoUriSharable = location.sharableUri
                cameraLauncher.launch(tempPhotoUriSharable!!.toUri())
            }
        )
    }
}

actual class CameraManager actual constructor(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

class ComposeFileProvider : FileProvider(R.xml.provider_paths) {
    companion object {
        fun getTempImageUri(context: Context): ImageLocation.TempUri {
            val tempFile = File.createTempFile(
                "picture_${System.currentTimeMillis()}", ".jpg", context.cacheDir
            ).apply {
                createNewFile()
            }
            val authority = context.applicationContext.packageName + ".provider"
            val sharableUri = getUriForFile(
                context,
                authority,
                tempFile,
            )
            return ImageLocation.TempUri(sharableUri = sharableUri.toString(), originalUri = tempFile.toUri().toString())
        }

        fun getSharableStoredUri(file: File, context: Context): Uri? {
            val authority = context.applicationContext.packageName + ".provider"
            val sharableUri = getUriForFile(
                context,
                authority,
                file,
            )
            return sharableUri
        }
    }
}
