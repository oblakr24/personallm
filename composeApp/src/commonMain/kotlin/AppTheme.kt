import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryDark = Color(0xFFD6D6EC)
val PrimaryLight = Color.DarkGray
val PrimaryVariantLight = Color(0xFFDBDBEC)
val PrimaryVariantDark = Color(0xFF303030)

val SecondaryLight = Color.DarkGray
val SecondaryDark = Color.LightGray

val DarkRed = Color(0xFFD50F00)
val DarkYellow = Color(0xFFDBA400)
val DarkOrange = Color(0xFFCE3100)
val DarkBrown = Color(0xFF882D10)
val DarkGreen = Color(0xFF008805)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = AppColors.PrimaryGreen,
    tertiary = AppColors.PrimaryGreen,
    background = AppColors.WhiteBG,
    primaryContainer = PrimaryVariantLight,
    onPrimaryContainer = AppColors.VeryDarkGray,
    secondaryContainer = PrimaryVariantLight,
    surface = AppColors.WhiteBG,
    onSurface = AppColors.IconDarkGray,
    surfaceTint = AppColors.WhiteBG,
    surfaceVariant = AppColors.WhiteBG,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = SecondaryDark,
    primaryContainer = PrimaryVariantDark,
    secondaryContainer = PrimaryVariantDark,
    onPrimaryContainer = AppColors.OffWhiteBG,
)

@Composable
fun AppTheme(
    overrideDarkMode: Boolean? = null,
    darkTheme: Boolean = overrideDarkMode ?: isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
