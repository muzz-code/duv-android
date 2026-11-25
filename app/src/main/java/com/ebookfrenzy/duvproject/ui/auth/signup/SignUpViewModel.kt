package com.ebookfrenzy.duvproject.ui.auth.signup

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.util.Validator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Holds the sign-up validation rules; the fragment only displays [uiState]. */
@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _events = Channel<SignUpEvent>(Channel.BUFFERED)
    val events: Flow<SignUpEvent> = _events.receiveAsFlow()

    private var password = ""
    private var confirmPassword = ""

    // Live validation while the user types (each field is checked as it changes).
    fun onEmailChanged(email: String) = _uiState.update { it.copy(emailError = emailError(email)) }

    fun onPhoneChanged(phone: String) = _uiState.update { it.copy(phoneError = phoneError(phone)) }

    fun onPasswordChanged(value: String) {
        password = value
        _uiState.update { it.copy(passwordError = passwordError(value)) }
    }

    fun onConfirmPasswordChanged(value: String) {
        confirmPassword = value
        _uiState.update { it.copy(confirmPasswordError = confirmPasswordError(password, value)) }
    }

    fun onSignUpClicked(email: String, phone: String, password: String, confirmPassword: String) {
        this.password = password
        this.confirmPassword = confirmPassword
        val state = SignUpUiState(
            emailError = emailError(email),
            phoneError = phoneError(phone),
            passwordError = passwordError(password),
            confirmPasswordError = confirmPasswordError(password, confirmPassword),
        )
        _uiState.value = state
        if (state.isValid) {
            // No backend yet, so a valid form just confirms, as before.
            send(SignUpEvent.ShowMessage(R.string.good_to_go))
        }
    }

    fun onLoginLinkClicked() = send(SignUpEvent.LoginLinkClicked)

    private fun send(event: SignUpEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    @StringRes
    private fun emailError(email: String): Int? = when {
        email.isBlank() -> R.string.email_cannot_be_empty
        !Validator.emailValidate(email.trim()) -> R.string.invalid_email
        else -> null
    }

    @StringRes
    private fun phoneError(phone: String): Int? = when {
        phone.isBlank() -> R.string.phone_number_cannot_be_empty
        !Validator.mobileValidate(phone.trim()) -> R.string.invalid_phone_number
        else -> null
    }

    @StringRes
    private fun passwordError(password: String): Int? = when {
        password.isBlank() -> R.string.pasword_cannot_be_empty
        !Validator.passwordValidate(password.trim()) -> R.string.minimum_of_5_xter
        else -> null
    }

    @StringRes
    private fun confirmPasswordError(password: String, confirm: String): Int? = when {
        confirm.isBlank() -> R.string.confirm_password_cannot_be_empty
        password.trim() != confirm.trim() -> R.string.password_does_not_match
        else -> null
    }
}
