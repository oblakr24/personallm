import android.os.Build
import com.rokoblak.personallm.BuildConfig

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val appVersion: AppVersion = AppVersion(
        name = BuildConfig.VERSION_NAME,
    )
}

actual fun getPlatform(): Platform = AndroidPlatform()