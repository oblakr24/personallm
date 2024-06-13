package feature.commonui

import AppTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

data class MessageDisplayData(
    val id: String,
    val content: String,
    val date: String,
    val alignedLeft: Boolean,
    val avatar: AvatarData?,
    val imageUri: String?,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageDisplay(data: MessageDisplayData, onEditClicked: () -> Unit, onDeleteClicked: () -> Unit, modifier: Modifier = Modifier) {
    val alignment = if (data.alignedLeft) Alignment.Start else Alignment.End
    val backgroundColor = if (data.alignedLeft) Color.LightGray else Color.Blue
    val textColor = if (data.alignedLeft) Color.Black else Color.White

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(modifier = Modifier.wrapContentWidth()) {
            if (data.avatar != null) {
                AvatarBadge(
                    data = data.avatar,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(28.dp),
                    textStyle = MaterialTheme.typography.labelMedium
                )
            }
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                val dropdownState = rememberGenericDropdownState()

                GenericDropdownMenu(dropdownState) {
                    GenericDropdownMenuItem("Edit", Icons.Outlined.Edit) {
                        onEditClicked()
                    }
                    val deleteDialogState = rememberGenericAlertDialogState<Unit>(onConfirm = {
                        onDeleteClicked()
                    })
                    GenericAlertDialog(deleteDialogState)
                    GenericDropdownMenuItem("Delete", Icons.Outlined.Delete) {
                        deleteDialogState.open(DialogData(
                            title = "Delete message",
                            subtitle = "Are you sure?",
                        ), Unit)
                    }
                }

                Text(
                    modifier = Modifier
                        .combinedClickable(onLongClick = {
                            dropdownState.open()
                        }, onClick = {

                        })
                        .background(backgroundColor, RoundedCornerShape(12.dp))
                        .align(alignment)
                        .widthIn(min = 20.dp, max = 220.dp)
                        .padding(8.dp),
                    text = data.content,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                )
                if (data.imageUri != null) {
                    Surface(
                        modifier = modifier
                            .size(220.dp)
                            .padding(vertical = 8.dp)
                            .border(1.dp, MaterialTheme.colorScheme.background, CircleShape)
                        ,
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
//                            AsyncImage(model = Uri.parse(data.imageUri), contentDescription = null)
                        }
                    }
                }
                Text(
                    modifier = Modifier.padding(2.dp),
                    text = data.date,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview
@Composable
fun MessageDisplayOtherPreview() {
    AppTheme {
        MessageDisplay(
            modifier = Modifier,
            data = MessageDisplayData(
                id = "id1",
                content = "Content Looong Content Looooong Looong Looong",
                date = "Sun 14th Dec 2022, 13:44:55",
                alignedLeft = true,
                avatar = AvatarData.Initials("AB", Color.Blue),
                imageUri = null,
            ), onEditClicked = {}, onDeleteClicked = {},
        )
    }
}

@Preview
@Composable
fun MessageDisplayMinePreview() {
    AppTheme {
        MessageDisplay(
            modifier = Modifier,
            data = MessageDisplayData(
                id = "id1",
                content = "Content",
                date = "Sun 14th Dec 2022, 13:44:55",
                alignedLeft = false,
                avatar = null,
                imageUri = null,
            ), onEditClicked = {}, onDeleteClicked = {},
        )
    }
}
