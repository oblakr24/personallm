import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val appVersion: AppVersion by lazy {
        val infoDict = NSBundle.mainBundle.infoDictionary
        val build = infoDict?.get("CFBundleVersion") as? String ?: "release"
        val shortVersionString = infoDict?.get("CFBundleShortVersionString") as? String ?: "Unknown"
        AppVersion(
            name = "$shortVersionString (build: $build)"
        )
    }
}

actual fun getPlatform(): Platform = IOSPlatform()