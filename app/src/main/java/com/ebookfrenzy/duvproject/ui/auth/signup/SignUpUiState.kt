package com.ebookfrenzy.duvproject.ui.auth.signup

import androidx.annotation.StringRes

/** Error message (as a string resource) to show under each field, or null when the field is fine. */
data class SignUpUiState(
    @StringRes val emailError: Int? = null,
    @StringRes val phoneError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
) {
    val isValid: Boolean
        get() = listOf(emailError, phoneError, passwordError, confirmPasswordError).all { it == null }
}

/** One-off actions for the sign-up screen. */
sealed interface SignUpEvent {
    data class ShowMessage(@StringRes val message: Int) : SignUpEvent
    /** The "Login" link was tapped. */
    data object LoginLinkClicked : SignUpEvent
}
