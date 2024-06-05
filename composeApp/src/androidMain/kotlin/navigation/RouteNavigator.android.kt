package navigation

import android.content.Intent
import android.net.Uri
import di.CurrentActivityProvider

actual class IntentHandler(private val activityProvider: CurrentActivityProvider) {
    actual fun openURL(url: String) {
        activityProvider.activity()?.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
    }
}
