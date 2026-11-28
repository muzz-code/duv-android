package com.ebookfrenzy.duvproject.ui.onboarding

import androidx.lifecycle.ViewModel
import com.ebookfrenzy.duvproject.data.model.OnBoardingData
import com.ebookfrenzy.duvproject.data.repository.OnBoardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    repository: OnBoardingRepository,
) : ViewModel() {

    private val _pages = MutableStateFlow(repository.getOnBoardingPages())
    val pages: StateFlow<List<OnBoardingData>> = _pages.asStateFlow()
}
