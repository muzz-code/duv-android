package com.ebookfrenzy.duvproject.data.repository

import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.model.OnBoardingData
import javax.inject.Inject

/** Onboarding pages bundled with the app (previously `util.ResourceDummyData`). */
class LocalOnBoardingRepository @Inject constructor() : OnBoardingRepository {

    override fun getOnBoardingPages(): List<OnBoardingData> = listOf(
        OnBoardingData(
            "Find events worth showing up for",
            "Discover concerts, parties and shows near you — and the DJs, MCs and bands that make them.",
            R.drawable.date_picker
        ),
        OnBoardingData(
            "Need a DJ? Book one in minutes",
            "Browse verified DJs by price, location and reviews, then send a booking request.",
            R.drawable.headphone_bro_image
        ),
        OnBoardingData(
            "An MC who keeps the party moving",
            "Compare MCs who've hosted weddings, birthdays and corporate events like yours.",
            R.drawable.music_festival_image
        ),
        OnBoardingData(
            "Live bands, any time, anywhere",
            "Hire a band for your next event — or run an auction and let them bid.",
            R.drawable.rock_band_image
        ),
    )
}
