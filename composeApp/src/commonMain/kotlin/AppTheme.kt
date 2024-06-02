import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppColors.PrimaryGreen,
    secondary = AppColors.PrimaryGreen,
    tertiary = AppColors.PrimaryGreen,
    background = AppColors.WhiteBG,
    primaryContainer = AppColors.WhiteBG,
    secondaryContainer = AppColors.WhiteBG,
    surface = AppColors.WhiteBG,
    onSurface = AppColors.IconDarkGray,
    surfaceTint = AppColors.WhiteBG,
    surfaceVariant = AppColors.WhiteBG,
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
