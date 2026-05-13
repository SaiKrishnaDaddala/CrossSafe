package com.crosssafe.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosssafe.app.model.FlashConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FlashViewModel : ViewModel() {
    var config: FlashConfig? = null
    val remainingMs = MutableLiveData<Long>()
    val elapsedMs = MutableLiveData<Long>(0L)
    val isWarning = MutableLiveData(false)
    val currentPresetName = MutableLiveData<String>()
    val currentBrightness = MutableLiveData(1.0f)
    val shouldExit = MutableLiveData(false)

    private var countdownJob: Job? = null

    fun startCountdown(totalMs: Long) {
        if (totalMs <= 0) {
            startElapsedTimer()
            return
        }
        remainingMs.value = totalMs
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = totalMs
            while (remaining > 0) {
                delay(1000L)
                remaining -= 1000L
                remainingMs.value = remaining
                if (remaining <= 10_000L) {
                    isWarning.value = true
                }
            }
            shouldExit.value = true
        }
    }

    private fun startElapsedTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var elapsed = 0L
            while (true) {
                delay(1000L)
                elapsed += 1000L
                elapsedMs.value = elapsed
            }
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
