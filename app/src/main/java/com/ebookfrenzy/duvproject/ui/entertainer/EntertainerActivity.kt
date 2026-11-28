package com.ebookfrenzy.duvproject.ui.entertainer

import com.ebookfrenzy.duvproject.ui.common.LoadingOverlay
import com.ebookfrenzy.duvproject.ui.common.LogoutViewModel
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.openLogin
import com.ebookfrenzy.duvproject.ui.common.setStatusBar
import com.ebookfrenzy.duvproject.ui.user.UserAccountActivity
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDialogFragment
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDialogFragment.Step

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.ActivityUserBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntertainerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityUserBinding
    private val logoutViewModel: LogoutViewModel by viewModels()
    private val loader by lazy { LoadingOverlay(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set status bar color
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.statusBarColor = resources?.getColor(R.color.white)!!
        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarUser.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navController = findNavController(R.id.nav_host_fragment_content_user)
        setUpDrawer(drawerLayout)

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        actionBarDrawerToggle.drawerArrowDrawable.color = resources?.getColor(R.color.black)!!

        appBarConfiguration = AppBarConfiguration(
            navController.graph,
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        // The set-up forms draw their own Figma header (back arrow + title), so the app bar steps aside there.
        val ownHeader = setOf(R.id.entertainerProfileFragment, R.id.emergencyContactFragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showAppBar = destination.id !in ownHeader
            binding.appBarUser.appBarLayout.isVisible = showAppBar
            // The scrolling-view behaviour keeps the old app-bar offset unless the content is reset too.
            binding.appBarUser.contentUser.root.translationY = 0f
            binding.appBarUser.contentUser.root.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                behavior = if (showAppBar) AppBarLayout.ScrollingViewBehavior() else null
            }
        }
//        navView.setupWithNavController(navController)

        collectWhenStarted(logoutViewModel.isLoading) { loader.setLoading(it) }
        collectWhenStarted(logoutViewModel.loggedOut) { openLogin() }
    }

    /** Drawer built from the Figma "Entertainer's Account Setup > Menu" frame (layout/drawer_entertainer.xml). */
    private fun setUpDrawer(drawerLayout: DrawerLayout) = with(binding.drawer) {
        val navController = findNavController(R.id.nav_host_fragment_content_user)
        fun closeAnd(action: () -> Unit) = View.OnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            action()
        }
        drawerClose.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }
        drawerDashboard.setOnClickListener(closeAnd { navController.navigate(R.id.dashboardFragment) })
        drawerEntertainerProfile.setOnClickListener(closeAnd { navController.navigate(R.id.entertainerProfileFragment) })
        drawerBank.setOnClickListener(closeAnd { SetupDialogFragment.show(supportFragmentManager, Step.BANK) })
        drawerEmergency.setOnClickListener(closeAnd { navController.navigate(R.id.emergencyContactFragment) })
        drawerYoutube.setOnClickListener(closeAnd { SetupDialogFragment.show(supportFragmentManager, Step.YOUTUBE) })
        drawerIdentification.setOnClickListener(closeAnd {
            SetupDialogFragment.show(supportFragmentManager, Step.IDENTIFICATION)
        })
        drawerSwitch.setOnClickListener(closeAnd {
            startActivity(Intent(this@EntertainerActivity, UserAccountActivity::class.java))
        })
        drawerLogout.setOnClickListener(closeAnd { logoutViewModel.onLogoutClicked() })
        // Not built yet: Edit Profile, Invite Friends. They just close the drawer for now.
        listOf(drawerEditProfile, drawerInvite).forEach { it.setOnClickListener(closeAnd {}) }
    }

    override fun onDestroy() {
        loader.hide()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_user)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        setStatusBar(R.color.white, darkIcons = true)
    }

}
