import data.repo.InAppSignaling
import me.tatarka.inject.annotations.Inject
import usecase.DarkModeToggleUseCase

@Inject
class MainAppComponent(
    private val darkModeUseCase: DarkModeToggleUseCase,
    private val signaling: InAppSignaling,
){
    val snackbars = signaling.snackbarEvents()

    val darkModeEnabled = darkModeUseCase.darkModeEnabled()
}
