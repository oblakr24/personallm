package data

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import di.PlatformProviders
import di.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath

interface StorageProvider {

    fun <T : Any> create(
        name: String,
        serializer: KSerializer<T>,
        defaultData: () -> T,
    ): PersistedStorage<T>
}

interface PersistedStorage<T> {

    suspend fun initialize()

    fun flow(): SharedFlow<T>

    suspend fun update(block: suspend T.() -> T)

    suspend fun clear()
}

interface DatastorePrefsFactory {

    fun dataStorePreferences(
        corruptionHandler: ReplaceFileCorruptionHandler<Preferences>?,
        coroutineScope: CoroutineScope,
        name: String,
    ): DataStore<Preferences>
}

internal fun createDataStoreWithDefaults(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    path: () -> String,
) = PreferenceDataStoreFactory
    .createWithPath(
        corruptionHandler = corruptionHandler,
        scope = coroutineScope,
        migrations = emptyList(),
        produceFile = {
            path().toPath()
        }
    )

private class GenericPersistedStorage(
    private val name: String,
    private val factory: DatastorePrefsFactory,
    private val defaultData: () -> String
) {

    val scope = CoroutineScope(Dispatchers.IO + Job())

    private val flow = flow {
        ensurePopulated()
        emitAll(flow().map {
            it
        })
    }.distinctUntilChanged()

    val state: SharedFlow<String> =
        flow.filterNotNull().shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)

    private val store = factory.dataStorePreferences(null, scope, name)

    suspend fun update(block: suspend String.() -> String) {
        ensurePopulated()
        val encoded = retrieve() ?: return
        val new = block(encoded)
        new.store()
    }

    suspend fun ensurePopulated() {
        if (!isKeyStored()) {
            defaultData().store()
        }
    }

    private suspend fun String.store() {
        store.edit {
            it[KEY_DATA] = this
        }
    }

    suspend fun clear() {
        store.edit {
            it.clear()
        }
    }

    private suspend fun isKeyStored() = store.data.firstOrNull()?.contains(KEY_DATA) ?: false

    private suspend fun retrieve(): String? {
        return store.data.firstOrNull()?.get(KEY_DATA)
    }

    private fun flow() = store.data.mapNotNull { it[KEY_DATA] }

    companion object {
        private val KEY_DATA = stringPreferencesKey("key-data")
    }
}

@Singleton
class StorageProviderImpl(
    private val platformProviders: PlatformProviders,
    private val json: Json
) : StorageProvider {

    override fun <T : Any> create(
        name: String,
        serializer: KSerializer<T>,
        defaultData: () -> T
    ): PersistedStorage<T> {
        val generic = GenericPersistedStorage(
            name = name,
            factory = platformProviders.datastoreFactory(),
            defaultData = {
                val default = defaultData()
                json.encodeToString(serializer, default)
            }
        )

        val mappedFlow = generic.state.map { serialized ->
            json.decodeFromString(serializer, serialized)
        }.shareIn(generic.scope, SharingStarted.Eagerly, replay = 1)

        return object : PersistedStorage<T> {
            override suspend fun initialize() {
                generic.ensurePopulated()
            }

            override fun flow(): SharedFlow<T> = mappedFlow

            override suspend fun clear() {
                generic.clear()
            }

            override suspend fun update(block: suspend T.() -> T) {
                generic.update {
                    val current = json.decodeFromString(serializer, this)
                    val updated = block(current)
                    json.encodeToString(serializer, updated)
                }
            }
        }
    }
}
