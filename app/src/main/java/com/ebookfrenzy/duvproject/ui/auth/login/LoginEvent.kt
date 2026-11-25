package com.ebookfrenzy.duvproject.ui.auth.login

/** One-off actions the login screen must perform (navigation, messages). */
sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent
    data object NavigateToForgotPassword : LoginEvent
}
