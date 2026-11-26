package com.ebookfrenzy.duvproject.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.ui.common.openHome
import com.ebookfrenzy.duvproject.ui.onboarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_activity)

        // lifecycleScope (instead of GlobalScope) cancels the timer if this screen is destroyed.
        lifecycleScope.launch {
            delay(SPLASH_DURATION_MS)
            when (val destination = viewModel.destination()) {
                is SplashDestination.Home -> openHome(destination.isEntertainer)
                SplashDestination.OnBoarding ->
                    startActivity(Intent(this@SplashScreenActivity, OnBoardingActivity::class.java))
            }
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 1000L
    }
}
