package di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import data.DatastorePrefsFactory
import data.SETTINGS_PREFERENCES
import data.createDataStoreWithDefaults
import db.DriverFactory
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
                corruptionHandler: ReplaceFileCorruptionHandler<Preferences>?,
                coroutineScope: CoroutineScope
            ): DataStore<Preferences> {
                return createDataStoreWithDefaults(
                    corruptionHandler = corruptionHandler,
                    coroutineScope = coroutineScope,
                    path = { SETTINGS_PREFERENCES }
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
}