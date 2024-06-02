package data

import util.OpResult
import util.RootError

sealed interface NetworkError : RootError {
    data class Error(val ex: Throwable) : NetworkError
    data class NotSuccessful(val body: String, val code: Int) : NetworkError
    data object NoData : NetworkError
}

typealias NetworkResponse<T> = OpResult<T, NetworkError>

object NetworkResp {
    fun <T : Any> success(data: T): NetworkResponse<T> = OpResult.Done(data)
    fun error(type: NetworkError) = OpResult.Error(type)
}
