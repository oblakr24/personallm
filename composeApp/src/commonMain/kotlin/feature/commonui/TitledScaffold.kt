package feature.commonui

import AppTheme
import AppTypography
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitledScaffold(
    title: String? = null,
    onBackClicked: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
    footer: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    hideShadows: Boolean = false,
) {
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = if (hideShadows) 0.dp else 12.dp,
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        if (title != null) {
                            Text(
                                text = title,
                                style = AppTypography.Body2SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (titleContent != null) {
                            titleContent()
                        }
                    },
                    navigationIcon = {
                        if (leadingIcon != null) {
                            leadingIcon()
                        }
                        if (onBackClicked != null) {
                            IconButton(onClick = { onBackClicked() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    },
                    actions = actions,
                )
            }
        },
        bottomBar = {
            footer?.let {
                Surface(
                    shadowElevation = if (hideShadows) 0.dp else 12.dp,
                    modifier = Modifier.imePadding(),
                ) {
                    it()
                }
            }
        },
        floatingActionButton = floatingActionButton,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun TitledScaffoldPreview() {
    AppTheme {
        TitledScaffold(title = "Title", content = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Content here")
            }
        }, onBackClicked = {}, footer = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Footer here")
            }
        })
    }
}
