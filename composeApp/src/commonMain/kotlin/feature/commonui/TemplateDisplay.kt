package feature.commonui

import AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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

data class TemplateDisplayData(
    val id: String,
    val title: AnnotatedString,
    val subtitle: AnnotatedString,
    val date: String,
)

@Composable
fun TemplateDisplay(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.background,
    data: TemplateDisplayData,
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(background)
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

@Preview
@Composable
fun TemplateDisplayPreview() {
    AppTheme {
        TemplateDisplay(
            data = TemplateDisplayData(
                id = "id1",
                title = AnnotatedString("Conv title"),
                subtitle = AnnotatedString("conv subtitle long message to make it really long and fit more than one line"),
                date = "13th Mar 2022 19:45:44",
            ),
        )
    }
}

@Preview
@Composable
fun TemplateDisplayLongTitlePreview() {
    AppTheme {
        TemplateDisplay(
            data = TemplateDisplayData(
                id = "id1",
                title = AnnotatedString("Conv title - Very Long name"),
                subtitle = AnnotatedString("conv subtitle long message to make it really long and fit more than one line"),
                date = "13th Mar 2022 19:45:44",
            ),
        )
    }
}
