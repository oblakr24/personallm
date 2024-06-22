package feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import feature.commonui.TemplateDisplay
import feature.commonui.TemplateDisplayData

@Composable
fun TemplateSelectionDialog(displays: List<TemplateDisplayData>, selectedId: String?, onSelected: (TemplateDisplayData) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(vertical = 8.dp)) {
        item {
            Text("Selected Template", modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center)
        }

        items(count = displays.size, key = { displays[it].id }, itemContent = { idx ->

            val display = displays[idx]

            Row(modifier = Modifier.fillMaxWidth().clickable {
                onSelected(display)
            }, verticalAlignment = Alignment.CenterVertically) {

                TemplateDisplay(modifier = Modifier.weight(1f), background = Color.Transparent, data = display, onCheckedChanged = {})

                Spacer(Modifier.width(2.dp))
                if (display.id == selectedId) {
                    Icon(
                        imageVector = Icons.Sharp.Check,
                        contentDescription = "Icon",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    Spacer(Modifier.width(24.dp))
                }
                Spacer(Modifier.width(16.dp))
            }
        })
    }
}
