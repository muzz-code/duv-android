package com.ebookfrenzy.duvproject.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebookfrenzy.duvproject.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drawer → Logout, shared by the user and entertainer home screens. */
@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loggedOut = Channel<Unit>(Channel.BUFFERED)
    /** Emits once the session is cleared; the screen then opens the login screens. */
    val loggedOut: Flow<Unit> = _loggedOut.receiveAsFlow()

    fun onLogoutClicked() {
        if (_isLoading.value) return
        _isLoading.value = true
        viewModelScope.launch {
            authRepository.logout()
            _isLoading.value = false
            _loggedOut.send(Unit)
        }
    }
}
