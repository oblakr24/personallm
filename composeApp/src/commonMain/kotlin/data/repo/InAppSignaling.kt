package data.repo

import util.OpResult
import androidx.compose.material3.SnackbarDuration
import data.NetworkError
import di.Singleton
import io.github.aakira.napier.Napier
import me.tatarka.inject.annotations.Inject
import util.SingleEventFlow
import util.AppSnackbarData

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

        val subtitle = when (val e = error.error) {
            is NetworkError.Error -> e.ex.message
            NetworkError.NoData -> "No data"
            is NetworkError.NotSuccessful -> null
        }
        val duration = if (subtitle == null) {
            SnackbarDuration.Short
        } else {
            SnackbarDuration.Indefinite
        }
        val showCloseRow = subtitle != null
        snackbars.send(
            AppSnackbarData(
                title = error.error.toString(),
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
