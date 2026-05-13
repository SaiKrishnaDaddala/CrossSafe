package com.crosssafe.app

import android.app.Application
import com.crosssafe.app.util.ThemeManager

class CrossSafeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.apply(this)
    }
}
