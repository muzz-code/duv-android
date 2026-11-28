package com.ebookfrenzy.duvproject.data.model

import androidx.annotation.DrawableRes

/** One page of the onboarding carousel. */
data class OnBoardingData(
    val title: String,
    val desc: String,
    @DrawableRes val imageRes: Int,
)
