package feature.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrowseGallery
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import feature.camera.rememberCameraManager
import feature.commonui.ButtonWithIcon
import feature.commonui.SecondaryButton
import feature.commonui.TitledScaffold
import feature.commonui.verticalScrollbar
import feature.gallery.rememberGalleryManager
import feature.permissions.PermissionStatus
import feature.permissions.PermissionType
import feature.permissions.rememberSuspendPermissionManager
import feature.permissions.request
import feature.permissions.requestGranted
import kotlinx.coroutines.launch

data class ImageContentUIState(
    val bitmap: ImageBitmap? = null,
    val completion: String? = null
)

@Composable
fun ImageContent(state: ImageContentUIState, onAction: (ImageAction) -> Unit, onBackClicked: () -> Unit) {
    TitledScaffold("Add Image", onBackClicked = onBackClicked, content = {
        val lazyListState = rememberLazyListState()

        val permsMgr = rememberSuspendPermissionManager()
        val scope = rememberCoroutineScope()
        val cameraManager = rememberCameraManager {
            onAction(ImageAction.OnImageResultReceived(it))
        }
        val galleryManager = rememberGalleryManager {
            onAction(ImageAction.OnImageResultReceived(it))
        }

        LazyColumn(state = lazyListState, modifier = Modifier.verticalScrollbar(lazyListState)) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                ButtonWithIcon("Add from Gallery", icon = Icons.Default.ImageSearch, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    scope.launch {
                        permsMgr.requestGranted(PermissionType.GALLERY)
                        galleryManager.launch()
                    }
                }
            }

            item {
                ButtonWithIcon("Add from Camera", icon = Icons.Default.Camera, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    scope.launch {
                        permsMgr.requestGranted(PermissionType.CAMERA)
                        cameraManager.launch()
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    if (state.bitmap != null) {
                        Image(
                            bitmap = state.bitmap,
                            contentDescription = "Image",
                            modifier = Modifier.size(200.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            modifier = Modifier.size(200.dp).clip(CircleShape),
                            imageVector = Icons.Default.Image,
                            contentDescription = "Image",
                        )
                    }
                }
            }

            if (state.completion != null) {
                item {
                    Text(text = state.completion, modifier = Modifier.padding(12.dp))
                }
            }
        }
    })
}
