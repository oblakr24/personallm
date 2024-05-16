package di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import data.DatastorePrefsFactory
import data.SETTINGS_PREFERENCES
import data.createDataStoreWithDefaults
import db.DriverFactory
import kotlinx.coroutines.CoroutineScope

actual class PlatformProviders {
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
}