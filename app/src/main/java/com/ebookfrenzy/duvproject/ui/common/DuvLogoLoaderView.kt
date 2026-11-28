package com.ebookfrenzy.duvproject.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.ebookfrenzy.duvproject.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * The DUV logo's 11 bars, moving like a music equaliser: each bar grows and shrinks around its own logo height,
 * in a wave that ripples out from the middle bar. Bar positions and heights are those of
 * drawable/splash_duv_logo.xml (118 x 70 units), scaled to the view's size.
 */
class DuvLogoLoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    /** Left edge and full height of each bar, in logo units (bars are [BAR_WIDTH] wide, centred vertically). */
    private val bars = listOf(0f to 23f, 11f to 27f, 22f to 37f, 34f to 49f, 45f to 62f, 57f to 70f,
        68f to 62f, 79f to 49f, 89f to 37f, 100f to 27f, 111f to 23f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.loader_bar)
    }
    private val rect = RectF()
    private var phase = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = context.resources.getInteger(R.integer.loader_cycle_ms).toLong()
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        contentDescription = context.getString(R.string.please_wait)
    }

    override fun onDraw(canvas: Canvas) {
        val scale = minOf(width / LOGO_WIDTH, height / LOGO_HEIGHT)
        val left0 = (width - LOGO_WIDTH * scale) / 2
        val centerY = height / 2f
        val radius = BAR_WIDTH * scale / 2
        val middle = (bars.size - 1) / 2f
        bars.forEachIndexed { index, (x, fullHeight) ->
            // Bars further from the middle lag behind it, so the movement spreads outwards like sound.
            val lag = abs(index - middle) / bars.size
            val wave = (sin(2 * PI * (phase - lag)).toFloat() + 1) / 2          // 0..1
            val barHeight = fullHeight * (MIN_SCALE + (1 - MIN_SCALE) * wave) * scale
            rect.set(left0 + x * scale, centerY - barHeight / 2, left0 + (x + BAR_WIDTH) * scale, centerY + barHeight / 2)
            canvas.drawRoundRect(rect, radius, radius, paint)
        }
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) animator.start() else animator.cancel()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val LOGO_WIDTH = 118f
        const val LOGO_HEIGHT = 70f
        const val BAR_WIDTH = 7f
        /** Smallest a bar gets, as a share of its logo height. */
        const val MIN_SCALE = 0.3f
    }
}
