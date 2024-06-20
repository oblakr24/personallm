package data.repo

import androidx.compose.material3.SnackbarDuration
import data.NetworkError
import di.Singleton
import io.github.aakira.napier.Napier
import me.tatarka.inject.annotations.Inject
import util.AppSnackbarData
import util.OpResult
import util.SingleEventFlow

@Singleton
@Inject
class InAppSignaling {

    private val snackbars = SingleEventFlow<AppSnackbarData>()
    fun snackbarEvents() = snackbars.eventsAsFlow()

    fun handleGenericError(error: OpResult.Error<NetworkError>, silent: Boolean = false) {
        if (silent) {
            Napier.i("Silent error: $error")
            return
        }

        val title = when (val e = error.error) {
            is NetworkError.Error -> e.ex.message ?: "Error"
            NetworkError.NoData -> "No data"
            is NetworkError.NotSuccessful -> e.body
        }
        val subtitle = when (val e = error.error) {
            is NetworkError.Error -> null
            NetworkError.NoData -> null
            is NetworkError.NotSuccessful -> {
                val stringBuilder = StringBuilder()
                stringBuilder.append(e.additionalInfo())
                stringBuilder.toString()
            }
        }
        val duration = if (subtitle == null) {
            SnackbarDuration.Long
        } else {
            SnackbarDuration.Indefinite
        }
        val showCloseRow = subtitle != null
        snackbars.send(
            AppSnackbarData(
                title = title,
                subtitle = subtitle,
                type = AppSnackbarData.Type.ERROR,
                duration = duration,
                showCloseRow = showCloseRow
            )
        )
    }

    fun sendGenericMessage(message: String, subtitle: String? = null) {
        snackbars.send(
            AppSnackbarData(
                title = message,
                subtitle = subtitle,
                type = AppSnackbarData.Type.NORMAL
            )
        )
    }

    fun sendGenericError(message: String, subtitle: String? = null) {
        snackbars.send(
            AppSnackbarData(
                title = message,
                subtitle = subtitle,
                type = AppSnackbarData.Type.ERROR
            )
        )
    }

    fun handleError(msg: String, e: Throwable?) {
        val showCloseRow = e?.message != null
        val duration = if (!showCloseRow) {
            SnackbarDuration.Short
        } else {
            SnackbarDuration.Indefinite
        }
        snackbars.send(
            AppSnackbarData(
                title = msg,
                subtitle = e?.message,
                type = AppSnackbarData.Type.ERROR,
                showCloseRow = showCloseRow,
                duration = duration
            )
        )
    }
}
