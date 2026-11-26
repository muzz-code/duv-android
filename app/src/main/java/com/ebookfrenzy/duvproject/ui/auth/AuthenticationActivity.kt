package com.ebookfrenzy.duvproject.ui.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ebookfrenzy.duvproject.R
import dagger.hilt.android.AndroidEntryPoint

/** Hosts the auth navigation graph (options → login / sign up → forgot password). */
@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication_activity)
        supportActionBar?.hide()
    }

    /** Back arrow on the auth screens (android:onClick in their layouts). */
    @Suppress("UNUSED_PARAMETER")
    fun onDuvBackPressed(view: View) = onBackPressedDispatcher.onBackPressed()
}
