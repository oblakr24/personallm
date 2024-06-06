package di

import com.russhwolf.settings.Settings
import data.DatastorePrefsFactory
import db.DriverFactory
import navigation.IntentHandler

expect class PlatformProviders {

    fun initialize()

    fun driverFactory(): DriverFactory

    fun datastoreFactory(): DatastorePrefsFactory

    fun intentHandler(): IntentHandler

    fun settingsFactory(): Settings.Factory
}