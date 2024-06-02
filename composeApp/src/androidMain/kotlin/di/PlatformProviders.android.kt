package di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import data.DatastorePrefsFactory
import data.SETTINGS_PREFERENCES
import data.createDataStoreWithDefaults
import db.DriverFactory
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import java.io.File

actual class PlatformProviders(private val appContext: Context) {

    actual fun initialize() {
        Napier.base(DebugAntilog())
    }

    actual fun driverFactory(): DriverFactory {
        return DriverFactory(context = appContext)
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
                    path = {
                        File(appContext.filesDir, "datastore/$SETTINGS_PREFERENCES").path
                    }
                )
            }

        }
    }

    actual fun datastoreFactory(): DatastorePrefsFactory {
        return prefsFactory
    }
}