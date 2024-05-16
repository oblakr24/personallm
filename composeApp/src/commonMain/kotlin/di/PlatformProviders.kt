package di

import data.DatastorePrefsFactory
import db.DriverFactory

expect class PlatformProviders {

    fun driverFactory(): DriverFactory

    fun datastoreFactory(): DatastorePrefsFactory
}