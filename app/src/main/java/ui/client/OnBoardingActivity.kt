package ui.client

import Adapter.OnBoardingViewPagerAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.OnBoardingActivityBinding
import com.google.android.material.tabs.TabLayout
import data.OnBoardingData
import ui.AuthenticationActivity
import util.ResourceDummyData

class OnBoardingActivity : AppCompatActivity() {
    private lateinit var skipClick: TextView
    private lateinit var sliderDot: TabLayout

    private lateinit var binding: OnBoardingActivityBinding

    /* instantiate variables null variables for my
    view pager adapter, tab layout and view pager*/

    var onBoardingViewPagerAdapter: OnBoardingViewPagerAdapter? = null
    var onBoardingTabLayout: TabLayout? = null
    var onBoardingViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_DuvProject)
        binding = OnBoardingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initializing the tabLayout
        onBoardingTabLayout = binding.onBoardingActivityTabLayout
        skipClick = binding.onBoardingActivitySkipTextView

        skipClick.setOnClickListener {
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
        }

        /* create a reference to my object class to pass
         in the data to my data to my view pager adapter class locally*/
        val onBoardingData = ResourceDummyData.listOfOnBoardingData()
        setOnBoardingViewPagerAdapter(onBoardingData)

        supportActionBar?.hide()
    }

    // create a function outside the onCreate to bind the variables to the actual views
    private fun setOnBoardingViewPagerAdapter(onBoardingData: List<OnBoardingData>) {
        sliderDot = binding.onBoardingActivityTabLayout
        onBoardingViewPager = binding.screenPager
        onBoardingViewPagerAdapter = OnBoardingViewPagerAdapter(this, onBoardingData)
        onBoardingViewPager!!.adapter = onBoardingViewPagerAdapter
        onBoardingTabLayout?.setupWithViewPager(onBoardingViewPager)
        sliderDot.setupWithViewPager(onBoardingViewPager, true)
    }
}
