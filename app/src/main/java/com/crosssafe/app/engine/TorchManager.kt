package com.crosssafe.app.engine

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.widget.Toast

class TorchManager(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var onTorchUnavailable: (() -> Unit)? = null
    private var isTorchOn = false
    private var torchCallback: CameraManager.TorchCallback? = null

    init {
        cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
        registerTorchCallback()
    }

    private fun registerTorchCallback() {
        torchCallback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                // Track system torch state changes
                // If system turns off torch (e.g., from quick settings), we need to know
                if (cameraId == this@TorchManager.cameraId && !enabled && isTorchOn) {
                    isTorchOn = false
                }
            }
        }
        cameraManager.registerTorchCallback(torchCallback!!, null)
    }

    fun setEnabled(on: Boolean) {
        val id = cameraId ?: return
        isTorchOn = on
        try {
            cameraManager.setTorchMode(id, on)
        } catch (e: CameraAccessException) {
            when (e.reason) {
                CameraAccessException.CAMERA_IN_USE ->
                    Toast.makeText(context, "Torch unavailable — camera in use", Toast.LENGTH_SHORT).show()
                CameraAccessException.MAX_CAMERAS_IN_USE ->
                    Toast.makeText(context, "Torch unavailable — too many camera apps open", Toast.LENGTH_SHORT).show()
                else -> { /* log and continue */ }
            }
            isTorchOn = false
        } catch (e: IllegalArgumentException) {
            onTorchUnavailable?.invoke()
            isTorchOn = false
        } catch (e: Exception) {
            // Catch any other exceptions to prevent crashes
            isTorchOn = false
        }
    }

    fun isAvailable(): Boolean = cameraId != null

    fun setOnTorchUnavailable(callback: () -> Unit) {
        onTorchUnavailable = callback
    }

    fun cleanup() {
        // Force torch off and unregister callback
        try {
            setEnabled(false)
            torchCallback?.let { cameraManager.unregisterTorchCallback(it) }
            torchCallback = null
        } catch (e: Exception) {
            // Silently ignore cleanup errors
        }
    }
}
