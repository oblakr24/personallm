package feature.commonui

import AppTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun InputBar(
    input: String,
    focusToInput: Boolean = false,
    sendEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onSend: (String) -> Unit,
    onChange: (String) -> Unit,
) {
    val bgColor = MaterialTheme.colorScheme.primaryContainer
    val focusRequester = remember { FocusRequester() }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(50.dp, 200.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.background(bgColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = input,
                onValueChange = onChange,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = bgColor,
                    unfocusedContainerColor = bgColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = "Enter text",
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                textStyle = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .weight(1f)
                    .background(bgColor)
                    .focusRequester(focusRequester)
            )

            if (input.isNotBlank() && sendEnabled) {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onSend(input)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
    LaunchedEffect(key1 = focusToInput) {
        if (focusToInput) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
}

@Preview
@Composable
fun InputBarPreview() {
    AppTheme {
        InputBar(input = "New SMS to be sent", onChange = {}, onSend = {

        })
    }
}

@Preview
@Composable
fun InputBarLongPreview() {
    AppTheme {
        InputBar(
            input = "New SMS to be sent and more text to fit into multiple lines",
            onChange = {},
            onSend = {

            })
    }
}