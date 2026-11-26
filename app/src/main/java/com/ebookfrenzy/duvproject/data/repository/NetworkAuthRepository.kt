package com.ebookfrenzy.duvproject.data.repository

import com.ebookfrenzy.duvproject.data.local.TokenStore
import com.ebookfrenzy.duvproject.data.remote.ApiResult
import com.ebookfrenzy.duvproject.data.remote.AuthApi
import com.ebookfrenzy.duvproject.data.remote.apiCall
import com.ebookfrenzy.duvproject.data.remote.requireSuccess
import com.ebookfrenzy.duvproject.data.remote.dto.AuthResponse
import com.ebookfrenzy.duvproject.data.remote.dto.ForgotPasswordRequest
import com.ebookfrenzy.duvproject.data.remote.dto.LoginRequest
import com.ebookfrenzy.duvproject.data.remote.dto.RefreshTokenRequest
import com.ebookfrenzy.duvproject.data.remote.dto.RegisterRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NetworkAuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val gson: Gson,
) : AuthRepository {

    override val isLoggedIn: Boolean get() = tokenStore.isLoggedIn
    override val isEntertainer: Boolean get() = tokenStore.isEntertainer

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        category: AccountCategory,
        password: String,
    ): ApiResult<Session> = signIn {
        api.register(RegisterRequest(firstName, lastName, email, phone, category.name, password))
    }

    override suspend fun login(email: String, password: String): ApiResult<Session> =
        signIn { api.login(LoginRequest(email, password)) }

    override suspend fun forgotPassword(email: String): ApiResult<Unit> =
        apiCall(gson) { api.forgotPassword(ForgotPasswordRequest(email)).requireSuccess() }

    override suspend fun logout() {
        tokenStore.refreshToken?.let { token -> apiCall(gson) { api.logout(RefreshTokenRequest(token)).requireSuccess() } }
        withContext(Dispatchers.IO) { tokenStore.clear() }
    }

    private suspend fun signIn(call: suspend () -> AuthResponse): ApiResult<Session> =
        when (val result = apiCall(gson, call)) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                withContext(Dispatchers.IO) { tokenStore.save(result.data) }
                ApiResult.Success(Session(result.data.user.firstName, tokenStore.isEntertainer))
            }
        }
}
