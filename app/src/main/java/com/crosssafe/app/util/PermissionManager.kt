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

class PermissionManager(private val activity: AppCompatActivity) {

    private val notificationLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* non-blocking */ }


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
