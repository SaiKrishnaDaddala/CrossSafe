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

    init {
        cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }

    fun setEnabled(on: Boolean) {
        val id = cameraId ?: return
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
        } catch (e: IllegalArgumentException) {
            onTorchUnavailable?.invoke()
        }
    }

    fun isAvailable(): Boolean = cameraId != null

    fun setOnTorchUnavailable(callback: () -> Unit) {
        onTorchUnavailable = callback
    }
}
