package com.ebookfrenzy.duvproject.data.remote

import com.ebookfrenzy.duvproject.data.local.TokenStore
import com.ebookfrenzy.duvproject.data.remote.dto.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

/**
 * Access tokens last 15 minutes. When a call comes back 401, swap the refresh token for a new pair
 * (the backend rotates it) and retry once. If refreshing fails the session is over, so clear it.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    // Provider + a separate client without this authenticator, so refreshing can never loop.
    @Named(REFRESH_API) private val refreshApi: Provider<AuthApi>,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        if (request.url.encodedPath.startsWith(AuthInterceptor.AUTH_PATH)) return null
        if (response.priorResponse != null) return null // already retried once

        synchronized(this) {
            val sentToken = request.header("Authorization")?.removePrefix("Bearer ")
            val current = tokenStore.accessToken
            // Another thread refreshed while this call was in flight: just retry with the new token.
            if (current != null && current != sentToken) return request.withToken(current)

            val refreshToken = tokenStore.refreshToken ?: return null
            val refreshed = runCatching {
                refreshApi.get().refresh(RefreshTokenRequest(refreshToken)).execute()
            }.getOrNull()

            val body = refreshed?.body()
            if (refreshed?.isSuccessful == true && body != null) {
                tokenStore.save(body)
                return request.withToken(body.accessToken)
            }
            // 4xx means the refresh token is no longer valid; a network error keeps the session.
            if (refreshed != null && refreshed.code() in 400..499) tokenStore.clear()
            return null
        }
    }

    private fun Request.withToken(token: String) =
        newBuilder().header("Authorization", "Bearer $token").build()

    companion object {
        const val REFRESH_API = "refreshApi"
    }
}
