package com.ebookfrenzy.duvproject.ui.auth

import com.ebookfrenzy.duvproject.data.remote.ApiResult
import com.ebookfrenzy.duvproject.data.repository.AccountCategory
import com.ebookfrenzy.duvproject.data.repository.AuthRepository
import com.ebookfrenzy.duvproject.data.repository.Session

class FakeAuthRepository(
    var result: ApiResult<Session> = ApiResult.Success(Session("Ada", isEntertainer = false)),
) : AuthRepository {
    override var isLoggedIn = false
    override var isEntertainer = false

    var lastLogin: Pair<String, String>? = null
    var lastRegisterCategory: AccountCategory? = null
    var lastRegisterPhone: String? = null
    var loggedOut = false

    override suspend fun register(
        firstName: String, lastName: String, email: String, phone: String,
        category: AccountCategory, password: String,
    ): ApiResult<Session> {
        lastRegisterCategory = category
        lastRegisterPhone = phone
        return result
    }

    override suspend fun login(email: String, password: String): ApiResult<Session> {
        lastLogin = email to password
        return result
    }

    override suspend fun forgotPassword(email: String): ApiResult<Unit> = ApiResult.Success(Unit)

    override suspend fun logout() {
        loggedOut = true
    }
}
