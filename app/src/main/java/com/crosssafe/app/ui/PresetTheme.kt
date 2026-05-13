package com.crosssafe.app.ui

import android.graphics.Color

object PresetTheme {

    fun getBackgroundColors(presetId: String): Pair<Int, Int> = when (presetId) {
        "police"      -> Pair(Color.parseColor("#4A0A30"), Color.parseColor("#0A2050"))
        "ambulance"   -> Pair(Color.parseColor("#4A0A0A"), Color.parseColor("#3A2000"))
        "fire"        -> Pair(Color.parseColor("#4A1800"), Color.parseColor("#4A0800"))
        "sos"         -> Pair(Color.parseColor("#4A0000"), Color.parseColor("#200000"))
        "hazard"      -> Pair(Color.parseColor("#4A2800"), Color.parseColor("#382000"))
        "white_only"  -> Pair(Color.parseColor("#202030"), Color.parseColor("#181825"))
        "red_only"    -> Pair(Color.parseColor("#4A0808"), Color.parseColor("#280505"))
        "blue_only"   -> Pair(Color.parseColor("#04144A"), Color.parseColor("#020A4A"))
        "amber_only"  -> Pair(Color.parseColor("#4A3000"), Color.parseColor("#382500"))
        "green_only"  -> Pair(Color.parseColor("#004A0A"), Color.parseColor("#003A12"))
        "tricolor"    -> Pair(Color.parseColor("#4A0A0A"), Color.parseColor("#04144A"))
        "rainbow"     -> Pair(Color.parseColor("#1E004A"), Color.parseColor("#4A1E00"))
        "heartbeat"   -> Pair(Color.parseColor("#4A040A"), Color.parseColor("#250210"))
        "sos_white"   -> Pair(Color.parseColor("#202020"), Color.parseColor("#2A2A2A"))
        "night_red"   -> Pair(Color.parseColor("#2A0200"), Color.parseColor("#180000"))
        "night_blue"  -> Pair(Color.parseColor("#020A28"), Color.parseColor("#010518"))
        "night_amber" -> Pair(Color.parseColor("#281400"), Color.parseColor("#180B00"))
        else          -> Pair(Color.parseColor("#1E1E2A"), Color.parseColor("#1A1A20"))
    }
}
