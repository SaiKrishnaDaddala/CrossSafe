package com.crosssafe.app.model

data class FlashConfig(
    val presetId: String = "police",
    val colors: List<Int>,
    val intervalMs: Long,
    val torchEnabled: Boolean,
    val torchSyncMode: TorchSync = TorchSync.SYNC_WITH_FLASH,
    val brightness: Float = 1.0f,
    val autoStopMs: Long = 90_000L,
    val patternType: PatternType = PatternType.ALTERNATING,
    val presetName: String = ""
)

enum class TorchSync { SYNC_WITH_FLASH, ALWAYS_ON, ALWAYS_OFF }

enum class PatternType {
    ALTERNATING,
    SEQUENTIAL,
    HEARTBEAT,
    SOS,
    DOUBLE_FLASH,
    TRIPLE_FLASH
}

enum class FlashSpeed(val intervalMs: Long, val label: String) {
    PULSE(2000L, "Pulse"),
    SLOW(1200L, "Slow"),
    MEDIUM(600L, "Medium"),
    FAST(300L, "Fast"),
    RAPID(150L, "Rapid")
}
