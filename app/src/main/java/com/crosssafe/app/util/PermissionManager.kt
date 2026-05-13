package com.crosssafe.app.util

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionManager(private val activity: AppCompatActivity) {

    private var cameraGrantedCallback: (() -> Unit)? = null
    private var cameraDeniedCallback: (() -> Unit)? = null

    private val cameraLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraGrantedCallback?.invoke()
        else cameraDeniedCallback?.invoke()
    }

    private val notificationLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* non-blocking */ }

    fun requestCameraIfNeeded(onGranted: () -> Unit, onDenied: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> onGranted()

            activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("Camera permission needed")
                    .setMessage(
                        "CrossSafe needs camera access to turn on your phone's flashlight. " +
                        "The torch flashes in sync with the screen to make you more visible to drivers."
                    )
                    .setPositiveButton("Allow") { _, _ -> requestCamera(onGranted, onDenied) }
                    .setNegativeButton("Not now") { _, _ -> onDenied() }
                    .show()
            }

            else -> requestCamera(onGranted, onDenied)
        }
    }

    private fun requestCamera(onGranted: () -> Unit, onDenied: () -> Unit) {
        cameraGrantedCallback = onGranted
        cameraDeniedCallback = onDenied
        cameraLauncher.launch(Manifest.permission.CAMERA)
    }

    fun requestNotificationIfNeeded(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onGranted()
            }
        } else {
            onGranted()
        }
    }

    fun isTorchAvailable(): Boolean =
        activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(this)
        }
    }
}
