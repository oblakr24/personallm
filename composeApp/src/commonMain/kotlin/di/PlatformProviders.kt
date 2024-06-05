package di

import data.DatastorePrefsFactory
import db.DriverFactory
import navigation.IntentHandler

expect class PlatformProviders {

    fun initialize()

    fun driverFactory(): DriverFactory

    fun datastoreFactory(): DatastorePrefsFactory

    fun intentHandler(): IntentHandler
}