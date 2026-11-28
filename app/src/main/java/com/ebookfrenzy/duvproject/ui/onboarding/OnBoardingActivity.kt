package com.ebookfrenzy.duvproject.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.OnBoardingActivityBinding
import com.ebookfrenzy.duvproject.ui.auth.AuthenticationActivity
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: OnBoardingActivityBinding
    private val viewModel: OnBoardingViewModel by viewModels()
    private val pagerAdapter = OnBoardingViewPagerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_DuvProject)
        binding = OnBoardingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.screenPager.adapter = pagerAdapter
        binding.onBoardingActivityTabLayout.setupWithViewPager(binding.screenPager, true)

        binding.onBoardingActivitySkipTextView.setOnClickListener {
            startActivity(Intent(this, AuthenticationActivity::class.java))
        }

        collectWhenStarted(viewModel.pages) { pages -> pagerAdapter.submitPages(pages) }
    }
}
