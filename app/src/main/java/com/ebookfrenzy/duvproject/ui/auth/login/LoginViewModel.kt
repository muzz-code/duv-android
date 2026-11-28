package com.ebookfrenzy.duvproject.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.remote.ApiResult
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

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onLoginClicked(email: String, password: String) {
        if (_uiState.value.isLoading) return
        val trimmedEmail = email.trim()
        val emailError = when {
            trimmedEmail.isEmpty() -> R.string.email_cannot_be_empty
            !Validator.emailValidate(trimmedEmail) -> R.string.invalid_email
            else -> null
        }
        val passwordError = if (password.isEmpty()) R.string.pasword_cannot_be_empty else null
        _uiState.value = LoginUiState(emailError = emailError, passwordError = passwordError)
        if (emailError != null || passwordError != null) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = authRepository.login(trimmedEmail, password)
            _uiState.update { it.copy(isLoading = false) }
            when (result) {
                is ApiResult.Success -> _events.send(LoginEvent.NavigateToHome(result.data.isEntertainer))
                is ApiResult.Failure -> _events.send(LoginEvent.ShowMessage(result.message))
            }
        }
    }

    fun onForgotPasswordClicked() = send(LoginEvent.NavigateToForgotPassword)

    fun onCreateAccountClicked() = send(LoginEvent.NavigateToSignUp)

    private fun send(event: LoginEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
