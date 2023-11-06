package ui.entertainer

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.EntertainerDashboardActivityBinding
import com.google.android.material.navigation.NavigationView

class EntertainerDashboardActivity : AppCompatActivity() {
    // declare my variables for the views and also for binding
    private lateinit var binding: EntertainerDashboardActivityBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration
    private lateinit var listener: NavController.OnDestinationChangedListener
    private lateinit var toolBar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EntertainerDashboardActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize the views
        drawerLayout = binding.entertainerDashboardDrawerLayout
        navController = findNavController(R.id.fragmentContainerView)
        navView = binding.entertainerDashboardNavViewLayout
        navView.setupWithNavController(navController)

        appBarConfig = AppBarConfiguration(navController.graph, drawerLayout)

        listener =
            NavController.OnDestinationChangedListener { controller, destination, arguments ->
                when (destination.id) {
                    R.id.dashboardFragment -> supportActionBar?.setBackgroundDrawable(
                        ColorDrawable(
                            getColor(R.color.white)
                        )
                    )

                    R.id.emergencyContactFragment -> supportActionBar?.setBackgroundDrawable(
                        ColorDrawable(
                            getColor(R.color.white)
                        )
                    )
                    else -> supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor((R.color.white))))
                }
            }

        // set the toggle view as the navigation drawer also
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        setupActionBarWithNavController(navController, appBarConfig)

//        navView.setNavigationItemSelectedListener {
//            it.isChecked = true
//            when (it.itemId) {
//                R.id.nav_bank_account_details -> Toast.makeText(applicationContext, "CLicked Bank Details", Toast.LENGTH_SHORT).show()
//                R.id.nav_youtube_channel -> Toast.makeText(applicationContext, "Clicked Youtube Channel", Toast.LENGTH_SHORT).show()
//                R.id.nav_valid_identification -> Toast.makeText(applicationContext, "Clicked ID", Toast.LENGTH_SHORT).show()
//                R.id.nav_switch_user -> Toast.makeText(applicationContext, "Clicked Switch User", Toast.LENGTH_SHORT).show()
//                R.id.nav_logout -> Toast.makeText(applicationContext, "Logout", Toast.LENGTH_SHORT).show()
//            }
//            true
//        }
    }

    // override onOptionsItemSelected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragmentContainerView)
        return navController.navigateUp(appBarConfig) ||
            super.onSupportNavigateUp()
    }
}
