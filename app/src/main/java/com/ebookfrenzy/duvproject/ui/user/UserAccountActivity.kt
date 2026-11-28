package com.ebookfrenzy.duvproject.ui.user

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.ActivityUserAccountBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ebookfrenzy.duvproject.ui.common.LoadingOverlay
import com.ebookfrenzy.duvproject.ui.common.LogoutViewModel
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.openLogin
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAccountBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var listener: NavController.OnDestinationChangedListener
    private val logoutViewModel: LogoutViewModel by viewModels()
    private val loader by lazy { LoadingOverlay(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set status bar color
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.statusBarColor = resources?.getColor(com.ebookfrenzy.duvproject.R.color.white)!!
        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


        binding = ActivityUserAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.userAccountToolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val bottomNavView: BottomNavigationView = binding.navigationView
        val navController: NavController =
            findNavController(R.id.nav_host_fragment_activity_user_account)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.hamburger_menu)

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavView.setupWithNavController(navController)
        setUpDrawer(drawerLayout, navController)

        collectWhenStarted(logoutViewModel.isLoading) { loader.setLoading(it) }
        collectWhenStarted(logoutViewModel.loggedOut) { openLogin() }
    }

    /** Drawer built from the Figma "User > Menu" frame (layout/drawer_user.xml). */
    private fun setUpDrawer(drawerLayout: DrawerLayout, navController: NavController) = with(binding.drawer) {
        fun closeAnd(action: () -> Unit) = View.OnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            action()
        }
        drawerClose.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }
        drawerDashboard.setOnClickListener(closeAnd { navController.navigate(R.id.navigation_home) })
        drawerAuctions.setOnClickListener(closeAnd { navController.navigate(R.id.auctionsFragment2) })
        drawerRequests.setOnClickListener(closeAnd { navController.navigate(R.id.requestsFragment) })
        drawerMyEvents.setOnClickListener(closeAnd { navController.navigate(R.id.myEventsFragment) })
        drawerLogout.setOnClickListener(closeAnd { logoutViewModel.onLogoutClicked() })
        // Not built yet: Notifications, Hire Entertainers, Payments History, Leave a review, Public Events,
        // Switch account, Edit Profile, Invite Friends. They just close the drawer for now.
        listOf(drawerNotifications, drawerHire, drawerPayments, drawerReview, drawerPublicEvents,
            drawerSwitch, drawerEditProfile, drawerInvite).forEach { it.setOnClickListener(closeAnd {}) }
    }

    override fun onDestroy() {
        loader.hide()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_user_account)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

//    fun onDestinationChangedListener(){
//        listener = NavController.OnDestinationChangedListener{controller, destination, arguments -> {
//            when(destination.id) {
//                R.id.dashboardFragment -> {
//
//                }
//            }
//
//            }
//        })
//    }

}