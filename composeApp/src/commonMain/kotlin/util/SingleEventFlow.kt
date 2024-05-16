package util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

// Used for communicating single event effects to a single consumer.
// Should *NOT* be used for things that *must* be processed, as there is always a possibility of a hot flow emission not being collected.
class SingleEventFlow<T> {

    private val channel = Channel<T>(capacity = 1)

    fun send(event: T) {
        val prev = channel.tryReceive().getOrNull()
        if (prev != null) {
//            Log.e(TAG, "Event $prev was not collected before a new one $event was sent")
        }

        val sent = channel.trySend(event)
        if (sent.isFailure) {
//            Log.e(TAG, "Channel send failure: ${sent.exceptionOrNull()?.message}")
        }
    }

    fun eventsAsFlow() = channel.receiveAsFlow()

    companion object {
        private const val TAG = "SingleEventFlow"
    }
}