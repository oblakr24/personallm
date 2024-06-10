interface Platform {
    val name: String
    val appVersion: AppVersion
}

data class AppVersion(
    val name: String,
)

expect fun getPlatform(): Platform