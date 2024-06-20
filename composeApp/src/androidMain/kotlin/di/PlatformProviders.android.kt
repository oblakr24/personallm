package di

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import androidx.constraintlayout.core.platform.WeakReference
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import data.DatastorePrefsFactory
import data.createDataStoreWithDefaults
import db.DriverFactory
import feature.sharedimage.ImageResolver
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import navigation.IntentHandler
import java.io.File

actual class PlatformProviders(private val appContext: Context, private val activityProvider: CurrentActivityProvider) {

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
                coroutineScope: CoroutineScope,
                name: String,
            ): DataStore<Preferences> {
                return createDataStoreWithDefaults(
                    corruptionHandler = corruptionHandler,
                    coroutineScope = coroutineScope,
                    path = {
                        File(appContext.filesDir, "datastore/$name").path
                    }
                )
            }

        }
    }

    actual fun datastoreFactory(): DatastorePrefsFactory {
        return prefsFactory
    }

    actual fun intentHandler(): IntentHandler {
        return IntentHandler(activityProvider)
    }

    actual fun settingsFactory(): Settings.Factory {
        return SharedPreferencesSettings.Factory(appContext)
    }

    actual fun imageResolver(): ImageResolver {
        return ImageResolver(appContext)
    }
}

class CurrentActivityProvider(private var current: WeakReference<Activity>? = null) {

    fun activity(): Activity? {
        return current?.get()
    }

    val callback = object: ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            current = WeakReference(activity)
        }

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityResumed(activity: Activity) = Unit

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) = Unit

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) {
            current = null
        }
    }
}