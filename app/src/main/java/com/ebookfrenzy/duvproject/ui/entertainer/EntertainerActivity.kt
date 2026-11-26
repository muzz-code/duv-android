package com.ebookfrenzy.duvproject.ui.entertainer

import com.ebookfrenzy.duvproject.ui.common.LoadingOverlay
import com.ebookfrenzy.duvproject.ui.common.LogoutViewModel
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.openLogin
import com.ebookfrenzy.duvproject.ui.common.setStatusBar
import com.ebookfrenzy.duvproject.ui.user.UserAccountActivity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.ActivityUserBinding
import com.google.android.material.navigation.NavigationView
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
        val navView: NavigationView = binding.navView
        navView.bringToFront()
        val navController = findNavController(R.id.nav_host_fragment_content_user)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        // inflating an alert dialog on the bank_details_item
//        val bankDetails = findViewById<Button>(R.id.entertainer_dashboard_continue_btn)

        navView.setNavigationItemSelectedListener { it ->
//           showDialog()
//            Toast.makeText(this, "e work", Toast.LENGTH_SHORT).show()

            when (it.itemId) {
                R.id.dashboardFragment -> {
                    findNavController(R.id.nav_host_fragment_content_user).navigate(R.id.dashboardFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.entertainerProfileFragment -> {
                    findNavController(R.id.nav_host_fragment_content_user).navigate(R.id.entertainerProfileFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_bank_account_details -> {
                    Toast.makeText(this, "Bank Account Details", Toast.LENGTH_SHORT).show()
                    showDialog()
                    return@setNavigationItemSelectedListener true
                }
                R.id.emergencyContactFragment -> {
                    Toast.makeText(this, "Emergency Contact", Toast.LENGTH_SHORT).show()
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_youtube_channel -> {
                    Toast.makeText(this, "youtube Channel", Toast.LENGTH_SHORT).show()
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_logout -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    logoutViewModel.onLogoutClicked()
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_switch_user -> {
                    val intent = Intent(this, UserAccountActivity::class.java)
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true
                }
                else -> return@setNavigationItemSelectedListener true
            }
        }

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
//        navView.setupWithNavController(navController)

        collectWhenStarted(logoutViewModel.isLoading) { loader.setLoading(it) }
        collectWhenStarted(logoutViewModel.loggedOut) { openLogin() }
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

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.account_details_dialog)
        dialog.setCancelable(true)
        val ip = WindowManager.LayoutParams()
        ip.copyFrom(dialog.window!!.attributes)
        ip.width = WindowManager.LayoutParams.WRAP_CONTENT
        ip.height = WindowManager.LayoutParams.WRAP_CONTENT
        val etPost = dialog.findViewById<TextView>(R.id.dialog_text_view)
        dialog.show()
        dialog.window!!.attributes = ip
    }
}
