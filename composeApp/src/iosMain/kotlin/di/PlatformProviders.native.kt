package di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import data.DatastorePrefsFactory
import data.SETTINGS_PREFERENCES
import db.DriverFactory
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import navigation.IntentHandler
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual class PlatformProviders {

    actual fun initialize() {
        Napier.base(DebugAntilog())
    }

    actual fun driverFactory(): DriverFactory {
        return DriverFactory()
    }

    private var prefsFactory: DatastorePrefsFactory? = null

    @OptIn(ExperimentalForeignApi::class)
    actual fun datastoreFactory(): DatastorePrefsFactory {
        if (prefsFactory != null) return prefsFactory!!
        prefsFactory = object : DatastorePrefsFactory {
            override fun dataStorePreferences(
                corruptionHandler: ReplaceFileCorruptionHandler<Preferences>?,
                coroutineScope: CoroutineScope
            ): DataStore<Preferences> {
                return data.createDataStoreWithDefaults(
                    corruptionHandler = corruptionHandler,
                    coroutineScope = coroutineScope,
                    path = {
                        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                            directory = NSDocumentDirectory,
                            inDomain = NSUserDomainMask,
                            appropriateForURL = null,
                            create = false,
                            error = null,
                        )
                        (requireNotNull(documentDirectory).path + "/$SETTINGS_PREFERENCES")
                    }
                )
            }
        }
        return prefsFactory!!
    }

    actual fun intentHandler(): IntentHandler {
        return IntentHandler()
    }

    actual fun settingsFactory(): Settings.Factory {
        return NSUserDefaultsSettings.Factory()
    }
}