package di

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

interface VMContext {

    val lifecycle: Lifecycle
    val instanceKeeper: InstanceKeeper

    companion object {
        fun fromContext(componentContext: ComponentContext) = object : VMContext {
            override val lifecycle: Lifecycle = componentContext.lifecycle
            override val instanceKeeper: InstanceKeeper = componentContext.instanceKeeper
        }
    }
}

class VMScope(mainContext: CoroutineContext) : InstanceKeeper.Instance, CoroutineScope {

    private val scope = CoroutineScope(mainContext + SupervisorJob())

    override fun onDestroy() {
        scope.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = scope.coroutineContext
}

inline fun <reified T: VMContext>T.vmScope(mainContext: CoroutineContext): VMScope {
    val key = this::class.simpleName ?: "-"
    return instanceKeeper.getOrCreate(key) { VMScope(mainContext) }
}