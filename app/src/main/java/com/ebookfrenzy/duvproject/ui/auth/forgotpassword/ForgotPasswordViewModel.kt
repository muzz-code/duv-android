package com.ebookfrenzy.duvproject.ui.auth.forgotpassword

import androidx.annotation.StringRes
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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(
    @StringRes val emailError: Int? = null,
    val isLoading: Boolean = false,
)

sealed interface ForgotPasswordEvent {
    /** The reset link was requested; show [message] and go back to login. */
    data class LinkSent(@StringRes val message: Int) : ForgotPasswordEvent
    data class ShowMessage(val message: String) : ForgotPasswordEvent
    data object NavigateBack : ForgotPasswordEvent
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<ForgotPasswordEvent>(Channel.BUFFERED)
    val events: Flow<ForgotPasswordEvent> = _events.receiveAsFlow()

    fun onSendClicked(email: String) {
        if (_uiState.value.isLoading) return
        val trimmed = email.trim()
        val error = when {
            trimmed.isEmpty() -> R.string.email_cannot_be_empty
            !Validator.emailValidate(trimmed) -> R.string.invalid_email
            else -> null
        }
        _uiState.value = ForgotPasswordUiState(emailError = error, isLoading = error == null)
        if (error != null) return

        viewModelScope.launch {
            val result = authRepository.forgotPassword(trimmed)
            _uiState.value = ForgotPasswordUiState()
            // The backend answers the same way whether or not the email exists, so accounts can't be probed.
            _events.send(
                when (result) {
                    is ApiResult.Success -> ForgotPasswordEvent.LinkSent(R.string.reset_link_sent)
                    is ApiResult.Failure -> ForgotPasswordEvent.ShowMessage(result.message)
                },
            )
        }
    }

    fun onBackToLoginClicked() {
        viewModelScope.launch { _events.send(ForgotPasswordEvent.NavigateBack) }
    }
}
