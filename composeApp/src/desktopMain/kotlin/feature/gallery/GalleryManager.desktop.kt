package feature.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import feature.sharedimage.ImageLocation
import feature.sharedimage.SharedImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser

actual class GalleryManager actual constructor(private val onLaunch: () -> Unit) {
    actual fun launch() {
        onLaunch()
    }
}

@Composable
actual fun rememberGalleryManager(onResult: (SharedImage?) -> Unit): GalleryManager {
    return remember {
        GalleryManager(onLaunch = {
            val fileChooser = JFileChooser()
            fileChooser.isAcceptAllFileFilterUsed = false
            fileChooser.addChoosableFileFilter(javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"))
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val file: File = fileChooser.selectedFile
                val image: BufferedImage = ImageIO.read(file)
                val location = ImageLocation.TempUri(file.absolutePath, file.absolutePath)
                onResult.invoke(SharedImage(image, location))
            } else {
                onResult.invoke(null)
            }
        })
    }
}