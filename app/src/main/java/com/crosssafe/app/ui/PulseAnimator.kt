package com.crosssafe.app.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

object PulseAnimator {

    fun start(view: View): AnimatorSet {
        val interp = AccelerateDecelerateInterpolator()
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.06f, 1f).apply {
            duration = 2000L
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = interp
        }
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.06f, 1f).apply {
            duration = 2000L
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = interp
        }
        return AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            start()
        }
    }

    fun stop(view: View, animatorSet: AnimatorSet) {
        animatorSet.cancel()
        view.scaleX = 1f
        view.scaleY = 1f
    }
}
