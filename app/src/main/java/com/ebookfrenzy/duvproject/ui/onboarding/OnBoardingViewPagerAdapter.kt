package com.ebookfrenzy.duvproject.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ebookfrenzy.duvproject.data.model.OnBoardingData
import com.ebookfrenzy.duvproject.databinding.OnBoardingScreenBinding

/** Shows each [OnBoardingData] page. Pages come from [OnBoardingViewModel]. */
class OnBoardingViewPagerAdapter : PagerAdapter() {

    private var pages: List<OnBoardingData> = emptyList()

    fun submitPages(newPages: List<OnBoardingData>) {
        pages = newPages
        notifyDataSetChanged()
    }

    override fun getCount(): Int = pages.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = OnBoardingScreenBinding.inflate(LayoutInflater.from(container.context), container, false)
        val page = pages[position]
        binding.findEventFragmentImage.setImageResource(page.imageRes)
        binding.findEventFragmentFindAnEventTextView.text = page.title
        binding.findEventFragmentDummyTextView.text = page.desc
        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
