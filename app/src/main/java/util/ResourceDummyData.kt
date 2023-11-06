package util

import com.ebookfrenzy.duvproject.R
import data.OnBoardingData
import java.util.ArrayList

object ResourceDummyData {

    fun listOfOnBoardingData(): MutableList<OnBoardingData> {
        val onBoardingData: MutableList<OnBoardingData> = ArrayList()
        onBoardingData.add(OnBoardingData("Find an event?", "duv live is the perfect place where you can attend nice events", R.drawable.date_picker))
        onBoardingData.add(OnBoardingData("Need a DJ anytime and anywhere", "duv live is the perfect place where you can attend nice events", R.drawable.headphone_bro_image))
        onBoardingData.add(OnBoardingData("Need a MC anytime and anywhere", "duv live is the perfect place where you can attend nice events", R.drawable.music_festival_image))
        onBoardingData.add(OnBoardingData("Need a Live band anytime and anywhere", "duv live is the perfect place where you can attend nice events", R.drawable.rock_band_image))
        return onBoardingData
    }
}
