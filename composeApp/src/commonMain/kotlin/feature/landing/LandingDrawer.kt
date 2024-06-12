package feature.landing

import AppTheme
import alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.DarkModeState
import feature.commonui.ButtonWithIcon
import feature.commonui.PrimaryTextButton
import org.jetbrains.compose.ui.tooling.preview.Preview

data class LandingDrawerUIState(
    val darkMode: DarkModeState? = null,
    val versionLabel: String = "",
)

@Composable
fun LandingDrawer(
    state: LandingDrawerUIState,
    onAction: (LandingAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth(), horizontalAlignment = Alignment.Start
    ) {
        ButtonWithIcon(modifier = Modifier.padding(horizontal = 16.dp),
            text = "FAQ",
            icon = Icons.Outlined.QuestionAnswer,
            onClick = { onAction(LandingAction.FAQClicked) })
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1f))

        Row(
            Modifier
                .wrapContentWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Appearance")
            Row(
                Modifier
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                PrimaryTextButton("Light", small = true) {
                    onAction(LandingAction.SetDarkMode(DarkModeState.OFF))
                }
                PrimaryTextButton("Dark", small = true) {
                    onAction(LandingAction.SetDarkMode(DarkModeState.ON))
                }
                PrimaryTextButton("Auto", small = true) {
                    onAction(LandingAction.SetDarkMode(DarkModeState.FOLLOW_SYSTEM))
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.alpha(0.6f))
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                Modifier
                    .fillMaxWidth(),
            ) {
                Text("This is an open-source app.\nYou can post feedback, change requests or contribute on Github:")
                Spacer(modifier = Modifier.height(12.dp))
                ButtonWithIcon("Github", Icons.Filled.Code) {
                    onAction(LandingAction.OpenRepoUrl)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(state.versionLabel, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
private fun MainDrawerPreview() {
    val darkMode = false
    AppTheme(overrideDarkMode = darkMode) {
        LandingDrawer(
            LandingDrawerUIState(
                darkMode = DarkModeState.ON,
                versionLabel = "Version 1.0.0",
            ),
            onAction = {})
    }
}