package com.crosssafe.app.ui

import android.graphics.Color

object PresetTheme {

    fun getBackgroundColors(presetId: String): Pair<Int, Int> = when (presetId) {
        "police"      -> Pair(Color.parseColor("#1A0515"), Color.parseColor("#05101A"))
        "ambulance"   -> Pair(Color.parseColor("#1A0505"), Color.parseColor("#1A1005"))
        "fire"        -> Pair(Color.parseColor("#1A0800"), Color.parseColor("#1A0500"))
        "sos"         -> Pair(Color.parseColor("#1A0000"), Color.parseColor("#0D0000"))
        "hazard"      -> Pair(Color.parseColor("#1A0F00"), Color.parseColor("#150D00"))
        "white_only"  -> Pair(Color.parseColor("#111115"), Color.parseColor("#0D0D12"))
        "red_only"    -> Pair(Color.parseColor("#1A0303"), Color.parseColor("#0D0202"))
        "blue_only"   -> Pair(Color.parseColor("#020A1A"), Color.parseColor("#01061A"))
        "amber_only"  -> Pair(Color.parseColor("#1A1000"), Color.parseColor("#150D00"))
        "green_only"  -> Pair(Color.parseColor("#001A05"), Color.parseColor("#00150A"))
        "tricolor"    -> Pair(Color.parseColor("#1A0505"), Color.parseColor("#020A1A"))
        "rainbow"     -> Pair(Color.parseColor("#0A001A"), Color.parseColor("#1A0A00"))
        "heartbeat"   -> Pair(Color.parseColor("#1A0205"), Color.parseColor("#0D0108"))
        "sos_white"   -> Pair(Color.parseColor("#0F0F0F"), Color.parseColor("#111111"))
        "night_red"   -> Pair(Color.parseColor("#120100"), Color.parseColor("#0A0000"))
        "night_blue"  -> Pair(Color.parseColor("#01030F"), Color.parseColor("#010209"))
        "night_amber" -> Pair(Color.parseColor("#0F0800"), Color.parseColor("#0A0500"))
        else          -> Pair(Color.parseColor("#0A0A0A"), Color.parseColor("#0F0F0F"))
    }
}
