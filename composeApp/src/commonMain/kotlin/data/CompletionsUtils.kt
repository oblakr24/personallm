package data

import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json


fun HttpStatement.toStreamingFlow(): Flow<NetworkResponse<String>> = flow {
    execute { httpResponse ->
        val channel = httpResponse.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line()
            if (!line.isNullOrBlank()) {
                if (line.contains("[DONE]")) {
                    println("Done!")
                } else {
                    emit(NetworkResp.success(line))
                }
            } else if (line == null) {
                NetworkResp.error(NetworkError.NoData)
            } else {
                println("Blank line")
            }
        }
    }
}

inline fun <reified TData: Any>Flow<NetworkResponse<String>>.parseToResponse(crossinline mapper: (json: String) -> TData?, crossinline errorMapper: (body: String) -> NetworkError): Flow<NetworkResponse<TData>> {
    val partialResponse = StringBuilder()
    val emissions = this.mapNotNull {
        it.flatMapNullable { jsonString ->
            try {
                val mappedResp = mapper(jsonString)
                if (mappedResp != null) {
                    NetworkResp.success(mappedResp)
                } else {
                    partialResponse.append(jsonString)
                    null
                }
            } catch (e: Throwable) {
                println("Could not parse: $jsonString because of ${e.message}")
                NetworkResp.error(NetworkError.Error(e))
            }
        }
    }
    return flow {
        emitAll(emissions)
        if (partialResponse.isNotBlank()) {
            val body = partialResponse.toString()
            val errorResponse = try {
                NetworkResp.error(
                    errorMapper(body)
                )
            } catch (e: Throwable) {
                println("Could not parse: $body because of ${e.message}")
                NetworkResp.error(NetworkError.Error(e))
            }
            emit(errorResponse)
        }
    }
}