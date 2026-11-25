package com.ebookfrenzy.duvproject.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onLoginClicked() {
        // No backend yet: login goes straight to the entertainer area, as before.
        // When an auth API exists, inject an AuthRepository here and call it.
        send(LoginEvent.NavigateToHome)
    }

    fun onForgotPasswordClicked() = send(LoginEvent.NavigateToForgotPassword)

    private fun send(event: LoginEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
