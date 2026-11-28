package com.ebookfrenzy.duvproject.data.repository

import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.model.OnBoardingData
import javax.inject.Inject

/** Onboarding pages bundled with the app (previously `util.ResourceDummyData`). */
class LocalOnBoardingRepository @Inject constructor() : OnBoardingRepository {

    override fun getOnBoardingPages(): List<OnBoardingData> = listOf(
        OnBoardingData(
            "Find an event?",
            "duv live is the perfect place where you can attend nice events",
            R.drawable.date_picker
        ),
        OnBoardingData(
            "Need a DJ anytime and anywhere",
            "duv live is the perfect place where you can attend nice events",
            R.drawable.headphone_bro_image
        ),
        OnBoardingData(
            "Need a MC anytime and anywhere",
            "duv live is the perfect place where you can attend nice events",
            R.drawable.music_festival_image
        ),
        OnBoardingData(
            "Need a Live band anytime and anywhere",
            "duv live is the perfect place where you can attend nice events",
            R.drawable.rock_band_image
        ),
    )
}
