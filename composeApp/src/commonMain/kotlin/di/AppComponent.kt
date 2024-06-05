package di

import MainAppComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import data.OpResultResponseConverterFactory
import data.OpenAIAPI
import data.StorageProvider
import data.StorageProviderImpl
import db.createDatabase
import de.jensklingenberg.ktorfit.Ktorfit
import feature.chat.ChatComponent
import feature.chats.ChatsComponent
import feature.samplerequest.SampleRequestComponent
import feature.image.ImageComponent
import feature.landing.LandingComponent
import feature.listing.ListingComponent
import feature.main.MainComponent
import feature.main.MainPageNavigator
import feature.main.PageNavigation
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import navigation.DefaultRootComponent
import navigation.RouteNavigator
import personallm.db.Database
import kotlin.coroutines.CoroutineContext

@Component
@Singleton
abstract class AppComponent {

    // Manually injected from outside, by each platform
    lateinit var navigation: StackNavigation<DefaultRootComponent.Config>
    lateinit var platformProviders: PlatformProviders

    // Components
    abstract val mainAppComponent: MainAppComponent
    abstract val mainComponent: (pageNavigator: PageNavigation, context: VMContext) -> MainComponent
    abstract val sampleRequestComponent: (context: VMContext) -> SampleRequestComponent
    abstract val listingComponent: (context: VMContext) -> ListingComponent
    abstract val landingComponent: (context: VMContext) -> LandingComponent
    abstract val imageComponent: (context: VMContext) -> ImageComponent
    abstract val chatComponent: (context: VMContext, config: DefaultRootComponent.Config.Chat) -> ChatComponent
    abstract val chatsComponent: (context: VMContext) -> ChatsComponent

    fun pageNavigation(componentContext: ComponentContext): PageNavigation {
        return MainPageNavigator(this, componentContext)
    }

    @Provides
    @Singleton
    fun navigation(): StackNavigation<DefaultRootComponent.Config> {
        return navigation
    }

    @Provides
    @Singleton
    fun navigator(): RouteNavigator {
        return object : RouteNavigator {
            override val navigation: StackNavigation<DefaultRootComponent.Config>
                get() = this@AppComponent.navigation

        }
    }

    @Provides
    fun mainContext(): CoroutineContext {
        return Dispatchers.Main
    }

    @Provides
    @Singleton
    fun db(): Database {
        return createDatabase(platformProviders.driverFactory())
    }

    @Provides
    @Singleton
    fun json(): Json {
        return Json {
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }
    }

    @Provides
    @Singleton
    fun storageProvider(json: Json): StorageProvider {
        return StorageProviderImpl(platformProviders, json)
    }

    @Provides
    fun ktorClient(json: Json): HttpClient {
        return HttpClient {
            install(HttpTimeout) {
                this.requestTimeoutMillis = 35_000
            }
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    @Provides
    @Singleton
    fun openAIAPI(client: HttpClient): OpenAIAPI {
        val ktorfit = Ktorfit.Builder()
            .httpClient(client)
            .baseUrl("https://api.openai.com/")
            .converterFactories(OpResultResponseConverterFactory())
            .build()
        val api = ktorfit.create<OpenAIAPI>()
        return api
    }
}
