package com.ebookfrenzy.duvproject.ui.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ebookfrenzy.duvproject.R

/**
 * A spinner over a dimmed screen that blocks taps while a request runs.
 * Screens call [setLoading] from their render function with the ViewModel's isLoading flag.
 */
class LoadingOverlay(private val context: Context) {

    private var dialog: Dialog? = null

    fun setLoading(loading: Boolean) {
        if (loading) show() else hide()
    }

    private fun show() {
        if (dialog?.isShowing == true) return
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_loading)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.4f)
            show()
        }
    }

    fun hide() {
        dialog?.dismiss()
        dialog = null
    }
}

/** A [LoadingOverlay] that is dismissed automatically when the fragment's view goes away. */
fun Fragment.loadingOverlay(): LoadingOverlay = LoadingOverlay(requireContext()).also { overlay ->
    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) = overlay.hide()
    })
}
