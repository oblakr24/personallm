package feature.commonui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun GenericDropdownMenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(text = {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text, modifier = Modifier.weight(1f))
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }, onClick = onClick)
}
