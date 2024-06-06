package feature.commonui

import AppColors
import AppTheme
import alpha
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = {
            if (!loading) {
                onClick()
            }
        },
        modifier = modifier.height(56.dp),
        enabled = enabled,
        border = BorderStroke(1.dp, if (enabled)  MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primaryContainer.alpha(0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor =  MaterialTheme.colorScheme.primaryContainer,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.primaryContainer.alpha(0.4f),

            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (loading) {
            LoadingSpinner()
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview
@Composable
private fun SecondaryButtonPreviews() {
    AppTheme {
        Column(modifier = Modifier.background(AppColors.WhiteBG)) {
            SecondaryButton(text = "Button Text", onClick = { })
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(text = "Button Text", enabled = false, onClick = { })
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(text = "Button Text", loading = true, onClick = { })
        }
    }
}