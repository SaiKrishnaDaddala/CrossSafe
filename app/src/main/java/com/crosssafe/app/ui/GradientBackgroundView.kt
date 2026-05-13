package com.crosssafe.app.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin

class GradientBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var color1 = Color.parseColor("#1A0A0A")
    private var color2 = Color.parseColor("#0A0A1A")

    private var animTick = 0f
    private val breathingAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 8000L
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener {
            animTick = it.animatedValue as Float
            invalidate()
        }
    }

    private var colorAnimator: ValueAnimator? = null

    init {
        breathingAnimator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        val cx1 = w * (0.2f + 0.15f * sin(animTick * 2 * Math.PI).toFloat())
        val cy1 = h * (0.25f + 0.12f * cos(animTick * 2 * Math.PI).toFloat())

        val cx2 = w * (0.75f + 0.15f * sin((animTick + 0.5f) * 2 * Math.PI).toFloat())
        val cy2 = h * (0.65f + 0.12f * cos((animTick + 0.5f) * 2 * Math.PI).toFloat())

        val radius1 = w * 0.65f
        gradientPaint.shader = RadialGradient(
            cx1, cy1, radius1,
            intArrayOf(Color.argb(60, Color.red(color1), Color.green(color1), Color.blue(color1)), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx1, cy1, radius1, gradientPaint)

        val radius2 = w * 0.55f
        gradientPaint.shader = RadialGradient(
            cx2, cy2, radius2,
            intArrayOf(Color.argb(45, Color.red(color2), Color.green(color2), Color.blue(color2)), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx2, cy2, radius2, gradientPaint)
    }

    fun transitionToColors(newColor1: Int, newColor2: Int) {
        val fromColor1 = color1
        val fromColor2 = color2

        colorAnimator?.cancel()
        colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600L
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float
                color1 = blendColors(fromColor1, newColor1, t)
                color2 = blendColors(fromColor2, newColor2, t)
            }
            start()
        }
    }

    private fun blendColors(from: Int, to: Int, ratio: Float): Int {
        val inv = 1f - ratio
        return Color.rgb(
            (Color.red(from) * inv + Color.red(to) * ratio).toInt(),
            (Color.green(from) * inv + Color.green(to) * ratio).toInt(),
            (Color.blue(from) * inv + Color.blue(to) * ratio).toInt()
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        breathingAnimator.cancel()
        colorAnimator?.cancel()
    }
}
