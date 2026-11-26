package com.ebookfrenzy.duvproject.ui.auth.signup

import androidx.annotation.StringRes
import com.ebookfrenzy.duvproject.data.repository.AccountCategory

/**
 * Error message (as a string resource) to show under each field, or null when the field is fine.
 * [serverFieldErrors] holds messages the backend returned for specific fields (e.g. "Email already registered").
 */
data class SignUpUiState(
    @StringRes val firstNameError: Int? = null,
    @StringRes val lastNameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val phoneError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    @StringRes val categoryError: Int? = null,
    val serverFieldErrors: Map<String, String> = emptyMap(),
    /** Picked in the "Choose category" field: USER to hire, or DJ / MC / LIVE_BAND for entertainers. */
    val category: AccountCategory? = null,
    val isLoading: Boolean = false,
) {
    val isValid: Boolean
        get() = listOf(firstNameError, lastNameError, emailError, phoneError, passwordError, confirmPasswordError, categoryError)
            .all { it == null }
}

/** One-off actions for the sign-up screen. */
sealed interface SignUpEvent {
    data class ShowMessage(val message: String) : SignUpEvent
    /** Show the category list (User, DJ, MC, Live band). */
    data object ChooseCategory : SignUpEvent
    data class NavigateToHome(val isEntertainer: Boolean) : SignUpEvent
    /** The "Login" link was tapped. */
    data object NavigateToLogin : SignUpEvent
}
