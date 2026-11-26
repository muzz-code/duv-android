package com.ebookfrenzy.duvproject.data.remote

import com.ebookfrenzy.duvproject.data.remote.dto.ApiErrorDto
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Outcome of a backend call, so ViewModels never deal with exceptions or HTTP details. */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    /**
     * [message] is ready to show to the user. [fieldErrors] maps a request field name (e.g. "email")
     * to the backend's message for it, when the backend rejected specific fields.
     */
    data class Failure(
        val message: String,
        val fieldErrors: Map<String, String> = emptyMap(),
        val isNetworkError: Boolean = false,
    ) : ApiResult<Nothing>
}

/** Runs [block] and turns any HTTP or connection error into [ApiResult.Failure]. */
suspend fun <T> apiCall(gson: Gson, block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    val error = e.response()?.errorBody()?.charStream()?.use { reader ->
        runCatching { gson.fromJson(reader, ApiErrorDto::class.java) }.getOrNull()
    }
    ApiResult.Failure(
        message = error?.message?.takeIf { it.isNotBlank() } ?: "Something went wrong (${e.code()}). Please try again.",
        fieldErrors = error?.fieldErrors.orEmpty().associate { it.field to it.message },
    )
} catch (e: IOException) {
    ApiResult.Failure(
        message = "Can't reach the DUV server. Check your internet connection and try again.",
        isNetworkError = true,
    )
}

/** For endpoints declared as Response<Unit>: turns an error status into the same HttpException Retrofit throws. */
fun Response<*>.requireSuccess() {
    if (!isSuccessful) throw HttpException(this)
}
