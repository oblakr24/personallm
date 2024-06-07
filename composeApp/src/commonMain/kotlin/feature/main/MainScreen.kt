package feature.main

import AppTypography
import alpha
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.BottomNavigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.pages.Pages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import feature.commonui.TitledScaffold
import kotlinx.coroutines.launch

data class MainScreenUIState(
    val drawer: MainDrawerUIState = MainDrawerUIState(),
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalDecomposeApi::class)
@Composable
fun MainScreen(component: MainComponent) {
    val state = component.state.collectAsState().value
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                MainDrawer(state.drawer) { action ->
                    scope.launch {
                        drawerState.close()
                        component.onAction(action)
                    }
                }
            }
        },
    ) {
        TitledScaffold(
            title = "PersonaLLM",
            footer = {
                val selectedIdx = component.selectedPage.subscribeAsState().value
                BottomNavigationBar(selectedIdx, onSelected = { index ->
                    component.selectPage(index)
                })
            }, leadingIcon = {
                IconButton(onClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Open menu",
                    )
                }
            }, content = {
                Pages(
                    pages = component.pages,
                    onPageSelected = component::selectPage,
                    modifier = Modifier.fillMaxSize(),
                    scrollAnimation = PagesScrollAnimation.Default,
                    pager = { modifier, state, key, pageContent ->
                        HorizontalPager(
                            state,
                            pageContent = pageContent,
                            key = key,
                            modifier = modifier
                        )
                    },
                    pageContent = { _, pageComponent ->
                        pageComponent.Content()
                    }
                )
            })
    }
}

enum class BottomNavItem(val icon: ImageVector, val label: String) {
    Landing(Icons.Outlined.Home, "Landing"),
    Chats(Icons.Outlined.ChatBubbleOutline, "Chats"),
    Templates(Icons.Outlined.Build, "Templates"),
}

@Composable
fun BottomNavigationBar(selectedIdx: Int, onSelected: (Int) -> Unit) {
    BottomNavigation(modifier = Modifier, backgroundColor = MaterialTheme.colorScheme.primaryContainer) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BottomNavItem.entries.forEachIndexed { index, item ->
                BottomNavItemDisplay(
                    selected = selectedIdx == index,
                    item,
                    modifier = Modifier.weight(1f).clickable {
                        onSelected(index)
                    })
            }
        }
    }
}

@Composable
fun BottomNavItemDisplay(selected: Boolean, item: BottomNavItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Icon(
            imageVector = item.icon,
            contentDescription = "Icon",
            modifier = Modifier.size(16.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.alpha(0.4f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            item.label,
            style = if (selected) AppTypography.Body12SemiBold else AppTypography.Body12Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.alpha(0.4f)
        )
    }
}
