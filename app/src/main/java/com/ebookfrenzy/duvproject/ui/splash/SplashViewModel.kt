package com.ebookfrenzy.duvproject.ui.splash

import androidx.lifecycle.ViewModel
import com.ebookfrenzy.duvproject.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed interface SplashDestination {
    /** Already logged in: skip onboarding and login. */
    data class Home(val isEntertainer: Boolean) : SplashDestination
    data object OnBoarding : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    fun destination(): SplashDestination =
        if (authRepository.isLoggedIn) SplashDestination.Home(authRepository.isEntertainer)
        else SplashDestination.OnBoarding
}
