package ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ebookfrenzy.duvproject.R

class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication_activity)

        supportActionBar?.hide()
    }
}
