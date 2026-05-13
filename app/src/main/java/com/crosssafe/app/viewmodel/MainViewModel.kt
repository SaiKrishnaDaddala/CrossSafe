package com.crosssafe.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.crosssafe.app.data.PresetRepository
import com.crosssafe.app.model.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PresetRepository(application)
    val prefs = application.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)

    val pinnedPresets = MutableLiveData(repo.getPinnedPresets())
    val activePreset = MutableLiveData(repo.getActivePreset())
    val torchEnabled = MutableLiveData(prefs.getBoolean(PrefKeys.TORCH_DEFAULT_ON, true))
    val autoStopMs = MutableLiveData(prefs.getString(PrefKeys.AUTO_STOP_MS, "90000")?.toLongOrNull() ?: 90_000L)
    val flashSpeed = MutableLiveData(prefs.getString(PrefKeys.FLASH_SPEED, "MEDIUM") ?: "MEDIUM")

    fun selectPreset(preset: Preset) {
        repo.setActivePreset(preset.id)
        activePreset.value = preset
    }

    fun setTorch(on: Boolean) {
        prefs.edit().putBoolean(PrefKeys.TORCH_DEFAULT_ON, on).apply()
        torchEnabled.value = on
    }

    fun setAutoStop(ms: Long) {
        prefs.edit().putString(PrefKeys.AUTO_STOP_MS, ms.toString()).apply()
        autoStopMs.value = ms
    }

    fun setFlashSpeed(speed: String) {
        prefs.edit().putString(PrefKeys.FLASH_SPEED, speed).apply()
        flashSpeed.value = speed
    }

    fun refreshPinnedPresets() {
        pinnedPresets.value = repo.getPinnedPresets()
    }

    fun refreshActivePreset() {
        activePreset.value = repo.getActivePreset()
    }

    fun getRepository(): PresetRepository = repo

    fun buildFlashConfig(): FlashConfig {
        val preset = activePreset.value ?: repo.getActivePreset()
        val speedStr = flashSpeed.value ?: "MEDIUM"
        val speed = try { FlashSpeed.valueOf(speedStr) } catch (e: Exception) { FlashSpeed.MEDIUM }
        val intervalMs = if (preset.patternType == PatternType.ALTERNATING || preset.patternType == PatternType.SEQUENTIAL) {
            speed.intervalMs
        } else {
            preset.intervalMs
        }
        return FlashConfig(
            presetId = preset.id,
            colors = preset.colors,
            intervalMs = intervalMs,
            torchEnabled = torchEnabled.value ?: true,
            torchSyncMode = TorchSync.SYNC_WITH_FLASH,
            brightness = (prefs.getInt(PrefKeys.DEFAULT_BRIGHTNESS, 100) / 100f).coerceIn(0.1f, 1.0f),
            autoStopMs = autoStopMs.value ?: 90_000L,
            patternType = preset.patternType,
            presetName = preset.name
        )
    }
}
