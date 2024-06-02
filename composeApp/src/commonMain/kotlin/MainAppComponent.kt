import data.repo.InAppSignaling
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext

@Inject
class MainAppComponent(
    private val mainContext: CoroutineContext,
    private val signaling: InAppSignaling,
) {

    val snackbars = signaling.snackbarEvents()
}
