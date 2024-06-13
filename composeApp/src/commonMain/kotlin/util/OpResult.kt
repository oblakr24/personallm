package util
interface RootError

sealed interface OpResult<out T : Any?, out E : RootError> {
    data class Done<out T : Any, out E : RootError>(val data: T) : OpResult<T, E>
    data class Error<out E : RootError>(val error: E) : OpResult<Nothing, E>

    fun <R : Any> map(mapper: (T) -> R): OpResult<R, E> = when (this) {
        is Done -> Done(mapper(data))
        is Error -> this
    }

    @Suppress("UNCHECKED_CAST")
    fun <R : Any, E: RootError> flatMap(mapper: (T) -> OpResult<R, E>): OpResult<R, E> = when (this) {
        is Done -> mapper(data)
        is Error -> this as OpResult<R, E>
    }

    @Suppress("UNCHECKED_CAST")
    fun <R : Any, E: RootError> flatMapNullable(mapper: (T) -> OpResult<R, E>?): OpResult<R, E>? = when (this) {
        is Done -> mapper(data)
        is Error -> this as OpResult<R, E>
    }

    fun optValue() = when (this) {
        is Done -> data
        is Error -> null
    }

    fun doOnError(block: (error: Error<E>) -> Unit): OpResult<T, E> {
        when (this) {
            is Done -> Unit
            is Error -> block(this)
        }
        return this
    }

    fun doOnSuccess(block: (data: T) -> Unit): OpResult<T, E> {
        when (this) {
            is Done -> block(data)
            is Error -> Unit
        }
        return this
    }

    suspend fun doOnSuccessSusp(block: suspend (data: T) -> Unit): OpResult<T, E> {
        when (this) {
            is Done -> block(data)
            is Error -> Unit
        }
        return this
    }
}