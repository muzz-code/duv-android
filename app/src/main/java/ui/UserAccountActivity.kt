package ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
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
import com.google.android.material.navigation.NavigationView


class UserAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAccountBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var listener: NavController.OnDestinationChangedListener


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
        val navigationView: NavigationView = binding.navView
        val bottomNavView: BottomNavigationView = binding.navigationView
        navigationView.bringToFront()
        val navViewHeader = navigationView.getHeaderView(0)

        val profileText: TextView =
            navViewHeader.findViewById(R.id.entertainer_dashboard_nav_header_status_textview)
        profileText.visibility = View.GONE


        val navController: NavController =
            findNavController(R.id.nav_host_fragment_activity_user_account)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_event, R.id.nav_settings
//            )
//        )
        appBarConfiguration = AppBarConfiguration(
            navController.graph,
            drawerLayout
        )
        supportActionBar?.setHomeAsUpIndicator(R.drawable.hamburger_menu);


        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavView.setupWithNavController(navController)
        navigationView.setupWithNavController(navController)

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.dashboardFragment -> {
                    findNavController(R.id.nav_host_fragment_activity_user_account).navigate(
                        R.id.navigation_home)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }

                R.id.auctionsFragment -> {
                    findNavController(R.id.nav_host_fragment_activity_user_account).navigate(
                        R.id.auctionsFragment2)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.requestsFragment -> {
                    findNavController(R.id.nav_host_fragment_activity_user_account).navigate(R.id.requestsFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.myEventsFragment -> {
                    findNavController(R.id.nav_host_fragment_activity_user_account).navigate(R.id.myEventsFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }
                else -> return@setNavigationItemSelectedListener true
            }
        }
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