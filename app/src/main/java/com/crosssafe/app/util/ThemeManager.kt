package com.crosssafe.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    const val THEME_DARK   = "dark"
    const val THEME_LIGHT  = "light"
    const val THEME_SYSTEM = "system"

    fun apply(context: Context) {
        when (getCurrent(context)) {
            THEME_DARK   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_LIGHT  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else         -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun save(context: Context, theme: String) {
        context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)
            .edit().putString("app_theme", theme).apply()
    }

    fun getCurrent(context: Context): String {
        return context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)
            .getString("app_theme", THEME_SYSTEM) ?: THEME_SYSTEM
    }
}
