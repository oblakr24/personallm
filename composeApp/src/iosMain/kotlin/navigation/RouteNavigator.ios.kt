package navigation

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class IntentHandler {
    actual fun openURL(url: String) {
        val nsURL = NSURL.URLWithString(url)
        nsURL?.let {
            UIApplication.sharedApplication.openURL(it)
        } ?: println("Invalid URL")
    }
}
