package data

import util.OpResult
import util.RootError

sealed interface NetworkError : RootError {
    data class Error(val ex: Throwable) : NetworkError
    data class NotSuccessful(val body: String, val code: Int?, val type: String?, val codeString: String?) : NetworkError {
        fun additionalInfo(): String {
            val sb = StringBuilder()
            val codeOrCodeString = code?.toString() ?: codeString
            if (codeOrCodeString != null) {
                sb.append("Code: $codeOrCodeString\n")
            }
            if (type != null) {
                sb.append("Type: $type")
            }
            return sb.toString()
        }
    }
    data object NoData : NetworkError
}

fun NetworkError.combinedMessage(): String {
    return when (this) {
        is NetworkError.Error -> ex.message ?: "Error"
        NetworkError.NoData -> "No data"
        is NetworkError.NotSuccessful -> {
            val sb = StringBuilder()
            sb.append(body)
            sb.append("\n")
            sb.append(additionalInfo())
            sb.toString()
        }
    }
}

typealias NetworkResponse<T> = OpResult<T, NetworkError>

object NetworkResp {
    fun <T : Any> success(data: T): NetworkResponse<T> = OpResult.Done(data)
    fun error(type: NetworkError) = OpResult.Error(type)
}
