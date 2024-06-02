package feature.main

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.Value
import di.AppComponent
import di.VMContext
import feature.chats.ChatsComponent
import feature.chats.ChatsScreen
import feature.landing.LandingComponent
import feature.landing.LandingScreen
import kotlinx.serialization.Serializable

@OptIn(ExperimentalDecomposeApi::class)
class MainComponent(
    componentContext: ComponentContext,
    private val diComponent: AppComponent,
): ComponentContext by componentContext {

    private val pagesNavigation = PagesNavigation<PageConfig>()

    val pages: Value<ChildPages<*, PageComponent>> =
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

    fun selectPage(index: Int) {
        pagesNavigation.select(index = index)
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
}




