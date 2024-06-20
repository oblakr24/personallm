package di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import data.DatastorePrefsFactory
import data.createDataStoreWithDefaults
import db.DriverFactory
import feature.sharedimage.ImageResolver
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import navigation.IntentHandler

actual class PlatformProviders {

    actual fun initialize() {
        Napier.base(DebugAntilog())
    }

    actual fun driverFactory(): DriverFactory {
        return DriverFactory()
    }

    private val prefsFactory by lazy {
        object : DatastorePrefsFactory {
            override fun dataStorePreferences(
                corruptionHandler: ReplaceFileCorruptionHandler<androidx.datastore.preferences.core.Preferences>?,
                coroutineScope: CoroutineScope,
                name: String,
            ): DataStore<androidx.datastore.preferences.core.Preferences> {
                return createDataStoreWithDefaults(
                    corruptionHandler = corruptionHandler,
                    coroutineScope = coroutineScope,
                    path = { "settings/$name" }
                )
            }
        }
    }

    actual fun datastoreFactory(): DatastorePrefsFactory {
        return prefsFactory
    }

    actual fun intentHandler(): IntentHandler {
        return IntentHandler()
    }

    actual fun settingsFactory(): Settings.Factory {
        return PreferencesSettings.Factory()
    }

    actual fun imageResolver(): ImageResolver {
        return ImageResolver()
    }
}