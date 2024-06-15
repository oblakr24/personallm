package data

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import util.OpResult

class OpResultResponseConverterFactory : Converter.Factory {

    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *>? {
        if (typeData.typeInfo.type == OpResult::class) {
            return object : Converter.SuspendResponseConverter<HttpResponse, Any> {

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
