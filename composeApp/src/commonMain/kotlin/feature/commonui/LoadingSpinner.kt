package feature.commonui

import AppColors
import AppTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    trackColor: Color = AppColors.FadedGreen,
) {
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        color = Color.White,
        trackColor = trackColor,
        strokeWidth = 3.dp,
    )
}

@Preview
@Composable
private fun LoadingSpinnerPreview() {
    AppTheme {
        LoadingSpinner()
    }
}