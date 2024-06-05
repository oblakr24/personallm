package navigation

import MainAppComponent
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import di.AppComponent
import di.VMContext
import feature.chat.ChatComponent
import feature.chat.ChatScreen
import feature.samplerequest.SampleRequestComponent
import feature.image.ImageComponent
import feature.image.ImageScreen
import feature.listing.ListingComponent
import feature.listing.ListingScreen
import feature.main.MainComponent
import feature.main.MainScreen
import feature.samplerequest.SampleRequestScreen
import kotlinx.serialization.Serializable

interface RootComponent: BackHandlerOwner {

    val childStack: Value<ChildStack<*, Child>>

    val mainAppComponent: MainAppComponent

    fun onBackClicked()

    sealed class Child {

        class Main(val component: MainComponent) : Child() {
            @Composable
            override fun Content() = MainScreen(component)
        }

        class SampleRequest(val component: SampleRequestComponent) : Child() {
            @Composable
            override fun Content() = SampleRequestScreen(component)
        }

        class Listing(val component: ListingComponent) : Child() {
            @Composable
            override fun Content() = ListingScreen(component)
        }

        class Image(val component: ImageComponent) : Child() {
            @Composable
            override fun Content() = ImageScreen(component)
        }

        class Chat(val component: ChatComponent) : Child() {
            @Composable
            override fun Content() = ChatScreen(component)
        }

        @Composable
        abstract fun Content()
    }
}

interface RouteNavigator {
    fun navigateUp() {
        navigation.pop()
    }

    val navigation: StackNavigation<DefaultRootComponent.Config>
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val diComponent: AppComponent,
) : RootComponent, ComponentContext by componentContext {

    init {
        diComponent.platformProviders.initialize()
    }

    private val navigation = StackNavigation<Config>().also {
        diComponent.navigation = it
    }

    override val mainAppComponent: MainAppComponent
        get() = diComponent.mainAppComponent

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Main,
            handleBackButton = true, // Pop the back stack on back button press
            childFactory = ::createChild,
        )

    override fun onBackClicked() {
        navigation.pop()
    }

    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child =
        when (config) {
            Config.Main -> {
                RootComponent.Child.Main(
                    diComponent.mainComponent(diComponent.pageNavigation(componentContext), VMContext.fromContext(componentContext))
                )
            }

            Config.Home -> {
                RootComponent.Child.SampleRequest(
                    diComponent.sampleRequestComponent(VMContext.fromContext(componentContext))
                )
            }

            Config.Listing -> {
                RootComponent.Child.Listing(
                    diComponent.listingComponent(VMContext.fromContext(componentContext))
                )
            }

            Config.Image -> {
                RootComponent.Child.Image(
                    diComponent.imageComponent(VMContext.fromContext(componentContext))
                )
            }

            is Config.Chat -> {
                RootComponent.Child.Chat(
                    diComponent.chatComponent(VMContext.fromContext(componentContext), config)
                )
            }
        }

    @Serializable
    sealed class Config {

        @Serializable
        data object Main : Config()

        @Serializable
        data object Home : Config()

        @Serializable
        data object Listing : Config()

        @Serializable
        data object Image : Config()

        @Serializable
        data class Chat(val chatId: String?) : Config()
    }
}