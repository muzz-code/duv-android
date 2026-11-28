package com.ebookfrenzy.duvproject.data.remote

import com.ebookfrenzy.duvproject.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Adds "Authorization: Bearer <access token>" to every request except the public /auth endpoints. */
class AuthInterceptor @Inject constructor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenStore.accessToken
        if (token == null || request.url.encodedPath.startsWith(AUTH_PATH)) {
            return chain.proceed(request)
        }
        return chain.proceed(request.newBuilder().header("Authorization", "Bearer $token").build())
    }

    companion object {
        const val AUTH_PATH = "/api/v1/auth/"
    }
}
