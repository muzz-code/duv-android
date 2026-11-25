package com.ebookfrenzy.duvproject.ui.common

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView

/**
 * Shows [fullText] with the part from [clickableStart] to the end styled bold/white/larger and
 * clickable. Shared by the login and sign-up screens (it used to be copy-pasted in both).
 */
fun TextView.setStyledLinkText(
    fullText: String,
    styleStart: Int,
    clickableStart: Int,
    onClick: () -> Unit,
) {
    val end = fullText.length
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) = onClick()

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }
    text = SpannableString(fullText).apply {
        setSpan(clickableSpan, clickableStart, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        setSpan(ForegroundColorSpan(Color.WHITE), styleStart, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        setSpan(RelativeSizeSpan(1.1f), styleStart, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        setSpan(StyleSpan(Typeface.BOLD), styleStart, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}
