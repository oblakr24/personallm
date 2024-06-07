package feature.chat

import alpha
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModelSelectionDialog(displays: List<ChatContentUIState.ModelDisplay>, onSelected: (ChatContentUIState.ModelDisplay) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(vertical = 8.dp)) {
        item {
            Text("Selected Model", modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center)
        }

        items(count = displays.size, key = { displays[it].value }, itemContent = { idx ->
            val display = displays[idx]
            Row(modifier = Modifier.fillMaxWidth().clickable {
                onSelected(display)
            }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(display.name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.alpha(if (display.selected) 1.0f else 0.7f))
                if (display.selected) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Sharp.Check,
                        contentDescription = "Icon",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        })
    }
}
