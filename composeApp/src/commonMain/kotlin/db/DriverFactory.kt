package db

import app.cash.sqldelight.db.SqlDriver
import personallm.db.Database

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val driver = driverFactory.createDriver()
    val database = Database(driver)
    return database
}

val Database.Companion.DB_VERSION: Int get() {
    return 2
}