package data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings
import com.russhwolf.settings.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalSettingsApi::class)
class AppStorage(factory: Settings.Factory) {

    private val settings = factory.create("app_settings")
    @OptIn(ExperimentalSettingsApi::class)
    private val suspendSettings = settings.toSuspendSettings()

    private val darkModeState: DarkModeState

    init {
        val darkMode = settings.getStringOrNull(KEY_DARK_MODE)
        darkModeState = darkMode.toDarkModeEnum()

    }

    private val _darkModeStateFlow: MutableStateFlow<DarkModeState> by lazy {
        MutableStateFlow(darkModeState)
    }

    val darkModeStateFlow = _darkModeStateFlow.asStateFlow()

    suspend fun updateDarkmode(new: DarkModeState) {
        suspendSettings.putString(KEY_DARK_MODE, new.toStringValue())
        _darkModeStateFlow.update { new }
    }

    private fun String?.toDarkModeEnum() = when(this) {
        DARK_MODE_ON -> DarkModeState.ON
        DARK_MODE_OFF -> DarkModeState.OFF
        DARK_MODE_FOLLOW_SYSTEM -> DarkModeState.FOLLOW_SYSTEM
        null -> DarkModeState.FOLLOW_SYSTEM
        else -> DarkModeState.FOLLOW_SYSTEM
    }

    private fun DarkModeState.toStringValue() = when(this) {
        DarkModeState.ON -> DARK_MODE_ON
        DarkModeState.OFF -> DARK_MODE_OFF
        DarkModeState.FOLLOW_SYSTEM -> DARK_MODE_FOLLOW_SYSTEM
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val DARK_MODE_ON = "dark_mode_on"
        private const val DARK_MODE_OFF = "dark_mode_off"
        private const val DARK_MODE_FOLLOW_SYSTEM = "dark_mode_follow_system"
    }
}

enum class DarkModeState {
    ON, OFF, FOLLOW_SYSTEM
}