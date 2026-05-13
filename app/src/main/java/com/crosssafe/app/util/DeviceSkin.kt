package com.crosssafe.app.util

import android.os.Build

object DeviceSkin {
    val isMIUI: Boolean get() = !getSystemProperty("ro.miui.ui.version.name").isNullOrEmpty()
    val isOneUI: Boolean get() = Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    val isColorOS: Boolean get() = Build.MANUFACTURER.equals("oppo", ignoreCase = true)
        || Build.MANUFACTURER.equals("realme", ignoreCase = true)
    val isFunTouchOS: Boolean get() = Build.MANUFACTURER.equals("vivo", ignoreCase = true)
    val isOxygenOS: Boolean get() = Build.MANUFACTURER.equals("oneplus", ignoreCase = true)

    private fun getSystemProperty(key: String): String? = try {
        Class.forName("android.os.SystemProperties")
            .getMethod("get", String::class.java)
            .invoke(null, key) as? String
    } catch (e: Exception) { null }

    fun getAutostartMessage(): String = when {
        isMIUI -> "On Xiaomi/MIUI: Settings → Apps → CrossSafe → Other permissions → Autostart → ON"
        isOneUI -> "On Samsung One UI: Settings → Device care → Battery → Background usage limits → Never sleeping apps → Add CrossSafe"
        isColorOS -> "On ColorOS/Realme: Settings → Battery → App energy efficiency → CrossSafe → Allow background run"
        isFunTouchOS -> "On Vivo: Settings → Battery → Background app management → CrossSafe → Allow"
        else -> "On your device: go to Settings → Apps → CrossSafe → Battery → Unrestricted"
    }
}
