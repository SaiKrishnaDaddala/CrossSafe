package com.crosssafe.app.engine

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.Window
import com.crosssafe.app.model.FlashConfig
import com.crosssafe.app.model.PatternType
import com.crosssafe.app.model.TorchSync
import com.crosssafe.app.ui.FlashRenderer

class FlashEngine(
    private val context: Context,
    private val window: Window,
    private val rootView: View,
    private var config: FlashConfig
) {
    private val handler = Handler(Looper.getMainLooper())
    var isRunning = false
        private set
    private var currentColorIndex = 0
    private var patternStep = 0
    private var torchManager: TorchManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var originalBrightness: Float = -1f
    private var flashRenderer: FlashRenderer? = null
    private var onPresetChanged: ((String) -> Unit)? = null
    private var allPresets: List<com.crosssafe.app.model.Preset> = emptyList()
    private var currentPresetIndex = 0

    init {
        torchManager = TorchManager(context)
    }

    fun setFlashRenderer(renderer: FlashRenderer) {
        flashRenderer = renderer
    }

    private fun setRootColor(color: Int) {
        flashRenderer?.flashToColor(color) ?: rootView.setBackgroundColor(color)
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        patternStep = 0
        currentColorIndex = 0
        acquireWakeLock()
        saveBrightness()
        setBrightness(config.brightness)
        if (config.torchEnabled && config.torchSyncMode == TorchSync.ALWAYS_ON) {
            torchManager?.setEnabled(true)
        }
        scheduleNextFlash()
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        flashRenderer?.reset() ?: rootView.setBackgroundColor(Color.BLACK)
        torchManager?.setEnabled(false)
        restoreBrightness()
        releaseWakeLock()
    }

    fun updateConfig(newConfig: FlashConfig) {
        config = newConfig
        patternStep = 0
        currentColorIndex = 0
    }

    fun setBrightness(level: Float) {
        val params = window.attributes
        params.screenBrightness = level.coerceIn(0.1f, 1.0f)
        window.attributes = params
    }

    fun cycleToNextPreset() {
        if (allPresets.isNotEmpty()) {
            currentPresetIndex = (currentPresetIndex + 1) % allPresets.size
            val preset = allPresets[currentPresetIndex]
            updateConfig(config.copy(
                presetId = preset.id,
                colors = preset.colors,
                intervalMs = preset.intervalMs,
                patternType = preset.patternType,
                torchEnabled = preset.torchEnabled,
                presetName = preset.name
            ))
            onPresetChanged?.invoke(preset.name)
        }
    }

    fun cycleToPreviousPreset() {
        if (allPresets.isNotEmpty()) {
            currentPresetIndex = (currentPresetIndex - 1 + allPresets.size) % allPresets.size
            val preset = allPresets[currentPresetIndex]
            updateConfig(config.copy(
                presetId = preset.id,
                colors = preset.colors,
                intervalMs = preset.intervalMs,
                patternType = preset.patternType,
                torchEnabled = preset.torchEnabled,
                presetName = preset.name
            ))
            onPresetChanged?.invoke(preset.name)
        }
    }

    fun getCurrentPresetName(): String = config.presetName

    fun setPresetList(presets: List<com.crosssafe.app.model.Preset>, currentId: String) {
        allPresets = presets
        currentPresetIndex = presets.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
    }

    fun setOnPresetChanged(callback: (String) -> Unit) {
        onPresetChanged = callback
    }

    private fun scheduleNextFlash() {
        if (!isRunning) return
        when (config.patternType) {
            PatternType.ALTERNATING, PatternType.SEQUENTIAL -> scheduleSimpleFlash()
            PatternType.HEARTBEAT -> scheduleHeartbeat()
            PatternType.SOS -> scheduleSos()
            PatternType.DOUBLE_FLASH -> scheduleDoubleFlash()
            PatternType.TRIPLE_FLASH -> scheduleTripleFlash()
        }
    }

    private fun scheduleSimpleFlash() {
        val color = config.colors[currentColorIndex % config.colors.size]
        currentColorIndex = (currentColorIndex + 1) % config.colors.size
        setRootColor(color)
        syncTorch(color)
        handler.postDelayed({ scheduleNextFlash() }, config.intervalMs)
    }

    private fun scheduleHeartbeat() {
        val color = config.colors.firstOrNull() ?: Color.RED
        val beatPattern = listOf(
            Pair(color, 120L),
            Pair(Color.BLACK, 80L),
            Pair(color, 120L),
            Pair(Color.BLACK, 600L)
        )
        schedulePattern(beatPattern, 0)
    }

    private fun schedulePattern(pattern: List<Pair<Int, Long>>, step: Int) {
        if (!isRunning) return
        val (color, delay) = pattern[step]
        setRootColor(color)
        syncTorch(color)
        val nextStep = (step + 1) % pattern.size
        handler.postDelayed({ schedulePattern(pattern, nextStep) }, delay)
    }

    private val sosPattern: List<Pair<Int, Long>> by lazy {
        val color = config.colors.firstOrNull() ?: Color.RED
        val dot = 200L
        val dash = 600L
        val gap = 200L
        val letterGap = 800L
        listOf(
            Pair(color, dot), Pair(Color.BLACK, gap),
            Pair(color, dot), Pair(Color.BLACK, gap),
            Pair(color, dot), Pair(Color.BLACK, letterGap),
            Pair(color, dash), Pair(Color.BLACK, gap),
            Pair(color, dash), Pair(Color.BLACK, gap),
            Pair(color, dash), Pair(Color.BLACK, letterGap),
            Pair(color, dot), Pair(Color.BLACK, gap),
            Pair(color, dot), Pair(Color.BLACK, gap),
            Pair(color, dot), Pair(Color.BLACK, 2000L)
        )
    }

    private fun scheduleSos() {
        schedulePattern(sosPattern, 0)
    }

    private fun scheduleDoubleFlash() {
        val color = config.colors.firstOrNull() ?: Color.WHITE
        val pattern = listOf(
            Pair(color, 120L),
            Pair(Color.BLACK, 80L),
            Pair(color, 120L),
            Pair(Color.BLACK, 700L)
        )
        schedulePattern(pattern, 0)
    }

    private fun scheduleTripleFlash() {
        val color = config.colors.firstOrNull() ?: Color.WHITE
        val pattern = listOf(
            Pair(color, 100L),
            Pair(Color.BLACK, 80L),
            Pair(color, 100L),
            Pair(Color.BLACK, 80L),
            Pair(color, 100L),
            Pair(Color.BLACK, 700L)
        )
        schedulePattern(pattern, 0)
    }

    private fun syncTorch(color: Int) {
        when (config.torchSyncMode) {
            TorchSync.SYNC_WITH_FLASH -> torchManager?.setEnabled(color != Color.BLACK && config.torchEnabled)
            TorchSync.ALWAYS_ON -> torchManager?.setEnabled(config.torchEnabled)
            TorchSync.ALWAYS_OFF -> torchManager?.setEnabled(false)
        }
    }

    private fun saveBrightness() {
        originalBrightness = window.attributes.screenBrightness
        if (originalBrightness < 0) {
            originalBrightness = try {
                Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS, 128
                ) / 255f
            } catch (e: Exception) {
                0.5f
            }
        }
    }

    private fun restoreBrightness() {
        val params = window.attributes
        params.screenBrightness = originalBrightness
        window.attributes = params
    }

    private fun acquireWakeLock() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "CrossSafe:FlashWakeLock"
        )
        wakeLock?.acquire(30 * 60 * 1000L)
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }
}
