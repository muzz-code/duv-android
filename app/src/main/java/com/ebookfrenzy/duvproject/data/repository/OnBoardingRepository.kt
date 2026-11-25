package com.ebookfrenzy.duvproject.data.repository

import com.ebookfrenzy.duvproject.data.model.OnBoardingData

/** Source of the onboarding pages. Swap the implementation to load them from an API later. */
interface OnBoardingRepository {
    fun getOnBoardingPages(): List<OnBoardingData>
}
