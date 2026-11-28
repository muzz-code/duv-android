package com.ebookfrenzy.duvproject.ui.auth.signup

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.remote.ApiResult
import com.ebookfrenzy.duvproject.data.repository.AccountCategory
import com.ebookfrenzy.duvproject.data.repository.AuthRepository
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

/** Holds the sign-up validation rules and creates the account; the fragment only displays [uiState]. */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _events = Channel<SignUpEvent>(Channel.BUFFERED)
    val events: Flow<SignUpEvent> = _events.receiveAsFlow()

    private var password = ""

    // Live validation while the user types (each field is checked as it changes).
    fun onEmailChanged(email: String) =
        _uiState.update { it.copy(emailError = emailError(email), serverFieldErrors = it.serverFieldErrors - "email") }

    fun onPhoneChanged(phone: String) =
        _uiState.update { it.copy(phoneError = phoneError(phone), serverFieldErrors = it.serverFieldErrors - "phone") }

    fun onPasswordChanged(value: String) {
        password = value
        _uiState.update { it.copy(passwordError = passwordError(value)) }
    }

    fun onConfirmPasswordChanged(value: String) =
        _uiState.update { it.copy(confirmPasswordError = confirmPasswordError(password, value)) }

    fun onCategoryFieldClicked() = send(SignUpEvent.ChooseCategory)

    fun onCategoryChosen(category: AccountCategory) =
        _uiState.update { it.copy(category = category, categoryError = null) }

    fun onSignUpClicked(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
    ) {
        if (_uiState.value.isLoading) return
        this.password = password
        val checked = _uiState.value.copy(
            firstNameError = if (firstName.isBlank()) R.string.first_name_cannot_be_empty else null,
            lastNameError = if (lastName.isBlank()) R.string.last_name_cannot_be_empty else null,
            emailError = emailError(email),
            phoneError = phoneError(phone),
            passwordError = passwordError(password),
            confirmPasswordError = confirmPasswordError(password, confirmPassword),
            categoryError = if (_uiState.value.category == null) R.string.please_choose_a_category else null,
            serverFieldErrors = emptyMap(),
        )
        _uiState.value = checked
        val category = checked.category
        if (!checked.isValid || category == null) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = authRepository.register(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim(),
                phone = normalizePhone(phone),
                category = category,
                password = password,
            )
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(SignUpEvent.ShowAccountCreated(result.data.isEntertainer))
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, serverFieldErrors = result.fieldErrors) }
                    _events.send(SignUpEvent.ShowMessage(result.message))
                }
            }
        }
    }

    fun onLoginLinkClicked() = send(SignUpEvent.NavigateToLogin)

    private fun send(event: SignUpEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    /** "0803 123 4567" → "08031234567": the hint shows spaces, the backend does not accept them. */
    private fun normalizePhone(phone: String) = phone.filterNot { it.isWhitespace() || it == '-' }

    @StringRes
    private fun emailError(email: String): Int? = when {
        email.isBlank() -> R.string.email_cannot_be_empty
        !Validator.emailValidate(email.trim()) -> R.string.invalid_email
        else -> null
    }

    @StringRes
    private fun phoneError(phone: String): Int? = when {
        phone.isBlank() -> R.string.phone_number_cannot_be_empty
        !Validator.mobileValidate(normalizePhone(phone)) -> R.string.invalid_phone_number
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
