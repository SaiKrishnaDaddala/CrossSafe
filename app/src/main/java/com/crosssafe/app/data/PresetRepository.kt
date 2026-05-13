package com.crosssafe.app.data

import android.content.Context
import android.graphics.Color
import com.crosssafe.app.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PresetRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val builtInPresets: List<Preset> = buildBuiltInPresets()

    fun getAllPresets(): List<Preset> = builtInPresets + getCustomPresets()

    fun getPresetById(id: String): Preset? = getAllPresets().find { it.id == id }

    fun getPinnedPresets(): List<Preset> {
        val ids = loadPinnedIds()
        return ids.mapNotNull { getPresetById(it) }
    }

    fun pinPreset(id: String) {
        val ids = loadPinnedIds().toMutableList()
        if (!ids.contains(id)) {
            if (ids.size >= 4) ids.removeLastOrNull()
            ids.add(0, id)
            savePinnedIds(ids)
        }
    }

    fun unpinPreset(id: String) {
        val ids = loadPinnedIds().toMutableList()
        ids.remove(id)
        savePinnedIds(ids)
    }

    fun replacePinnedAt(position: Int, newId: String) {
        val ids = loadPinnedIds().toMutableList()
        if (position < ids.size) {
            ids[position] = newId
        } else {
            ids.add(newId)
        }
        savePinnedIds(ids)
    }

    fun getActivePreset(): Preset {
        val id = prefs.getString(PrefKeys.LAST_PRESET_ID, "police") ?: "police"
        return getPresetById(id) ?: builtInPresets.first()
    }

    fun setActivePreset(id: String) {
        prefs.edit().putString(PrefKeys.LAST_PRESET_ID, id).apply()
    }

    fun getPresetsByCategory(category: PresetCategory): List<Preset> =
        getAllPresets().filter { it.category == category }

    fun saveCustomPreset(preset: Preset) {
        val list = getCustomPresets().toMutableList()
        val existing = list.indexOfFirst { it.id == preset.id }
        if (existing >= 0) list[existing] = preset else list.add(preset)
        prefs.edit().putString(PrefKeys.CUSTOM_PRESETS_JSON, gson.toJson(list)).apply()
    }

    fun deleteCustomPreset(id: String) {
        val list = getCustomPresets().filter { it.id != id }
        prefs.edit().putString(PrefKeys.CUSTOM_PRESETS_JSON, gson.toJson(list)).apply()
    }

    private fun getCustomPresets(): List<Preset> {
        val json = prefs.getString(PrefKeys.CUSTOM_PRESETS_JSON, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<Preset>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadPinnedIds(): List<String> {
        val raw = prefs.getString(PrefKeys.PINNED_PRESET_IDS, "police,red_only,white_only,amber_only")
        return raw?.split(",")?.filter { it.isNotBlank() }
            ?: listOf("police", "red_only", "white_only", "amber_only")
    }

    private fun savePinnedIds(ids: List<String>) {
        prefs.edit().putString(PrefKeys.PINNED_PRESET_IDS, ids.joinToString(",")).apply()
    }

    private fun buildBuiltInPresets(): List<Preset> = listOf(
        // EMERGENCY
        Preset("police", "Police", "🔵🔴", PresetCategory.EMERGENCY,
            listOf(FlashColors.RED, FlashColors.BLUE), 300L, PatternType.ALTERNATING, true, chipColor = Color.parseColor("#0066FF")),
        Preset("ambulance", "Ambulance", "🔴⚪", PresetCategory.EMERGENCY,
            listOf(FlashColors.RED, FlashColors.WHITE), 250L, PatternType.ALTERNATING, true),
        Preset("fire", "Fire truck", "🔴⚪", PresetCategory.EMERGENCY,
            listOf(FlashColors.RED, FlashColors.WHITE), 150L, PatternType.ALTERNATING, true),
        Preset("sos", "SOS", "🆘", PresetCategory.EMERGENCY,
            listOf(FlashColors.RED), 200L, PatternType.SOS, true),
        Preset("hazard", "Hazard", "🟡", PresetCategory.EMERGENCY,
            listOf(FlashColors.AMBER), 500L, PatternType.DOUBLE_FLASH, true, chipColor = Color.parseColor("#FF8C00")),
        Preset("emergency_white", "Bright alert", "⚪", PresetCategory.EMERGENCY,
            listOf(FlashColors.WHITE), 200L, PatternType.ALTERNATING, true, chipColor = Color.parseColor("#AAAAAA")),
        // SINGLE
        Preset("white_only", "Pure white", "⚪", PresetCategory.SINGLE,
            listOf(FlashColors.WHITE), 600L, PatternType.ALTERNATING, true, chipColor = Color.parseColor("#AAAAAA")),
        Preset("red_only", "Red only", "🔴", PresetCategory.SINGLE,
            listOf(FlashColors.RED), 400L, PatternType.ALTERNATING, true),
        Preset("blue_only", "Blue only", "🔵", PresetCategory.SINGLE,
            listOf(FlashColors.BLUE), 400L, PatternType.ALTERNATING, true, chipColor = Color.parseColor("#0066FF")),
        Preset("amber_only", "Amber", "🟡", PresetCategory.SINGLE,
            listOf(FlashColors.AMBER), 500L, PatternType.ALTERNATING, true, chipColor = Color.parseColor("#FF8C00")),
        Preset("green_only", "Green", "🟢", PresetCategory.SINGLE,
            listOf(FlashColors.GREEN), 500L, PatternType.ALTERNATING, false, chipColor = Color.parseColor("#00CC44")),
        Preset("yellow_only", "Yellow", "🟡", PresetCategory.SINGLE,
            listOf(FlashColors.YELLOW), 400L, PatternType.ALTERNATING, true, chipColor = Color.parseColor("#FFEE00")),
        Preset("cyan_only", "Cyan", "🔵", PresetCategory.SINGLE,
            listOf(FlashColors.CYAN), 400L, PatternType.ALTERNATING, false, chipColor = Color.parseColor("#00CCFF")),
        Preset("magenta_only", "Magenta", "🟣", PresetCategory.SINGLE,
            listOf(FlashColors.MAGENTA), 400L, PatternType.ALTERNATING, false, chipColor = Color.parseColor("#FF00CC")),
        // MULTI
        Preset("red_blue", "Red + Blue", "🔴🔵", PresetCategory.MULTI,
            listOf(FlashColors.RED, FlashColors.BLUE), 300L, PatternType.ALTERNATING, true),
        Preset("red_white", "Red + White", "🔴⚪", PresetCategory.MULTI,
            listOf(FlashColors.RED, FlashColors.WHITE), 350L, PatternType.ALTERNATING, true),
        Preset("blue_white", "Blue + White", "🔵⚪", PresetCategory.MULTI,
            listOf(FlashColors.BLUE, FlashColors.WHITE), 350L, PatternType.ALTERNATING, true, chipColor = Color.parseColor("#0066FF")),
        Preset("tricolor", "Tricolor", "🔴⚪🔵", PresetCategory.MULTI,
            listOf(FlashColors.RED, FlashColors.WHITE, FlashColors.BLUE), 300L, PatternType.SEQUENTIAL, true),
        Preset("rainbow", "Rainbow", "🌈", PresetCategory.MULTI,
            listOf(FlashColors.RED, FlashColors.ORANGE, FlashColors.YELLOW, FlashColors.GREEN, FlashColors.BLUE, FlashColors.MAGENTA),
            200L, PatternType.SEQUENTIAL, false, chipColor = Color.parseColor("#FF5500")),
        Preset("warm", "Warm flash", "🟠", PresetCategory.MULTI,
            listOf(FlashColors.RED, FlashColors.ORANGE, FlashColors.YELLOW), 250L, PatternType.SEQUENTIAL, true, chipColor = Color.parseColor("#FF5500")),
        Preset("cool", "Cool flash", "🔵", PresetCategory.MULTI,
            listOf(FlashColors.BLUE, FlashColors.CYAN, FlashColors.GREEN), 250L, PatternType.SEQUENTIAL, false, chipColor = Color.parseColor("#00CCFF")),
        Preset("india", "India", "🇮🇳", PresetCategory.MULTI,
            listOf(Color.parseColor("#FF9933"), FlashColors.WHITE, Color.parseColor("#138808")),
            400L, PatternType.SEQUENTIAL, true, chipColor = Color.parseColor("#FF9933")),
        // PATTERN
        Preset("slow_pulse", "Slow pulse", "⚪", PresetCategory.PATTERN,
            listOf(FlashColors.WHITE), 1200L, PatternType.ALTERNATING, false, chipColor = Color.parseColor("#AAAAAA")),
        Preset("medium_blink", "Medium blink", "⚪", PresetCategory.PATTERN,
            listOf(FlashColors.WHITE), 600L, PatternType.ALTERNATING, false, chipColor = Color.parseColor("#AAAAAA")),
        Preset("rapid_strobe", "Rapid strobe", "⚡", PresetCategory.PATTERN,
            listOf(FlashColors.WHITE), 100L, PatternType.ALTERNATING, true, chipColor = Color.parseColor("#AAAAAA")),
        Preset("heartbeat", "Heartbeat", "❤️", PresetCategory.PATTERN,
            listOf(FlashColors.RED), 120L, PatternType.HEARTBEAT, true),
        Preset("double_flash", "Double flash", "⚪", PresetCategory.PATTERN,
            listOf(FlashColors.WHITE), 120L, PatternType.DOUBLE_FLASH, true, chipColor = Color.parseColor("#AAAAAA")),
        Preset("triple_flash", "Triple flash", "⚪", PresetCategory.PATTERN,
            listOf(FlashColors.WHITE), 120L, PatternType.TRIPLE_FLASH, true, chipColor = Color.parseColor("#AAAAAA")),
        Preset("sos_white", "SOS (white)", "🆘", PresetCategory.PATTERN,
            listOf(FlashColors.WHITE), 200L, PatternType.SOS, true, chipColor = Color.parseColor("#AAAAAA")),
        // NIGHT
        Preset("night_red", "Dim red", "🔴", PresetCategory.NIGHT,
            listOf(Color.parseColor("#CC0000")), 1000L, PatternType.ALTERNATING, false),
        Preset("night_blue", "Soft blue", "🔵", PresetCategory.NIGHT,
            listOf(Color.parseColor("#004499")), 800L, PatternType.ALTERNATING, false, chipColor = Color.parseColor("#004499")),
        Preset("night_amber", "Low amber", "🟡", PresetCategory.NIGHT,
            listOf(Color.parseColor("#CC6600")), 900L, PatternType.ALTERNATING, false, chipColor = Color.parseColor("#CC6600")),
        Preset("night_white", "Night white", "⚪", PresetCategory.NIGHT,
            listOf(FlashColors.WHITE), 800L, PatternType.ALTERNATING, false, chipColor = Color.parseColor("#AAAAAA")),
        Preset("night_green", "Stealth green", "🟢", PresetCategory.NIGHT,
            listOf(Color.parseColor("#006600")), 1000L, PatternType.ALTERNATING, false, chipColor = Color.parseColor("#006600"))
    )
}
