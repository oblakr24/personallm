package util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Stable

data class AppSnackbarData(
    val title: String,
    val subtitle: String?,
    val type: Type = Type.NORMAL,
    val showCloseRow: Boolean = false,
    val duration: SnackbarDuration = SnackbarDuration.Short,
) {

    enum class Type {
        NORMAL, ERROR;
    }

    fun toVisuals() = AppSnackbarVisuals(
        message = title,
        subtitle = subtitle,
        type = type,
        duration = duration,
        showCloseRow = showCloseRow,
    )
}

@Stable
data class AppSnackbarVisuals(
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val message: String,
    override val withDismissAction: Boolean = false,
    val subtitle: String?,
    val type: AppSnackbarData.Type,
    val showCloseRow: Boolean,
) : SnackbarVisuals
