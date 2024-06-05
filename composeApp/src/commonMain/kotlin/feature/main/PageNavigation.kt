package feature.main

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import di.AppComponent
import di.VMContext
import feature.chats.ChatsComponent
import feature.chats.ChatsScreen
import feature.landing.LandingComponent
import feature.landing.LandingScreen
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

interface PageNavigation {
    @OptIn(ExperimentalDecomposeApi::class)
    val pages: Value<ChildPages<*, PageComponent>>

    val selectedPage: Value<Int>

    fun selectPage(index: Int)
}

sealed class PageComponent {
    class Landing(val component: LandingComponent) : PageComponent() {
        @Composable
        override fun Content() = LandingScreen(component)
    }

    class Chats(val component: ChatsComponent) : PageComponent() {
        @Composable
        override fun Content() = ChatsScreen(component)
    }

    @Composable
    abstract fun Content()
}

@Serializable
sealed class PageConfig {

    @Serializable
    data object Landing : PageConfig()


    @Serializable
    data object Chats : PageConfig()
}

@OptIn(ExperimentalDecomposeApi::class)
class MainPageNavigator(
    private val diComponent: AppComponent,
    componentContext: ComponentContext,
): ComponentContext by componentContext, PageNavigation {

    private val pagesNavigation = PagesNavigation<PageConfig>()

    override val pages: Value<ChildPages<*, PageComponent>> =
        childPages(
            source = pagesNavigation,
            serializer = PageConfig.serializer(),
            initialPages = {
                Pages(
                    items = listOf(PageConfig.Landing, PageConfig.Chats),
                    selectedIndex = 0,
                )
            },
        ) { config, childComponentContext ->
            when (config) {
                PageConfig.Landing -> PageComponent.Landing(
                    diComponent.landingComponent(VMContext.fromContext(childComponentContext))
                )
                PageConfig.Chats -> PageComponent.Chats(
                    diComponent.chatsComponent(VMContext.fromContext(childComponentContext))
                )
            }
        }

    override val selectedPage: Value<Int> = pages.map { it.selectedIndex }

    override fun selectPage(index: Int) {
        pagesNavigation.select(index = index)
    }
}




