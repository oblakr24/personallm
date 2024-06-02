import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// TODO: Setup
private val SourceSansProFont: FontFamily? = null

// These are fallbacks for the most part, i.e. for styling of nested components without direct access.
val Typography = Typography(
    bodyMedium = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        letterSpacing = TextUnit.Unspecified
    ),
    labelLarge = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = TextUnit.Unspecified,
    ),
    labelMedium = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = TextUnit.Unspecified,
    ),
    labelSmall = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = TextUnit.Unspecified,
    ),
)

object AppTypography {
    val TinySubtitle = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        textAlign = TextAlign.Center,
    )
    val SubtitleSemiBold = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val SmallClickableSubtitleCentered = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        textAlign = TextAlign.Center,
    )
    val SmallClickableSubtitle = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        textAlign = TextAlign.Start,
    )
    val LabelMediumGreenBold = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = AppColors.PrimaryGreen,
    )
    val SubtitleGray = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        color = AppColors.GrayText,
    )
    val SubtitleWhite = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        color = Color.White,
    )
    val Body2 = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    )
    val Body2SemiBold = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    )
    val Body3 = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val Body3SemiBold = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val Body4 = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 17.sp,
    )
    val Body4SemiBold = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 17.sp,
    )
    val Body22Normal = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 24.sp,
    )
    val Body22SemiBold = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 24.sp,
    )
    val Body18Strong = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )
    val Body18Regular = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )
    val Body18SemiBold = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )
    val Body16Normal = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val Body16SemiBold = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val Body13Normal = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    )
    val Body12Normal = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 14.sp,
    )
    val Body12SemiBold = TextStyle(
        fontFamily = SourceSansProFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
    )
}
