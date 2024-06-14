package feature.chat

import alpha
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.BrowseGallery
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.outlined.BrowseGallery
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoAlbum
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import feature.camera.rememberCameraManager
import feature.commonui.GenericDialog
import feature.commonui.GenericDropdownMenu
import feature.commonui.GenericDropdownMenuItem
import feature.commonui.InputBar
import feature.commonui.MessageDisplay
import feature.commonui.MessageDisplayData
import feature.commonui.PrimaryTextButton
import feature.commonui.TemplateCardDisplay
import feature.commonui.TemplateDisplayData
import feature.commonui.TitledScaffold
import feature.commonui.rememberGenericDialogState
import feature.commonui.rememberGenericDropdownState
import feature.commonui.verticalScrollbar
import feature.gallery.rememberGalleryManager
import feature.image.ImageAction
import feature.permissions.PermissionType
import feature.permissions.rememberSuspendPermissionManager
import feature.permissions.requestGranted
import kotlinx.coroutines.launch

data class ChatContentUIState(
    val title: String = "New Chat",
    val messages: List<MessageDisplayData> = emptyList(),
    val models: List<ModelDisplay> = emptyList(),
    val templates: List<TemplateDisplayData> = emptyList(),
    val showTemplatesCarousel: Boolean = false,
    val inEditState: Boolean,
    val sendEnabled: Boolean,
    val attachedImage: ImageBitmap?,
    val inputExpanded: Boolean,
    val selectedTemplateId: String? = null,
    val focusToInput: Boolean = false,
    val selectedModel: String = "",
) {
    data class ModelDisplay(val value: String, val name: String, val selected: Boolean)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatContent(
    state: ChatContentUIState,
    text: String, onAction: (ChatAction) -> Unit, onBackClicked: () -> Unit
) {
    val modelSelectionDialogState = rememberGenericDialogState()
    val templateSelectionDialogState = rememberGenericDialogState()
    val keyboardController = LocalSoftwareKeyboardController.current
    GenericDialog(modelSelectionDialogState, content = {
        ModelSelectionDialog(state.models, onSelected = {
            onAction(ChatAction.ModelSelected(it))
            modelSelectionDialogState.close()
        })
    })
    GenericDialog(templateSelectionDialogState, content = {
        TemplateSelectionDialog(state.templates, state.selectedTemplateId, onSelected = {
            onAction(ChatAction.TemplateSelected(it))
            templateSelectionDialogState.close()
        })
    })
    TitledScaffold(onBackClicked = onBackClicked,
        titleContent = {
            Column(
                modifier = Modifier.wrapContentWidth().clickable {
                    modelSelectionDialogState.open()
                    keyboardController?.hide()
                }.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.title, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        state.selectedModel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.alpha(0.8f)
                    )
                    Icon(
                        imageVector = Icons.Outlined.SwapVert,
                        contentDescription = "Icon",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.alpha(0.6f),
                    )
                }
            }
        }, actions = {
            IconButton(onClick = {
                if (state.templates.isEmpty()) {
                    onAction(ChatAction.ShowNoTemplatesMessage)
                } else {
                    templateSelectionDialogState.open()
                }
            }, content = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Icon",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.alpha(0.6f),
                )
            })

        }, content = {
            val lazyListState = rememberLazyListState()

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.verticalScrollbar(lazyListState)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(
                    count = state.messages.size,
                    key = { state.messages[it].id },
                    itemContent = { idx ->
                        val item = state.messages[idx]
                        MessageDisplay(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            data = item,
                            onEditClicked = {
                                onAction(ChatAction.EditClicked(item))
                            }, onDeleteClicked = {
                                onAction(ChatAction.DeleteClicked(item))
                            }
                        )
                    })
            }

            if (state.showTemplatesCarousel && state.templates.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { state.templates.size })
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.onBackground.alpha(0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Try a template:",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        PrimaryTextButton("Dismiss", small = true, onClick = {
                            onAction(ChatAction.DismissTemplates)
                        })
                    }

                    HorizontalPager(
                        state = pagerState, modifier = Modifier.fillMaxWidth()
                    ) { pageIdx ->
                        val templateData = state.templates[pageIdx]
                        TemplateCardDisplay(templateData, onClick = {
                            onAction(ChatAction.TemplateSelected(it))
                        }, modifier = Modifier)
                    }
                }
            }
            if (state.inEditState) {
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.onBackground.alpha(0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Icon",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.alpha(0.6f),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Editing message",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        PrimaryTextButton("Cancel", small = true, onClick = {
                            onAction(ChatAction.DismissEdit)
                        })
                    }
                }
            }
            if (state.inputExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.onBackground.alpha(0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(96.dp),
                        ) {
                            Box {
                                if (state.attachedImage != null) {
                                    Image(
                                        bitmap = state.attachedImage,
                                        contentDescription = "Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(onClick = {
                                        onAction(ChatAction.RemoveImage)
                                    }, modifier = Modifier.align(Alignment.TopEnd),
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                        content = {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            )
                                        })
                                } else {
                                    val permsMgr = rememberSuspendPermissionManager()
                                    val scope = rememberCoroutineScope()
                                    val cameraManager = rememberCameraManager {
                                        onAction(ChatAction.OnImageResultReceived(it))
                                    }
                                    val galleryManager = rememberGalleryManager {
                                        onAction(ChatAction.OnImageResultReceived(it))
                                    }

                                    val dropdownState = rememberGenericDropdownState()
                                    GenericDropdownMenu(dropdownState) {
                                        GenericDropdownMenuItem("Add from gallery", Icons.Outlined.PhotoAlbum) {
                                            scope.launch {
                                                permsMgr.requestGranted(PermissionType.GALLERY)
                                                galleryManager.launch()
                                            }
                                        }
                                        GenericDropdownMenuItem("Open camera", Icons.Outlined.Camera) {
                                            scope.launch {
                                                permsMgr.requestGranted(PermissionType.CAMERA)
                                                cameraManager.launch()
                                            }
                                        }
                                    }

                                    Image(
                                        modifier = Modifier.fillMaxSize().clickable {
                                            dropdownState.open()
                                        },
                                        imageVector = Icons.Filled.PhotoAlbum,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                                        contentDescription = "Image",
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        PrimaryTextButton("Close", small = true, onClick = {
                            onAction(ChatAction.DismissExpandedInput)
                        })
                    }
                }
            }
        }, footer = {
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    .background(MaterialTheme.colorScheme.primaryContainer.alpha(0.5f)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    onAction(ChatAction.OpenExpandedInput)
                }, content = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Open more",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.alpha(0.6f),
                    )
                })
                InputBar(
                    input = text,
                    focusToInput = state.focusToInput,
                    sendEnabled = state.sendEnabled,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        .padding(start = 0.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    onChange = {
                        onAction(ChatAction.TextChanged(it))
                    },
                    onSend = {
                        onAction(ChatAction.SendClicked)
                    })
            }
        })
}
