package feature.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import feature.sharedimage.ImageLocation
import feature.sharedimage.SharedImage
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

// GalleryManager.ios.kt
@Composable
actual fun rememberGalleryManager(onResult: (SharedImage?) -> Unit): GalleryManager {
    val imagePicker = UIImagePickerController()
    val galleryDelegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo.getValue(
                    UIImagePickerControllerEditedImage
                ) as? UIImage ?: didFinishPickingMediaWithInfo.getValue(
                    UIImagePickerControllerOriginalImage
                ) as? UIImage

                val imageUrl = didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL
                val uri = imageUrl?.path.orEmpty()

                val loc = ImageLocation.TempUri(uri, uri)
                onResult.invoke(SharedImage(image, loc))
                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    return remember {
        GalleryManager {
            imagePicker.setSourceType(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)
            imagePicker.setAllowsEditing(true)
            imagePicker.setDelegate(galleryDelegate)
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                imagePicker, true, null
            )
        }
    }
}

actual class GalleryManager actual constructor(private val onLaunch: () -> Unit) {
    actual fun launch() {
        onLaunch()
    }
}