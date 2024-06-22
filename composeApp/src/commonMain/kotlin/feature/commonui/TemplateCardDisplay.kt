package feature.commonui

import AppTheme
import alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TemplateCardDisplay(
    data: TemplateDisplayData,
    onClick: (TemplateDisplayData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth()
            .wrapContentHeight().padding(bottom = 12.dp, top = 0.dp, start = 24.dp, end = 24.dp)
            .clickable { onClick(data) },
        shape = CardDefaults.outlinedShape,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.alpha(0.4f)
        )
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                .padding(8.dp),
        ) {
            val (title, subtitle, date) = createRefs()

            Text(
                modifier = Modifier.constrainAs(title) {
                    start.linkTo(parent.start, 12.dp)
                    top.linkTo(parent.top)
                    end.linkTo(date.start, 4.dp)
                    width = Dimension.fillToConstraints
                }, text = data.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier.constrainAs(subtitle) {
                    start.linkTo(parent.start, 12.dp)
                    top.linkTo(title.bottom)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                text = data.subtitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 3,
                minLines = 3,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                modifier = Modifier.constrainAs(date) {
                    top.linkTo(parent.top, 4.dp)
                    end.linkTo(parent.end)
                },
                text = data.date,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }

}

@Preview
@Composable
fun TemplateCardDisplayPreview() {
    AppTheme {
        TemplateCardDisplay(
            data = TemplateDisplayData(
                id = "id1",
                title = AnnotatedString("Conv title"),
                subtitle = AnnotatedString("conv subtitle long message to make it really long and fit more than one line"),
                date = "13th Mar 2022 19:45:44",
                checked = null,
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
fun TemplateCardDisplayLongTitlePreview() {
    AppTheme {
        TemplateCardDisplay(
            data = TemplateDisplayData(
                id = "id1",
                title = AnnotatedString("Conv title - Very Long name"),
                subtitle = AnnotatedString("conv subtitle long message to make it really long and fit more than one line"),
                date = "13th Mar 2022 19:45:44",
                checked = null,
            ),
            onClick = {},
        )
    }
}
