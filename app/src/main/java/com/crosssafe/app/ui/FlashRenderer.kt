package com.crosssafe.app.ui

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View

class FlashRenderer(private val rootView: View) {

    private var currentColor = Color.BLACK
    private var colorAnimator: ValueAnimator? = null

    fun flashToColor(newColor: Int, durationMs: Long = 80L) {
        colorAnimator?.cancel()

        if (newColor == Color.BLACK) {
            rootView.setBackgroundColor(Color.BLACK)
            currentColor = Color.BLACK
            return
        }

        val from = currentColor
        colorAnimator = ValueAnimator.ofArgb(from, newColor).apply {
            duration = durationMs
            addUpdateListener { anim ->
                rootView.setBackgroundColor(anim.animatedValue as Int)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    currentColor = newColor
                }
            })
            start()
        }
    }

    fun reset() {
        colorAnimator?.cancel()
        rootView.setBackgroundColor(Color.BLACK)
        currentColor = Color.BLACK
    }
}
