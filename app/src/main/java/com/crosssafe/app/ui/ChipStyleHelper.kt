package com.crosssafe.app.ui

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View

object ChipStyleHelper {

    fun applySelectedStyle(chip: View, accentColor: Int) {
        chip.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 14.dp
            setStroke(2.dp.toInt(), accentColor)
            setColor(Color.argb(30, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)))
        }
    }

    fun applyUnselectedStyle(chip: View) {
        chip.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 14.dp
            setStroke(1, Color.argb(50, 255, 255, 255))
            setColor(Color.argb(25, 255, 255, 255))
        }
    }

    fun animateChipSelection(chip: View) {
        chip.animate()
            .scaleX(1.05f).scaleY(1.05f)
            .setDuration(100L)
            .withEndAction {
                chip.animate().scaleX(1f).scaleY(1f).setDuration(100L).start()
            }.start()
    }

    private val Int.dp: Float get() = this * Resources.getSystem().displayMetrics.density
}
