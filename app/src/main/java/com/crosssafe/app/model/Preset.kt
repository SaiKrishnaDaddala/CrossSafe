package com.crosssafe.app.model

import android.graphics.Color

data class Preset(
    val id: String,
    val name: String,
    val emoji: String,
    val category: PresetCategory,
    val colors: List<Int>,
    val intervalMs: Long,
    val patternType: PatternType,
    val torchEnabled: Boolean = true,
    val isBuiltIn: Boolean = true,
    val isPinned: Boolean = false,
    val chipColor: Int = Color.parseColor("#EF4444")
)

enum class PresetCategory(val label: String) {
    EMERGENCY("Emergency"),
    SINGLE("Single color"),
    MULTI("Multi color"),
    PATTERN("Pattern"),
    NIGHT("Night mode"),
    CUSTOM("My presets")
}
