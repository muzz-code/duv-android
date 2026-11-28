package com.ebookfrenzy.duvproject.ui.auth.login

import androidx.annotation.StringRes

/** What the login screen shows: field errors and whether a request is running. */
data class LoginUiState(
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val isLoading: Boolean = false,
)

/** One-off actions the login screen must perform (navigation, messages). */
sealed interface LoginEvent {
    data class NavigateToHome(val isEntertainer: Boolean) : LoginEvent
    data object NavigateToForgotPassword : LoginEvent
    data object NavigateToSignUp : LoginEvent
    data class ShowMessage(val message: String) : LoginEvent
}
