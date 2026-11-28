package com.ebookfrenzy.duvproject.data.remote.dto

/** JSON bodies for /api/v1/auth. Field names match the backend's records exactly. */

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val category: String,
    val password: String,
    val referralCode: String? = null,
)

data class LoginRequest(val email: String, val password: String)

data class RefreshTokenRequest(val refreshToken: String)

data class ForgotPasswordRequest(val email: String)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserDto,
)

data class UserDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val signupCategory: String?,
    val roles: List<String>,
    val emailVerified: Boolean,
)

/** The backend's single error format (ApiError). */
data class ApiErrorDto(
    val status: Int? = null,
    val code: String? = null,
    val message: String? = null,
    val fieldErrors: List<FieldErrorDto>? = null,
)

data class FieldErrorDto(val field: String, val message: String)
