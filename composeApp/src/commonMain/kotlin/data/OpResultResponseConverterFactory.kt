package data

import util.OpResult
import util.RootError
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess


class OpResultResponseConverterFactory : Converter.Factory {

    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *>? {
        if (typeData.typeInfo.type == OpResult::class) {
            return object : Converter.SuspendResponseConverter<HttpResponse, Any> {
                @Deprecated("Use convert(result: KtorfitResult)")
                override suspend fun convert(response: HttpResponse): Any {
                    return try {
                        if (response.status.isSuccess()) {
                            val resp =
                                OpResult.Done<Any, NetworkError>(response.body(typeData.typeArgs.first().typeInfo))
                            resp
                        } else {
                            OpResult.Error(
                                NetworkError.NotSuccessful(
                                    response.bodyAsText(),
                                    response.status.value
                                )
                            )
                        }

                    } catch (ex: Throwable) {
                        OpResult.Error(NetworkError.Error(ex))
                    }
                }

                override suspend fun convert(result: KtorfitResult): Any {
                    return when (result) {
                        is KtorfitResult.Failure -> OpResult.Error(NetworkError.Error(result.throwable))
                        is KtorfitResult.Success -> {
                            val response = result.response
                            if (response.status.isSuccess()) {
                                OpResult.Done(response.body(typeData.typeArgs.first().typeInfo))
                            } else {
                                OpResult.Error(
                                    NetworkError.NotSuccessful(
                                        response.bodyAsText(),
                                        response.status.value
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}
