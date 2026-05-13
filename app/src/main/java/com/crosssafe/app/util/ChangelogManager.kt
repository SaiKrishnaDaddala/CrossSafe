package com.crosssafe.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.crosssafe.app.BuildConfig
import com.crosssafe.app.R
import com.crosssafe.app.model.PrefKeys
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChangelogManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)

    fun showChangelogIfUpdated(activity: AppCompatActivity) {
        val currentVersion = BuildConfig.VERSION_CODE
        val lastSeen = prefs.getInt(PrefKeys.LAST_SEEN_VERSION, 0)
        if (currentVersion > lastSeen) {
            prefs.edit().putInt(PrefKeys.LAST_SEEN_VERSION, currentVersion).apply()
            showChangelogDialog(activity)
        }
    }

    private fun showChangelogDialog(activity: AppCompatActivity) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("What's new in CrossSafe ${BuildConfig.VERSION_NAME}")
            .setMessage(context.getString(R.string.changelog_current_version))
            .setPositiveButton("Got it", null)
            .show()
    }
}
