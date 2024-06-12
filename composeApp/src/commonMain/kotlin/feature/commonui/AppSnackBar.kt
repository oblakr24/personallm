package feature.commonui

import AppColors
import AppTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppSnackBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showCloseRow: Boolean = false,
    onDismissRequest: (() -> Unit)? = null,
    containerColor: Color = AppColors.SecondaryGreen
) {
    Snackbar(
        containerColor = containerColor,
        modifier = modifier,
        actionOnNewLine = true,
        action = {
            if (showCloseRow) {
                TextButton(
                    onClick = {
                        onDismissRequest?.invoke()
                    },
                    modifier = Modifier,
                    contentPadding = PaddingValues(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = AppColors.WhiteBG,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = AppColors.DisabledGray,
                    ),
                ) {
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 12.dp))
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview
@Composable
private fun AppSnackBarPreview() {
    AppTheme {
        Column {
            AppSnackBar(
                title = "Welcome, UserName!",
                subtitle = "You're logged in via user@mail.com"
            )
            AppSnackBar(
                title = "Title",
                subtitle = "Subtitle: long so that it goes into multiple lines which will make the snackbar taller even if it goes into more than two lines so that we can show full error message.",
                containerColor = AppColors.SecondaryRed,
                showCloseRow = true,
            )

            AppSnackBar(
                title = "Just title, no subtitle",
            )
        }
    }
}
