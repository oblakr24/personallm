package feature.commonui

import AppColors
import AppTheme
import AppTypography
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NavigationButton(
    text: String, modifier: Modifier = Modifier,
    rightContent: @Composable BoxScope.() -> Unit = {
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            tint = Color.Unspecified,
            contentDescription = "Go"
        )
    },
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.PrimaryGreen,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f)
        )

        Box(modifier = Modifier.wrapContentWidth(align = Alignment.End)) {
            rightContent()
        }
    }
}

@Preview
@Composable
fun NavigationButtonPreview() {
    AppTheme {
        Column(modifier = Modifier.background(AppColors.WhiteBG)) {
            NavigationButton(text = "Preferred Currency", rightContent = {
                Text(text = "USD - $", style = AppTypography.LabelMediumGreenBold)
            }, onClick = {})
            NavigationButton(text = "Saved", onClick = {})
        }
    }
}