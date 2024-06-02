package feature.commonui

import AppColors
import AppTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PrimaryTextButton(
    text: String,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(6.dp),
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(
            minWidth = ButtonDefaults.MinWidth,
            minHeight = 10.dp,
        ),
        enabled = enabled,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.PrimaryGreen,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = AppColors.DisabledGray,
        ),
    ) {
        Text(
            text = text,
            style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
        )
    }
}

@Preview
@Composable
private fun PrimaryTextButtonPreview() {
    AppTheme {
        PrimaryTextButton(text = "Text", onClick = {})
    }
}

@Preview
@Composable
private fun PrimaryTextButtonShortPreview() {
    AppTheme {
        PrimaryTextButton(
            text = "Short",
            small = true,
            contentPadding = PaddingValues(2.dp),
            onClick = {})
    }
}

@Preview
@Composable
private fun PrimaryTextButtonDisabledPreview() {
    AppTheme {
        PrimaryTextButton(text = "Text", enabled = false, onClick = {})
    }
}