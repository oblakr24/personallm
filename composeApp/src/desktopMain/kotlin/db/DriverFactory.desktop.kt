package db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import personallm.db.Database
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbDirectory = File("java.io.tmpdir")
        if (!dbDirectory.exists()) {
            dbDirectory.mkdirs()
        }
        val dbFilePath = File(dbDirectory, "personallm.db")
        println("Database path: ${dbFilePath.absolutePath}")

        if (!dbFilePath.exists()) {
            println("Database file does not exist, will be created.")
        }
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${dbFilePath}")
        val result = Database.Schema.create(driver)
        // TODO: It is necessary to await this result for desktop. An issue was raised: https://github.com/cashapp/sqldelight/issues/5286
        runBlocking {
            result.await()
        }
        return driver
    }
}
