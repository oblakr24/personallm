package feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import feature.sharedimage.ImageLocation
import feature.sharedimage.SharedImage
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraCaptureMode
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import util.randomUUID

// CameraManager.ios.kt
@Composable
actual fun rememberCameraManager(onResult: (SharedImage?) -> Unit): CameraManager {
    val imagePicker = UIImagePickerController()
    val cameraDelegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image =
                    didFinishPickingMediaWithInfo.getValue(UIImagePickerControllerEditedImage) as? UIImage
                        ?: didFinishPickingMediaWithInfo.getValue(
                            UIImagePickerControllerOriginalImage
                        ) as? UIImage

                if (image != null) {
                    val imageUrl = saveImage(image)
                    val loc: ImageLocation = ImageLocation.StoredUri(imageUrl, fullyResolved = false)
                    onResult.invoke(SharedImage(image, loc))
                    picker.dismissViewControllerAnimated(true, null)
                }
            }
        }
    }
    return remember {
        CameraManager {
            // TODO: This works on phones but not on Mac
            imagePicker.setSourceType(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)
            imagePicker.setAllowsEditing(true)
            imagePicker.setCameraCaptureMode(UIImagePickerControllerCameraCaptureMode.UIImagePickerControllerCameraCaptureModePhoto)
            imagePicker.setDelegate(cameraDelegate)
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                imagePicker, true, null
            )
        }
    }
}

private fun saveImage(image: UIImage): String {
    val documentsDirectory = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory, NSUserDomainMask, true
    ).firstOrNull()

    val fileName = "captured_${randomUUID()}.jpg"
    val filePath = "$documentsDirectory/$fileName"
    val fileURL = NSURL.fileURLWithPath(filePath)

    val imageData = UIImageJPEGRepresentation(image, 0.8)

    imageData?.writeToFile(filePath, atomically = true)
    return fileName
}

actual class CameraManager actual constructor(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}
