import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color

object AppColors {
    val WhiteBG = Color.White
    val OffWhiteBG = Color(0xFFFAFAFA)

    val PrimaryGreen = Color(0xFF629467)
    val PrimaryDarkGreen = Color(0xFF0A2C36)
    val PrimaryDarkNavy = Color(0xFF0D4353)
    val SecondaryGreen = Color(0xFF47B08F)
    val FadedGreen =  Color(0xFFC2E6C6) // Color(0xFF91b495)
    val VeryFadedGreen = Color(0xFFC2E6C6)
    val BackgroundLightGreen = Color(0xFFF4F7F1)//Color(0xFFF4F7F1)
    val DisabledGrayBackground = Color(0xFFE1E2E5)
    val DisabledGray = Color(0xFF5E6166)
    val DisabledLighterGray = Color(0xFF9A9B9E)
    val SecondaryRed = Color(0xFFCC4D51)
    val PrimaryTextGray = Color(0xFF5E6166)
    val GrayLine = Color(0xFFE1E2E5)
    val PlaceholderGray = Color(0xFFBCBEC2)
    val IconDarkGray  = Color(0xFF323232)

    val GrayText = Color(0xFF909998)
    val DarkGrayText = Color(0xFF4B4D50)

    val Gray400 = Color(0xFF94979E)
    val Gray600 = Color(0xFF61646B)
    val Gray500 = Color(0xFF797D86)
    val Grayscale = Color(0xFFE1E2E5)

    val Gray50 = Color(0xFFF2F2F3)

    val VeryDarkGray = Color(0xFF323335)

    val BlackAlpha20 = Color(0x33000000)
}

fun Color.alpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Color = this.copy(alpha = alpha)