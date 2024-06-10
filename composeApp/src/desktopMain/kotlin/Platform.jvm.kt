class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val appVersion: AppVersion by lazy {
        val appVersion = System.getProperty("jpackage.app-version") ?: "Pending"
        AppVersion(
            name = appVersion
        )
    }
}

actual fun getPlatform(): Platform = JVMPlatform()