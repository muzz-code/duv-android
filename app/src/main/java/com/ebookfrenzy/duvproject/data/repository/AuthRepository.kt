package com.ebookfrenzy.duvproject.data.repository

import com.ebookfrenzy.duvproject.data.remote.ApiResult

/** The account categories the backend accepts when signing up (backend enum AccountCategory). */
enum class AccountCategory(val label: String) {
    USER("User (I want to hire)"),
    DJ("DJ"),
    MC("MC"),
    LIVE_BAND("Live band");

    companion object {
        val entertainerTypes = listOf(DJ, MC, LIVE_BAND)
    }

    val isEntertainer: Boolean get() = this != USER
}

/** Who is logged in, as far as the screens need to know. */
data class Session(val firstName: String, val isEntertainer: Boolean)

interface AuthRepository {
    val isLoggedIn: Boolean
    val isEntertainer: Boolean

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        category: AccountCategory,
        password: String,
    ): ApiResult<Session>

    suspend fun login(email: String, password: String): ApiResult<Session>

    suspend fun forgotPassword(email: String): ApiResult<Unit>

    /** Revokes the refresh token on the server (best effort) and always clears it on the device. */
    suspend fun logout()
}
