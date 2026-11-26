package com.ebookfrenzy.duvproject.data.remote

import com.ebookfrenzy.duvproject.data.remote.dto.AuthResponse
import com.ebookfrenzy.duvproject.data.remote.dto.ForgotPasswordRequest
import com.ebookfrenzy.duvproject.data.remote.dto.LoginRequest
import com.ebookfrenzy.duvproject.data.remote.dto.RefreshTokenRequest
import com.ebookfrenzy.duvproject.data.remote.dto.RegisterRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** DUV backend authentication endpoints (see the backend's AuthController). */
interface AuthApi {

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    // These two reply with no body (204/202). Retrofit 2.9 crashes on an empty body for a suspend
    // function returning Unit, so they return Response; call requireSuccess() on the result.
    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: RefreshTokenRequest): Response<Unit>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<Unit>

    /** Blocking on purpose: called from OkHttp's [TokenAuthenticator], which already runs on a background thread. */
    @POST("api/v1/auth/refresh")
    fun refresh(@Body body: RefreshTokenRequest): Call<AuthResponse>
}
