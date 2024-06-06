package usecase

import data.AppStorage
import data.DarkModeState
import di.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Inject

@Singleton
@Inject
class DarkModeToggleUseCase(
    private val storage: AppStorage,
) {

    fun darkModeEnabled(): StateFlow<DarkModeState> = storage.darkModeStateFlow

    suspend fun setDarkMode(new: DarkModeState) {
        storage.updateDarkmode(new)
    }
}

