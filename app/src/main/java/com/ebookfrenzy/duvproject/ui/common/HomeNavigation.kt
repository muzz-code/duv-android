package com.ebookfrenzy.duvproject.ui.common

import android.app.Activity
import android.content.Intent
import com.ebookfrenzy.duvproject.ui.auth.AuthenticationActivity
import com.ebookfrenzy.duvproject.ui.entertainer.EntertainerActivity
import com.ebookfrenzy.duvproject.ui.user.UserAccountActivity

/** Opens the right home screen for the account and removes the login screens from the back stack. */
fun Activity.openHome(isEntertainer: Boolean) {
    val target = if (isEntertainer) EntertainerActivity::class.java else UserAccountActivity::class.java
    startActivity(Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
}

/** After logging out: back to the login/sign-up screens with nothing behind them. */
fun Activity.openLogin() {
    startActivity(
        Intent(this, AuthenticationActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
    )
}
