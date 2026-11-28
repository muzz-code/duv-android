package com.ebookfrenzy.duvproject.ui.common

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

/** Colours the status bar and picks dark or light icons so they stay readable. */
fun Activity.setStatusBar(@ColorRes colorRes: Int, darkIcons: Boolean) {
    window.statusBarColor = ContextCompat.getColor(this, colorRes)
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = darkIcons
}
