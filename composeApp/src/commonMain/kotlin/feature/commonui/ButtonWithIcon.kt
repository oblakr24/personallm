package feature.commonui

import AppColors
import AppTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ButtonWithIcon(
    text: String,
    icon: ImageVector,
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
        border = BorderStroke(1.dp, AppColors.GrayLine),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppColors.WhiteBG,
            contentColor = AppColors.PrimaryTextGray,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = AppColors.DisabledGray,
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = "Icon",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified,
            )
            Spacer(Modifier.weight(1f))
            if (loading) {
                LoadingSpinner()
            } else {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.weight(1f))
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

@Preview
@Composable
private fun ButtonWithIconPreview() {
    AppTheme {
        Column(modifier = Modifier.background(AppColors.GrayText)) {
            ButtonWithIcon(
                text = "Sign out",
                icon = Icons.AutoMirrored.Filled.Logout,
                onClick = { })
            Spacer(Modifier.height(8.dp))
            ButtonWithIcon(
                text = "Sign in with email",
                icon = Icons.AutoMirrored.Filled.Login,
                onClick = { })
        }
    }
}
