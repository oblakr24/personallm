package feature.commonui

import AppColors
import AppTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    onClick: () -> Unit,
) {
    Button(
        onClick = {
            if (!loading) {
                onClick()
            }
        },
        modifier = modifier.height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = AppColors.DisabledGrayBackground,
            disabledContentColor = AppColors.DisabledGray,
        ),
        contentPadding = contentPadding,
        shape = RoundedCornerShape(8.dp)
    ) {
        if (loading) {
            LoadingSpinner(trackColor = if (enabled) AppColors.FadedGreen else AppColors.Gray400)
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Preview
@Composable
private fun PrimaryButtonPreviews() {
    AppTheme {
        Column {
            PrimaryButton(text = "Button Text", onClick = { })
            PrimaryButton(text = "Button Text", enabled = false, onClick = { })
            PrimaryButton(text = "Button Text", loading = true, onClick = { })
            PrimaryButton(text = "Button Text", loading = true, enabled = false, onClick = { })
        }
    }
}