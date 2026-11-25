package com.ebookfrenzy.duvproject.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.ui.onboarding.OnBoardingActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_activity)

        // lifecycleScope (instead of GlobalScope) cancels the timer if this screen is destroyed.
        lifecycleScope.launch {
            delay(SPLASH_DURATION_MS)
            startActivity(Intent(this@SplashScreenActivity, OnBoardingActivity::class.java))
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 1000L
    }
}
